package com.jcaa.notifiers.infrastructure.adapter.email;

public record BrevoConfig(String apiUrl, String apiKey, String fromAddress, String fromName) {}
