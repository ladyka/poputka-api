package by.ladyka.poputka.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class IndexPageController {

    @GetMapping("/")
    public Map<String, Object> index(Principal principal) {
        return Map.of("status", "OK", "principal", principal != null
                                                   ? principal.getName()
                                                   : "ANON");
    }
}
