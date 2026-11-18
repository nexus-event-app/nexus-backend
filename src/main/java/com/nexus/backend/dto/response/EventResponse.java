package com.nexus.backend.dto.response;

import com.nexus.backend.enums.EventMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Evento tal como se muestra. No lleva quién lo compartió (privacidad); {@code mine} solo le dice a esa persona que
 * puede borrarlo. {@code host} es el dominio del enlace, para que se sepa adónde lleva «Apuntarme».
 * {@code interestedCount}: cuántas personas han pulsado «Me apunto» ({@code interested}: si quien mira es una).
 */
public record EventResponse(Long id, String title, String description, String url, String host, LocalDate date,
                            LocalTime time, EventMode mode, String city, String address, boolean forWomen,
                            boolean mine, long interestedCount, boolean interested) {
}
