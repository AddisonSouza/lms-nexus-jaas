package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.ReorderTopicsCommand;
import br.edu.lms.module.curriculum.application.dto.TopicResponse;

import java.util.List;

public interface ReorderTopicsUseCase {
    List<TopicResponse> execute(ReorderTopicsCommand command);
}
