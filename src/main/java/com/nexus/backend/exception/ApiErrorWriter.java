package com.nexus.backend.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Escribe el JSON de error del contrato desde componentes que quedan fuera de los controladores (filtros de seguridad). */
@Component
@RequiredArgsConstructor
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                      String error, String message) throws IOException {
        ApiError body = ApiError.of(status, error, message, request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
