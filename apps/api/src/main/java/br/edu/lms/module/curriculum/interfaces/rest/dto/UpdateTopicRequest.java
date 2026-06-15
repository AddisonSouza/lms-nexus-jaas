package br.edu.lms.module.curriculum.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTopicRequest {

    @NotBlank
    @Size(max = 255)
    private String title;
}
