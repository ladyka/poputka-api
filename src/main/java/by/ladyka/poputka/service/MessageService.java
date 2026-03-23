package by.ladyka.poputka.service;

import by.ladyka.poputka.data.dto.MessageCreateDto;
import by.ladyka.poputka.data.dto.MessageDto;
import by.ladyka.poputka.data.entity.Booking;
import by.ladyka.poputka.data.entity.BookingMessage;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.enums.MessageStatus;
import by.ladyka.poputka.data.repository.BookingMessageRepository;
import by.ladyka.poputka.data.repository.BookingRepository;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import by.ladyka.poputka.data.repository.TripRepository;
import by.ladyka.poputka.service.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final BookingRepository bookingRepository;
    private final BookingMessageRepository repository;
    private final PoputkaUserRepository poputkaUserRepository;
    private final TripRepository tripRepository;
    private final MessageMapper mapper;

    public MessageDto sendMessage(String username, MessageCreateDto dto) {

        PoputkaUser sender = poputkaUserRepository.findByUsername(username).orElseThrow();
        Booking booking = bookingRepository.findById(dto.getBookingId()).orElseThrow();
        TripEntity trip = tripRepository.findById(booking.getTripId()).orElseThrow();

        if ((booking.getPassengerId() == sender.getId()) || (trip.getOwnerId() == sender.getId())) {

            BookingMessage bookingMessage = mapper.toEntity(dto, sender.getId());
            bookingMessage = repository.save(bookingMessage);
            return mapper.toDto(bookingMessage);
        }

        throw new RuntimeException("Forbidden");
    }

    public void changeMessageStatus(String username, String messageId, MessageStatus status) {
        PoputkaUser currUser = poputkaUserRepository.findByUsername(username).orElseThrow();
        BookingMessage bookingMessage = repository.findById(messageId).orElseThrow();
        Booking booking = bookingRepository.findById(bookingMessage.getBookingId()).orElseThrow();
        TripEntity trip = tripRepository.findById(booking.getTripId()).orElseThrow();

        if (booking.getPassengerId() != currUser.getId() && trip.getOwnerId() != currUser.getId()) {
            throw new RuntimeException("Forbidden");
        }
        if (bookingMessage.getSenderId() != currUser.getId() && bookingMessage.getMessageStatus() != status) {
            bookingMessage.setMessageStatus(status);
            repository.save(bookingMessage);
        }
    }

}
