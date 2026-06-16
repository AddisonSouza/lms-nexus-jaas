package br.edu.lms.module.communication.domain.port.in;

import br.edu.lms.module.communication.application.dto.AnnouncementResponse;
import br.edu.lms.module.communication.application.dto.EditAnnouncementCommand;

public interface EditAnnouncementUseCase {
    AnnouncementResponse execute(EditAnnouncementCommand command);
}
