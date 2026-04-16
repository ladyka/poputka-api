package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.MessageCreateDto;
import by.ladyka.poputka.data.dto.MessageDto;
import by.ladyka.poputka.data.entity.BookingMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MessagePayloadResolver.class})
public interface MessageMapper {

    MessageDto toDto(BookingMessage bookingMessage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senderId", expression = "java(senderId)")
    @Mapping(target = "messageStatus", expression = "java(by.ladyka.poputka.data.enums.MessageStatus.SENT)")
    @Mapping(target = "payload", source = "messageCreateDto", qualifiedByName = "messagePayloadFromCreateDto")
    BookingMessage toEntity(MessageCreateDto messageCreateDto, Long senderId);
}
