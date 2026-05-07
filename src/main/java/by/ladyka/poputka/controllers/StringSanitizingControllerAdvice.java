package by.ladyka.poputka.controllers;

import by.ladyka.poputka.util.StringSanitizer;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

/**
 * Trims invisible whitespace for all non-JSON inputs (query params, form fields, etc.).
 *
 * <p>JSON request bodies are handled in {@code StringSanitizationConfiguration}.</p>
 */
@ControllerAdvice
public class StringSanitizingControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(StringSanitizer.trimInvisible(text));
            }
        });
    }
}

