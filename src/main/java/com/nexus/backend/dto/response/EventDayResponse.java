package com.nexus.backend.dto.response;

import java.time.LocalDate;

public record EventDayResponse(LocalDate date, int count, boolean forWomen) {
}
