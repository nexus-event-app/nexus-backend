package com.nexus.backend.dto.request;

import com.nexus.backend.enums.EventMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/** Lo mínimo para compartir un evento: enlace para apuntarse, título, fecha, dónde y una descripción corta. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "{validation.event.url.required}")
    @Size(max = 500, message = "{validation.url.max500}")
    @Pattern(regexp = "^https://[^\\s/]+\\.[^\\s/]+(/\\S*)?$", message = "{validation.event.url.pattern}")
    private String url;

    @NotBlank(message = "{validation.event.title.required}")
    @Size(min = 5, max = 120, message = "{validation.event.title.size}")
    private String title;

    @NotBlank(message = "{validation.event.description.required}")
    @Size(min = 10, max = 280, message = "{validation.event.description.size}")
    private String description;

    @NotNull(message = "{validation.event.date.required}")
    @FutureOrPresent(message = "{validation.event.date.future}")
    private LocalDate date;

    private LocalTime time;

    @NotNull(message = "{validation.event.mode.required}")
    private EventMode mode;

    @Size(max = 80, message = "{validation.event.city.size}")
    private String city;

    private boolean forWomen;
}
