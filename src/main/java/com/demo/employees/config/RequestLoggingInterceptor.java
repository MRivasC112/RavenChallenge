package com.demo.employees.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class RequestLoggingInterceptor  implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public boolean preHandle(HttpServletRequest request , HttpServletResponse response,
                             Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        String headers = Collections.list(request.getHeaderNames()).stream()
                .filter(headerName -> !AUTHORIZATION_HEADER.equalsIgnoreCase(headerName))
                .map(headerName -> headerName + ": " + request.getHeader(headerName))
                .collect(Collectors.joining(", "));

        logger.info("Incoming Request: {} {} | Headers: [{}]", method, uri, headers);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {

        int status = response.getStatus();

        Collection<String> headerNames = response.getHeaderNames();
        String headers = headerNames.stream()
                .distinct()
                .map(headerName -> headerName + ": " + response.getHeader(headerName))
                .collect(Collectors.joining(", "));

        logger.info("Outgoing Response: {} | Headers: [{}]", status, headers);
    }
}
