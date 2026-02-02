package com.dmytrozah.profitsoft.gateway.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RestController
public class ProfileController {

    @GetMapping("/profile")
    public Mono<Map<String, String>> profile(ServerWebExchange exchange) {
        String rawEmail = exchange.getRequest().getHeaders().getFirst("X-Goog-Authenticated-User-Email");
        String userId = exchange.getRequest().getHeaders().getFirst("X-Goog-Authenticated-User-Id");

        if (rawEmail == null || rawEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        String email = rawEmail.replaceFirst("^accounts\\.google\\.com:", "");

        return Mono.just(Map.of(
                "email", email,
                "sub", userId != null ? userId : ""
        ));
    }
}
