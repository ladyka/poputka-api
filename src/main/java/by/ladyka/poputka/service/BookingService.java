package by.ladyka.poputka.service;

import by.ladyka.poputka.data.dto.BookingChatDto;
import by.ladyka.poputka.data.dto.BookingCreateDto;
import by.ladyka.poputka.data.dto.BookingDto;
import by.ladyka.poputka.data.dto.BookingMessageDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
                            .content(bookingMessage.getContent())
                            .messageStatus(bookingMessage.getMessageStatus())
                            .modifiedDatetime(Instant.ofEpochSecond(bookingMessage.getModifiedDatetime()))
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
                        .start(Instant.ofEpochSecond((long) row[4]))
                        .bookingStatus(row[5] != null
                                       ? BookingStatus.valueOf((String) row[5])
                                       : BookingStatus.WAITING)
                        .oppositeUserName((String) row[6])
                        .content(String.valueOf(row[7]))
                        .messageStatus(row[8] != null
                                       ? MessageStatus.valueOf((String) row[8])
                                       : MessageStatus.SENT)
                        .lastMessageTime(Instant.ofEpochSecond((long) row[9]))
                        .userRole((String) row[10])
                        .build()).toList();
    }
}
