package br.edu.lms.module.communication.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class AnnouncementNotFoundException extends RuntimeException implements HttpMappable {
    public AnnouncementNotFoundException(String announcementId) {
        super("Announcement not found: " + announcementId);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "ANNOUNCEMENT_NOT_FOUND"; }
}
