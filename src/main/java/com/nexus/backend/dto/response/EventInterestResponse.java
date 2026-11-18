package com.nexus.backend.dto.response;

/** Tras pulsar «Me apunto»: cuántas personas se apuntan y si quien lo pulsó está entre ellas. */
public record EventInterestResponse(long count, boolean interested) {
}
