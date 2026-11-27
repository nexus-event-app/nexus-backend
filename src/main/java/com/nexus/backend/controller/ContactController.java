package com.nexus.backend.controller;

import com.nexus.backend.contact.ContactService;
import com.nexus.backend.dto.request.ContactRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<Void> send(@Valid @RequestBody ContactRequest request) {
        contactService.send(request);
        return ResponseEntity.accepted().build();
    }
}
