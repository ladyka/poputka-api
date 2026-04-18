package by.ladyka.poputka.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TripBookingReviewProperties.class)
public class TripBookingReviewConfiguration {
}
