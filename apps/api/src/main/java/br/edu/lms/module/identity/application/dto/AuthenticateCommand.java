package br.edu.lms.module.identity.application.dto;

public record AuthenticateCommand(String email, String rawPassword) {}
