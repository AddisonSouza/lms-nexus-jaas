package br.edu.lms.module.curriculum.interfaces.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReorderTopicsRequest {

    @NotEmpty
    private List<String> topicIds;
}
