package by.ladyka.poputka.controllers;

import by.ladyka.poputka.data.dto.PoputkaTG_RideRequestDto;
import by.ladyka.poputka.data.entity.PoputkaTG_Ride;
import by.ladyka.poputka.data.repository.PoputkaTG_RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/poputkatg/")
@RequiredArgsConstructor
public class PoputkaTGApi {
    private final PoputkaTG_RideRepository poputkaTGRideRepository;

    @PostMapping("/ride/update")
    public @ResponseBody Map<String, Object> newTrip(
            @RequestHeader(value = "poputkatg") String poputkatg,
            @RequestBody PoputkaTG_RideRequestDto ride
                                                    ) {
        if (Objects.equals(hash(ride.getToday()), poputkatg)) {
            return Map.of("id", poputkaTGRideRepository.save(toEntity(ride)).getRideId());
        } else {
            log.warn("REQUEST CHECK FAIL : " + ride.getToday() + " " + poputkatg);
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
        e.setPrice(ride.getPrice());
        e.setValutaShort(ride.getValutaShort());
        e.setTimeStart(ride.getTimeStart());
        e.setUserNickname(ride.getUserNickname());
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
