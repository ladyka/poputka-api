package by.ladyka.poputka.controllers;

import by.ladyka.poputka.ApplicationUserDetails;
import by.ladyka.poputka.service.TelegramService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(TelegramController.API_TELEGRAM)
@RequiredArgsConstructor
public class TelegramController {

    public static final String API_TELEGRAM = "/api/telegram";
    private final TelegramService telegramService;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody String telegram(
            long id,
            String first_name,
            String last_name,
            String username,
            String photo_url,
            String auth_date,
            String hash,
            ApplicationUserDetails applicationUserDetails
                                        ) {
        telegramService.telegram(id,
                first_name,
                last_name,
                username,
                photo_url,
                auth_date,
                hash,
                applicationUserDetails);
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta http-equiv=\"refresh\" content=\"0;url=/profile\">" +
                "</head>" +
                "<body>" +
                "Hello, " + currentAuth.getName() + "! Your authorities are: " + currentAuth.getAuthorities() +
                "</body>" +
                "</html>";
    }

}
