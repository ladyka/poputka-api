package by.ladyka.poputka.service;

import by.ladyka.poputka.data.dto.bookingTripOverview.TripBookingOverviewItemDto;
import by.ladyka.poputka.data.dto.bookingTripOverview.TripBookingOverviewResponseDto;
import by.ladyka.poputka.data.dto.bookingTripOverview.TripBookingOverviewSummaryDto;
import by.ladyka.poputka.data.dto.BookingAvailableStatusesResponseDto;
import by.ladyka.poputka.data.dto.BookingChatDto;
import by.ladyka.poputka.data.dto.BookingCreateDto;
import by.ladyka.poputka.data.dto.BookingDto;
import by.ladyka.poputka.data.dto.BookingMessageDto;
import by.ladyka.poputka.data.dto.BookingStatusChangeRequestDto;
import by.ladyka.poputka.data.dto.payload.MessagePayload;
import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.enums.BookingOverviewScope;
import by.ladyka.poputka.data.enums.BookingStatus;
import by.ladyka.poputka.data.enums.MessageStatus;
import by.ladyka.poputka.data.repository.BookingMessageRepository;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.mapper.BookingMapper;
import by.ladyka.poputka.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PoputkaUserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final BookingMessageRepository bookingMessageRepository;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BookingDto createBooking(String username, BookingCreateDto bookingCreateDto) {
        PoputkaUser passenger = userRepository.findByUsername(username).orElseThrow();
        Booking booking = bookingRepository.findBookingByTripIdAndPassengerId(bookingCreateDto.getTripId(), passenger.getId())
                .orElseGet(() -> {
                    TripEntity trip = tripRepository.findById(bookingCreateDto.getTripId())
                            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
                    if (trip.getOwnerId() == passenger.getId()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't book your own trip");
                    }
                    if (countOccupyingPassengerSeatBookings(trip.getId())
                        >= normalizedPassengerCapacity(trip)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All seats are already booked");
                    }
                    Booking b = bookingMapper.toEntity(bookingCreateDto, passenger.getId());
                    bookingRepository.save(b);
                    return b;
                });
        return bookingMapper.toDto(booking);
    }

    public List<BookingDto> getBookings(String username, Long tripId) {
        PoputkaUser user = userRepository.findByUsername(username).orElseThrow();
        TripEntity trip = tripRepository.findById(tripId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        if (user.getId() != trip.getOwnerId()) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        return bookingRepository.findBookingByTripId(tripId).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    public TripBookingOverviewResponseDto tripBookingOverview(String username,
                                                              long tripId,
                                                              String bookingScopeRaw) {
        BookingOverviewScope scope = parseBookingScopeParameter(bookingScopeRaw);
        PoputkaUser user = userRepository.findByUsername(username).orElseThrow();
        TripEntity trip = tripRepository.findById(tripId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (user.getId() != trip.getOwnerId()) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        TripBookingOverviewSummaryDto summary = buildSeatSummaryForTrip(trip);
        Page<Object[]> rawRows = bookingRepository.pageTripBookingOverview(
                tripId, scope, Instant.now().toEpochMilli(), Pageable.unpaged());
        List<TripBookingOverviewItemDto> items = rawRows.getContent().stream()
                .map(this::mapTripBookingOverviewRow)
                .toList();
        Page<TripBookingOverviewItemDto> bookings = new PageImpl<>(items);
        return TripBookingOverviewResponseDto.builder()
                .summary(summary)
                .bookings(bookings)
                .build();
    }

    public List<BookingMessageDto> bookingMessages(String username, String bookingId) {
        PoputkaUser user = userRepository.findByUsername(username).orElseThrow();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        TripEntity trip = tripRepository.findById(booking.getTripId()).orElseThrow();
        if ((booking.getPassengerId() == user.getId()) || (trip.getOwnerId() == user.getId())) {
            return bookingMessageRepository.findByBookingId(bookingId)
                    .stream()
                    .peek(message -> {
                        if (message.getSenderId() != user.getId() && MessageStatus.SENT.equals(message.getMessageStatus())) {
                            message.setMessageStatus(MessageStatus.DELIVERED);
                            bookingMessageRepository.save(message);
                        }
                    })
                    .map(bookingMessage -> BookingMessageDto
                            .builder()
                            .id(bookingMessage.getId())
                            .isMyMessage(Objects.equals(bookingMessage.getSenderId(), user.getId()))
                            .payload(bookingMessage.getPayload())
                            .messageStatus(bookingMessage.getMessageStatus())
                            .modifiedDatetime(bookingMessage.getModified())
                            .build())
                    .toList();
        }
        throw new ResponseStatusException(FORBIDDEN);
    }

    public List<BookingChatDto> getAllBookings(String username) {
        PoputkaUser user = userRepository.findByUsername(username).orElseThrow();
        return bookingRepository.findBookingByUserId(user.getId())
                .stream()
                .map(row -> BookingChatDto
                        .builder()
                        .bookingId((String) row[0])
                        .tripId((long) row[1])
                        .placeFrom((String) row[2])
                        .placeTo((String) row[3])
                        .start(Instant.ofEpochMilli((long) row[4]))
                        .bookingStatus(row[5] != null
                                       ? BookingStatus.valueOf((String) row[5])
                                       : BookingStatus.WAITING)
                        .oppositeUserName((String) row[6])
                        .lastMessagePayload(parseLastMessagePayload(row[7]))
                        .messageStatus(row[8] != null
                                       ? MessageStatus.valueOf((String) row[8])
                                       : MessageStatus.SENT)
                        .lastMessageTime(Instant.ofEpochMilli((long) row[9]))
                        .userRole((String) row[10])
                        .build()).toList();
    }

    public BookingAvailableStatusesResponseDto availableBookingStatuses(String username, String bookingId) {
        PoputkaUser actor = userRepository.findByUsername(username).orElseThrow();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        TripEntity trip = tripRepository.findById(booking.getTripId()).orElseThrow();

        if (!isParticipant(actor, booking, trip)) {
            throw new ResponseStatusException(FORBIDDEN);
        }

        return BookingAvailableStatusesResponseDto.builder()
                .available(computeAvailableTransitions(actor, booking, trip))
                .build();
    }

    @Transactional
    public BookingDto changeBookingStatus(String username, String bookingId, BookingStatusChangeRequestDto request) {
        PoputkaUser actor = userRepository.findByUsername(username).orElseThrow();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        TripEntity trip = tripRepository.findById(booking.getTripId()).orElseThrow();

        if (!isParticipant(actor, booking, trip)) {
            throw new ResponseStatusException(FORBIDDEN);
        }

        BookingStatus from = booking.getBookingStatus();
        BookingStatus to = request.getTo();

        assertTripHasPassengerSeatWhenAccepting(waitingBeforeAccept(from), to, trip);
        assertTransitionAllowed(actor, booking, trip, from, to);

        booking.setBookingStatus(to);
        bookingRepository.save(booking);

        messageService.appendBookingStatusChangedMessage(bookingId, actor.getId(), from, to);
        return bookingMapper.toDto(booking);
    }

    /**
     * System transition: WAITING -> EXPIRED when trip already started.
     * Service message is attributed to trip owner as {@code sender_id} (FK constraint), not as a product rule.
     */
    @Transactional
    public void systemExpireWaitingBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        TripEntity trip = tripRepository.findById(booking.getTripId()).orElseThrow();

        if (booking.getBookingStatus() != BookingStatus.WAITING) {
            return;
        }
        if (trip.getStartTime().isAfter(Instant.now())) {
            return;
        }

        BookingStatus from = booking.getBookingStatus();
        BookingStatus to = BookingStatus.EXPIRED;

        booking.setBookingStatus(to);
        bookingRepository.save(booking);

        messageService.appendBookingStatusChangedMessage(bookingId, trip.getOwnerId(), from, to);
    }

    private boolean isParticipant(PoputkaUser actor, Booking booking, TripEntity trip) {
        return Objects.equals(booking.getPassengerId(), actor.getId())
               || Objects.equals(trip.getOwnerId(), actor.getId());
    }

    private List<BookingStatus> computeAvailableTransitions(PoputkaUser actor, Booking booking, TripEntity trip) {
        BookingStatus from = booking.getBookingStatus();
        List<BookingStatus> candidates = new ArrayList<>();

        switch (from) {
            case WAITING -> {
                if (isOwner(actor, trip)) {
                    candidates.add(BookingStatus.ACCEPTED);
                    candidates.add(BookingStatus.REJECTED);
                }
                candidates.add(BookingStatus.CANCELLED);
            }
            case ACCEPTED -> {
                candidates.add(BookingStatus.CANCELLED);
                if (isPassenger(actor, booking)) {
                    candidates.add(BookingStatus.CHECKED_IN);
                }
                if (isOwner(actor, trip)) {
                    candidates.add(BookingStatus.NO_SHOW);
                }
            }
            case CHECKED_IN -> {
                candidates.add(BookingStatus.CANCELLED);
                if (isOwner(actor, trip)) {
                    candidates.add(BookingStatus.IN_PROGRESS);
                }
            }
            case IN_PROGRESS -> {
                if (isOwner(actor, trip)) {
                    candidates.add(BookingStatus.COMPLETED);
                }
                candidates.add(BookingStatus.CANCELLED);
            }
            default -> {
                // terminal-ish states: no transitions via chat actions
            }
        }

        return candidates.stream()
                .filter(to -> {
                    try {
                        assertTransitionAllowed(actor, booking, trip, from, to);
                        return true;
                    } catch (RuntimeException ex) {
                        return false;
                    }
                })
                .toList();
    }

    private void assertTransitionAllowed(PoputkaUser actor, Booking booking, TripEntity trip, BookingStatus from, BookingStatus to) {
        if (from == to) {
            throw new IllegalStateException("Status is already " + to);
        }

        switch (from) {
            case WAITING -> {
                if (to == BookingStatus.ACCEPTED || to == BookingStatus.REJECTED) {
                    requireOwner(actor, trip);
                } else if (to == BookingStatus.CANCELLED) {
                    // passenger or owner
                } else if (to == BookingStatus.EXPIRED) {
                    // system-only transition (not exposed via normal participant flow)
                    throw new IllegalStateException("EXPIRED is a system transition");
                } else {
                    throw new IllegalStateException("Illegal transition WAITING -> " + to);
                }
            }
            case ACCEPTED -> {
                if (to == BookingStatus.CHECKED_IN) {
                    requirePassenger(actor, booking);
                } else if (to == BookingStatus.NO_SHOW) {
                    requireOwner(actor, trip);
                } else if (to == BookingStatus.CANCELLED) {
                    // passenger or owner
                } else {
                    throw new IllegalStateException("Illegal transition ACCEPTED -> " + to);
                }
            }
            case CHECKED_IN -> {
                if (to == BookingStatus.IN_PROGRESS) {
                    requireOwner(actor, trip);
                } else if (to == BookingStatus.CANCELLED) {
                    // passenger or owner
                } else {
                    throw new IllegalStateException("Illegal transition CHECKED_IN -> " + to);
                }
            }
            case IN_PROGRESS -> {
                if (to == BookingStatus.COMPLETED) {
                    requireOwner(actor, trip);
                } else if (to == BookingStatus.CANCELLED) {
                    // passenger or owner
                } else {
                    throw new IllegalStateException("Illegal transition IN_PROGRESS -> " + to);
                }
            }
            default -> throw new IllegalStateException("Illegal transition " + from + " -> " + to);
        }
    }

    private boolean isOwner(PoputkaUser actor, TripEntity trip) {
        return Objects.equals(trip.getOwnerId(), actor.getId());
    }

    private boolean isPassenger(PoputkaUser actor, Booking booking) {
        return Objects.equals(booking.getPassengerId(), actor.getId());
    }

    private void requireOwner(PoputkaUser actor, TripEntity trip) {
        if (!isOwner(actor, trip)) {
            throw new ResponseStatusException(FORBIDDEN);
        }
    }

    private void requirePassenger(PoputkaUser actor, Booking booking) {
        if (!isPassenger(actor, booking)) {
            throw new ResponseStatusException(FORBIDDEN);
        }
    }

    private BookingOverviewScope parseBookingScopeParameter(String bookingScopeRaw) {
        try {
            return BookingOverviewScope.parseQueryParameter(bookingScopeRaw);
        } catch (IllegalArgumentException ignored) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "bookingScope must be all, active, or archived");
        }
    }

    private TripBookingOverviewSummaryDto buildSeatSummaryForTrip(TripEntity trip) {
        long tripId = trip.getId();
        int occupied = countOccupyingPassengerSeatBookings(tripId);
        int capacity = normalizedPassengerCapacity(trip);
        int freeSeats = Math.max(0, capacity - occupied);
        return TripBookingOverviewSummaryDto.builder()
                .tripId(tripId)
                .passengerSeatCapacity(capacity)
                .occupiedSeats(occupied)
                .freePassengerSeats(freeSeats)
                .full(freeSeats == 0)
                .build();
    }

    private int countOccupyingPassengerSeatBookings(long tripId) {
        return bookingRepository.countByTripIdAndBookingStatusIn(
                tripId, BookingStatus.occupyingPassengerSeatStatuses());
    }

    /** {@code TripEntity.passengers} is modeled as passenger capacity (0–100); stored as signed byte but never negative in valid data. */
    private int normalizedPassengerCapacity(TripEntity trip) {
        return trip.getPassengers() & 0xFF;
    }

    private BookingStatus waitingBeforeAccept(BookingStatus current) {
        return current != null ? current : BookingStatus.WAITING;
    }

    private void assertTripHasPassengerSeatWhenAccepting(BookingStatus fromPreview, BookingStatus to, TripEntity trip) {
        if (fromPreview == BookingStatus.WAITING && to == BookingStatus.ACCEPTED
            && countOccupyingPassengerSeatBookings(trip.getId())
               >= normalizedPassengerCapacity(trip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All seats are already booked");
        }
    }

    private TripBookingOverviewItemDto mapTripBookingOverviewRow(Object[] row) {
        String bookingId = String.valueOf(Objects.requireNonNull(row[0], "booking id"));
        BookingStatus bookingStatus = parseBookingStatusColumn(row[1]);
        String passengerDisplayName = Objects.toString(row[2], "");
        MessagePayload payload = parseLastMessagePayload(row[3]);
        Long lastEpoch = toEpochMillis(row[5]);
        Instant lastTime = lastEpoch != null ? Instant.ofEpochMilli(lastEpoch) : null;
        MessageStatus messageStatus;
        if (payload == null && lastTime == null) {
            messageStatus = null;
        } else {
            messageStatus = parseMessageStatusColumn(row[4]);
        }
        return TripBookingOverviewItemDto.builder()
                .bookingId(bookingId)
                .bookingStatus(bookingStatus)
                .passengerDisplayName(passengerDisplayName)
                .lastMessagePayload(payload)
                .messageStatus(messageStatus)
                .lastMessageTime(lastTime)
                .build();
    }

    private BookingStatus parseBookingStatusColumn(Object raw) {
        String text = Objects.toString(raw, "").trim();
        if (text.isEmpty()) {
            return BookingStatus.WAITING;
        }
        return BookingStatus.valueOf(text);
    }

    private MessageStatus parseMessageStatusColumn(Object raw) {
        if (raw == null) {
            return MessageStatus.SENT;
        }
        String text = String.valueOf(raw).trim();
        if (text.isEmpty()) {
            return MessageStatus.SENT;
        }
        return MessageStatus.valueOf(text);
    }

    private Long toEpochMillis(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Long l) {
            return l;
        }
        if (raw instanceof Integer i) {
            return i.longValue();
        }
        if (raw instanceof BigInteger bi) {
            return bi.longValue();
        }
        if (raw instanceof BigDecimal bd) {
            return bd.longValueExact();
        }
        if (raw instanceof Timestamp ts) {
            return ts.getTime();
        }
        if (raw instanceof Instant i) {
            return i.toEpochMilli();
        }
        throw new IllegalStateException("Unsupported timestamp column: " + raw.getClass());
    }

    private MessagePayload parseLastMessagePayload(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(String.valueOf(raw), MessagePayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid last message payload json", e);
        }
    }
}
