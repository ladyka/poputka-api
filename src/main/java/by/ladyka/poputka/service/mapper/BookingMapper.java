package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.BookingCreateDto;
import by.ladyka.poputka.data.dto.BookingDto;
import by.ladyka.poputka.data.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "bookingStatus", expression = "java(by.ladyka.poputka.data.enums.BookingStatus.WAITING)")
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "modifiedDatetime", ignore = true)
    @Mapping(target = "modifiedUser", ignore = true)
    @Mapping(target = "modified", ignore = true)
    @Mapping(target = "version", ignore = true)
    Booking toEntity(BookingCreateDto dto, Long passengerId);

    BookingDto toDto(Booking booking);

}
