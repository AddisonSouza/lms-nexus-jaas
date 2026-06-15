package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.CreateTopicCommand;
import br.edu.lms.module.curriculum.application.dto.TopicResponse;

public interface CreateTopicUseCase {
    TopicResponse execute(CreateTopicCommand command);
}
