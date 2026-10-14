package com.jcaa.notifiers.application.service.dto.command;

public record SendNotificationCommand(String email, String name, String subject, String body) {}
