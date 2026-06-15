package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.SubjectContentResponse;
import br.edu.lms.module.curriculum.application.dto.UpdateContentCommand;

public interface UpdateContentUseCase {
    SubjectContentResponse execute(UpdateContentCommand command);
}
