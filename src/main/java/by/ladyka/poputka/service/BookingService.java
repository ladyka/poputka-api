package by.ladyka.poputka.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                    Booking b = bookingMapper.toEntity(bookingCreateDto, passenger.getId());
                    TripEntity trip = tripRepository.findById(bookingCreateDto.getTripId()).orElseThrow();
                    if (trip.getOwnerId() == passenger.getId()) {
                        throw new RuntimeException("Can't book your own trip");
                    }
                    Integer approved = bookingRepository.countByTripIdAndBookingStatus(trip.getId(), BookingStatus.ACCEPTED);
                    if (approved >= trip.getPassengers()) {
                        throw new RuntimeException("All seats are already booked");
                    }
                    bookingRepository.save(b);
                    return b;
                });
        return bookingMapper.toDto(booking);
    }

    public List<BookingDto> getBookings(String username, Long tripId) {
        PoputkaUser user = userRepository.findByUsername(username).orElseThrow();
        TripEntity trip = tripRepository.findById(tripId).orElseThrow();

        if (user.getId() != trip.getOwnerId()) {
            throw new RuntimeException("Forbidden");
        }

        return bookingRepository.findBookingByTripId(tripId).stream()
                .map(bookingMapper::toDto)
                .toList();
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
        throw new RuntimeException("Forbidden");
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
            throw new RuntimeException("Forbidden");
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
            throw new RuntimeException("Forbidden");
        }

        BookingStatus from = booking.getBookingStatus();
        BookingStatus to = request.getTo();

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
            throw new RuntimeException("Forbidden");
        }
    }

    private void requirePassenger(PoputkaUser actor, Booking booking) {
        if (!isPassenger(actor, booking)) {
            throw new RuntimeException("Forbidden");
        }
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
