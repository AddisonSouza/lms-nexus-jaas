package br.edu.lms.module.curriculum.interfaces.rest.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateContentRequest {

    @Size(max = 255)
    private String title;

    private String description;

    private String externalUrl;
}
