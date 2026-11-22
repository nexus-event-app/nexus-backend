package com.nexus.backend.dto.request;

import com.nexus.backend.enums.EventMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotBlank(message = "{validation.event.url.required}")
    @Size(max = 500, message = "{validation.url.max500}")
    @Pattern(regexp = "^https://[^\\s/]+\\.[^\\s/]+(/\\S*)?$", message = "{validation.event.url.pattern}")
    private String url;

    @NotBlank(message = "{validation.event.title.required}")
    @Size(min = 5, max = 120, message = "{validation.event.title.size}")
    private String title;

    @NotBlank(message = "{validation.event.description.required}")
    @Size(min = 10, max = 2000, message = "{validation.event.description.size}")
    private String description;

    @NotNull(message = "{validation.event.date.required}")
    private LocalDate date;

    private LocalTime time;

    @NotNull(message = "{validation.event.mode.required}")
    private EventMode mode;

    @Size(max = 80, message = "{validation.event.city.size}")
    private String city;

    @Size(max = 200, message = "{validation.event.address.size}")
    private String address;

    private boolean forWomen;
}
