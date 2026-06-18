package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class GestorDashboardResponse {
    List<ClassroomHealthResponse> classrooms;
}
