package br.edu.lms.module.communication.interfaces.rest;

import br.edu.lms.module.communication.application.dto.AttachmentInput;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

final class AnnouncementAttachmentRequestMapper {

    private AnnouncementAttachmentRequestMapper() {
    }

    static List<AttachmentInput> buildAttachments(List<FileUpload> files, String externalUrl, String linkTitle) throws IOException {
        List<AttachmentInput> attachments = new ArrayList<>();
        if (files != null) {
            for (FileUpload file : files) {
                if (file != null && file.filePath() != null) {
                    InputStream stream = Files.newInputStream(file.filePath());
                    attachments.add(new AttachmentInput(stream, file.fileName(), file.contentType(), Files.size(file.filePath()), null, null));
                }
            }
        }
        if (externalUrl != null && !externalUrl.isBlank()) {
            attachments.add(new AttachmentInput(null, null, null, null, externalUrl, linkTitle));
        }
        return attachments;
    }
}
