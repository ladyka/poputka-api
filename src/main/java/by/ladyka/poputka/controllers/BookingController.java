package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.BookingChatDto;
import by.ladyka.poputka.data.dto.BookingCreateDto;
import by.ladyka.poputka.data.dto.BookingDto;
import by.ladyka.poputka.data.dto.BookingMessageDto;
import by.ladyka.poputka.data.dto.MessageCreateDto;
import by.ladyka.poputka.data.dto.MessageDto;
import by.ladyka.poputka.data.enums.MessageStatus;
import by.ladyka.poputka.service.BookingService;
import by.ladyka.poputka.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final MessageService messageService;

    @PutMapping
    public BookingDto createBooking(Principal principal, @RequestBody @Valid BookingCreateDto bookingCreateDto) {
        BookingDto dto = bookingService.createBooking(principal.getName(), bookingCreateDto);
        String message = bookingCreateDto.getMessage();
        if (message == null || message.isBlank()) {
            message = "Здравствуйте, хочу забронировать поездку с Вами!";
        }
        messageService.sendMessage(principal.getName(),
                MessageCreateDto.builder()
                        .bookingId(dto.getId())
                        .content(message)
                        .build());
        return dto;
    }

    @GetMapping({"/"})
    public List<BookingChatDto> getAllBookings(Principal principal) {
        return bookingService.getAllBookings(principal.getName());
    }

    @GetMapping({"/{tripId}"})
    public List<BookingDto> getBookings(Principal principal, @PathVariable Long tripId) {
        return bookingService.getBookings(principal.getName(), tripId);
    }

    @GetMapping({"/messages/{bookingId}"})
    public List<BookingMessageDto> getMessages(Principal principal, @PathVariable String bookingId) {
        return bookingService.bookingMessages(principal.getName(), bookingId);
    }

    @PutMapping({"/messages"})
    public MessageDto sendMessage(Principal principal, @RequestBody MessageCreateDto messageCreateDto) {
        if (messageCreateDto.getContent() != null && !messageCreateDto.getContent().isBlank()) {
            return messageService.sendMessage(principal.getName(),
                    MessageCreateDto.builder()
                            .bookingId(messageCreateDto.getBookingId())
                            .content(messageCreateDto.getContent())
                            .build());
        }
        throw new RuntimeException("Forbidden");
    }

    @PostMapping({"/messages/{messageId}/read"})
    public void markMessageAsRead(Principal principal, @PathVariable String messageId) {
        messageService.changeMessageStatus(principal.getName(), messageId, MessageStatus.READ);
    }
}
