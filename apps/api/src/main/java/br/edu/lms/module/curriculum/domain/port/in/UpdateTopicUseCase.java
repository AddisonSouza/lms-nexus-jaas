package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.application.dto.UpdateTopicCommand;

public interface UpdateTopicUseCase {
    TopicResponse execute(UpdateTopicCommand command);
}
