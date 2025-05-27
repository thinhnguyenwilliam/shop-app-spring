package com.example.shopapp.components;

import com.example.shopapp.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocalizationUtils {

    private final MessageSource messageSource;

    public LocalizationUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private Locale resolveLocaleFromRequest() {
        HttpServletRequest request = WebUtils.getCurrentRequest();
        if (request == null) {
            return Locale.getDefault(); // fallback
        }

        String lang = request.getHeader("Accept-Language");
        return (lang != null && !lang.isEmpty())
                ? Locale.forLanguageTag(lang)
                : Locale.getDefault();
    }

    public String getLocalizedMessage(String messageKey) {
        Locale locale = resolveLocaleFromRequest();
        return messageSource.getMessage(messageKey, null, locale);
    }

    public String getLocalizedMessage(String messageKey, Object[] args) {
        Locale locale = resolveLocaleFromRequest();
        return messageSource.getMessage(messageKey, args, locale);
    }
}
