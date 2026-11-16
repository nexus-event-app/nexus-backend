package com.nexus.backend.dto.response;

/** Datos que se muestran en «Ya casi está» antes de crear la cuenta: el nombre y el email que da Google. */
public record GoogleSignupResponse(String name, String email) {
}
