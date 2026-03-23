package by.ladyka.poputka.controllers;

import by.ladyka.poputka.ApplicationUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IndexPageController {

    @GetMapping("/")
    public Map<String, Object> index(@AuthenticationPrincipal ApplicationUserDetails principal) {
        return Map.of("status", "OK", "principal", principal.getUsername());
    }
}
