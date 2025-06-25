package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.PoputkaTG_RideRequestDeleteDto;
import by.ladyka.poputka.data.dto.PoputkaTG_RideRequestDto;
import by.ladyka.poputka.data.entity.PoputkaTG_Ride;
import by.ladyka.poputka.data.repository.PoputkaTG_RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(PoputkaTGApi.API_POPUTKATG)
@RequiredArgsConstructor
public class PoputkaTGApi {
    public static final String API_POPUTKATG = "/api/poputkatg";
    private final PoputkaTG_RideRepository poputkaTGRideRepository;
    private static final String[] month_names1 = new String[]{"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа",
                                                              "сентября", "октября", "ноября", "декабря"};

    @PostMapping("/ride/update")
    public @ResponseBody Map<String, Object> newTrip(
            @RequestHeader(value = "poputkatg") String poputkatg,
            @RequestBody PoputkaTG_RideRequestDto ride
                                                    ) {
        if (Objects.equals(hash(ride.getToday()), poputkatg)) {
            PoputkaTG_Ride poputkaTGRide = toEntity(ride);
            if (!StringUtils.isEmpty(poputkaTGRide.getUserNickname())) {
                return Map.of("id", poputkaTGRideRepository.save(poputkaTGRide).getRideId());
            } else {
                return Map.of("id", -1);
            }
        } else {
            log.warn("REQUEST CHECK FAIL : %s %s".formatted(ride.getToday(), poputkatg));
            return Map.of("success", false);
        }
    }

    @PostMapping("/ride/delete")
    public @ResponseBody Map<String, Object> newTrip(
            @RequestHeader(value = "poputkatg") String poputkatg,
            @RequestBody PoputkaTG_RideRequestDeleteDto rideForDelete
                                                    ) {
        if (Objects.equals(hash(rideForDelete.getToday()), poputkatg)) {
            poputkaTGRideRepository.deleteById(rideForDelete.getRideId());
            return Map.of("success", true);
        } else {
            log.warn("REQUEST CHECK FAIL : %s %s".formatted(rideForDelete.getToday(), poputkatg));
            return Map.of("success", false);
        }
    }

    private PoputkaTG_Ride toEntity(PoputkaTG_RideRequestDto ride) {
        PoputkaTG_Ride e = poputkaTGRideRepository.findById(ride.getRideId()).orElse(new PoputkaTG_Ride());
        e.setRideId(ride.getRideId());
        e.setFatherRideId(ride.getFatherRideId());
        e.setFromPlaceNow(ride.getFromPlaceNow());
        e.setToPlaceNow(ride.getToPlaceNow());
        e.setDay(ride.getDay());
        e.setMonthName(ride.getMonthName());
        e.setCar(ride.getCar());
        e.setDriverWishes(ride.getDriverWishes());
        e.setTimeStart(ride.getTimeStart());
        e.setUserNickname(ride.getUserNickname());

        //TODO Year is 2025 !!!
        int year = 2025;
        Month m;
        for (int month = 0; month_names1.length > month; month++) {
            if (ride.getMonthName().equals(month_names1[month])) {
                m = Month.of(month + 1);
                LocalDate startRide = LocalDate.of(year, m, ride.getDay());
                e.setStartRide(startRide);
            }
        }
        return e;
    }

    private String hash(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(s.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA ERROR", e);
            return "";
        }
    }
}
