package br.edu.lms.module.organization.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OrganizationResponse {
    String id;
    String name;
    String description;
    String ownerId;
    LocalDateTime createdAt;
}
