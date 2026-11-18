package com.nexus.backend.dto.response;

import java.time.LocalDate;

/** Un día del calendario con eventos: cuántos hay y si alguno es por y para mujeres (se marca en rosa). */
public record EventDayResponse(LocalDate date, int count, boolean forWomen) {
}
