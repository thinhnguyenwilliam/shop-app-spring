package com.example.shopapp.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class WebUtils {

    // Private constructor to prevent instantiation
    private WebUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
