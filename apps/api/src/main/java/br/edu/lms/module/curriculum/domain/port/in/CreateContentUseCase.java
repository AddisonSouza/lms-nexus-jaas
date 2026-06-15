package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.CreateContentCommand;
import br.edu.lms.module.curriculum.application.dto.SubjectContentResponse;

public interface CreateContentUseCase {
    SubjectContentResponse execute(CreateContentCommand command);
}
