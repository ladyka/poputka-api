package by.ladyka.poputka.service.mapper;

import by.ladyka.poputka.data.dto.OSMPlaceDto;
import by.ladyka.poputka.data.dto.TripCreateRequestDto;
import by.ladyka.poputka.data.dto.TripDto;
import by.ladyka.poputka.data.dto.TripUpdateRequestDto;
import by.ladyka.poputka.data.entity.PoputkaUser;
import by.ladyka.poputka.data.entity.TripEntity;
import by.ladyka.poputka.data.repository.PoputkaUserRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class TripMapper {

    @Autowired
    protected PoputkaUserRepository poputkaUserRepository;

    @Mapping(target = "from", expression = "java(toPlaceFromDto(entity))")
    @Mapping(target = "to", expression = "java(toPlaceToDto(entity))")
    @Mapping(target = "startEpochMillis", source = "start")
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "ownerTelegramUsername", ignore = true)
    public abstract TripDto toDto(TripEntity entity);

    @AfterMapping
    protected void fillOwnerFields(TripEntity entity, @MappingTarget TripDto dto) {
        PoputkaUser owner = poputkaUserRepository.findById(entity.getOwnerId()).orElseThrow();
        dto.setCar(owner.getCar());
        dto.setOwner(owner.getName());
        dto.setOwnerTelegramUsername(owner.getTelegramUsername());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "start", source = "startEpochMillis")
    @Mapping(target = "startTime", ignore = true) // derived getter/setter, keep out of MapStruct mapping
    @Mapping(target = "passengers", expression = "java((byte) dto.getPassengers())")
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "modifiedUser", ignore = true)
    @Mapping(target = "modifiedDatetime", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "placeFrom", source = "from.name")
    @Mapping(target = "placeFromCity", source = "from.city")
    @Mapping(target = "placeFromDisplayName", source = "from.displayName")
    @Mapping(target = "placeFromOsmId", source = "from.osm_id")
    @Mapping(target = "placeFromOsmType", source = "from.osm_type")
    @Mapping(target = "placeFromLat", source = "from.lat")
    @Mapping(target = "placeFromLon", source = "from.lon")
    @Mapping(target = "placeTo", source = "to.name")
    @Mapping(target = "placeToCity", source = "to.city")
    @Mapping(target = "placeToDisplayName", source = "to.displayName")
    @Mapping(target = "placeToOsmId", source = "to.osm_id")
    @Mapping(target = "placeToOsmType", source = "to.osm_type")
    @Mapping(target = "placeToLat", source = "to.lat")
    @Mapping(target = "placeToLon", source = "to.lon")
    public abstract void toEntity(TripCreateRequestDto dto, @MappingTarget TripEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "start", source = "startEpochMillis")
    @Mapping(target = "startTime", ignore = true) // derived getter/setter, keep out of MapStruct mapping
    @Mapping(target = "passengers", expression = "java((byte) dto.getPassengers())")
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "createdDatetime", ignore = true)
    @Mapping(target = "modifiedUser", ignore = true)
    @Mapping(target = "modifiedDatetime", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "placeFrom", source = "from.name")
    @Mapping(target = "placeFromCity", source = "from.city")
    @Mapping(target = "placeFromDisplayName", source = "from.displayName")
    @Mapping(target = "placeFromOsmId", source = "from.osm_id")
    @Mapping(target = "placeFromOsmType", source = "from.osm_type")
    @Mapping(target = "placeFromLat", source = "from.lat")
    @Mapping(target = "placeFromLon", source = "from.lon")
    @Mapping(target = "placeTo", source = "to.name")
    @Mapping(target = "placeToCity", source = "to.city")
    @Mapping(target = "placeToDisplayName", source = "to.displayName")
    @Mapping(target = "placeToOsmId", source = "to.osm_id")
    @Mapping(target = "placeToOsmType", source = "to.osm_type")
    @Mapping(target = "placeToLat", source = "to.lat")
    @Mapping(target = "placeToLon", source = "to.lon")
    public abstract TripEntity toEntity(TripUpdateRequestDto dto, @MappingTarget TripEntity entity);

    @Mapping(target = "name", source = "placeFrom")
    @Mapping(target = "city", source = "placeFromCity")
    @Mapping(target = "displayName", source = "placeFromDisplayName")
    @Mapping(target = "osm_id", source = "placeFromOsmId")
    @Mapping(target = "osm_type", source = "placeFromOsmType")
    @Mapping(target = "lat", source = "placeFromLat")
    @Mapping(target = "lon", source = "placeFromLon")
    protected abstract OSMPlaceDto toPlaceFromDto(TripEntity entity);

    @Mapping(target = "name", source = "placeTo")
    @Mapping(target = "city", source = "placeToCity")
    @Mapping(target = "displayName", source = "placeToDisplayName")
    @Mapping(target = "osm_id", source = "placeToOsmId")
    @Mapping(target = "osm_type", source = "placeToOsmType")
    @Mapping(target = "lat", source = "placeToLat")
    @Mapping(target = "lon", source = "placeToLon")
    protected abstract OSMPlaceDto toPlaceToDto(TripEntity entity);
}
