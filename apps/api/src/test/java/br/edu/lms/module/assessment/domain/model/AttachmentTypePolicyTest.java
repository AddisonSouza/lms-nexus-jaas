package br.edu.lms.module.assessment.domain.model;

import br.edu.lms.module.assessment.domain.exception.InvalidAttachmentTypeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentTypePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "resposta.pdf, application/pdf",
            "resposta.doc, application/msword",
            "resposta.docx, application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "resposta.zip, application/zip",
            "resposta.zip, application/x-zip-compressed",
            "foto.jpg, image/jpeg",
            "foto.jpeg, image/jpeg",
            "foto.png, image/png",
    })
    void acceptsTheDocumentsAndImagesAStudentActuallySends(String fileName, String mimeType) {
        assertDoesNotThrow(() -> AttachmentTypePolicy.validate(fileName, mimeType));
    }

    @ParameterizedTest
    @CsvSource({
            "RESPOSTA.PDF, application/pdf",
            "Foto.PnG, IMAGE/PNG",
    })
    void doesNotCareAboutCase(String fileName, String mimeType) {
        assertDoesNotThrow(() -> AttachmentTypePolicy.validate(fileName, mimeType));
    }

    @Test
    void ignoresTheParametersThatRideAlongWithTheMimeType() {
        assertDoesNotThrow(() -> AttachmentTypePolicy.validate("foto.png", "image/png; charset=binary"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"virus.exe", "script.sh", "payload.bat", "arquivo", "arquivo.", ".gitignore"})
    void refusesAnExtensionOutsideTheList(String fileName) {
        assertThrows(InvalidAttachmentTypeException.class,
                () -> AttachmentTypePolicy.validate(fileName, "application/pdf"));
    }

    @Test
    void refusesAnExecutableRenamedToLookLikeADocument() {
        assertThrows(InvalidAttachmentTypeException.class,
                () -> AttachmentTypePolicy.validate("virus.pdf", "application/x-msdownload"));
    }

    @Test
    void refusesAnAllowedMimeTypeUnderTheWrongExtension() {
        // O par precisa bater: PDF declarado num .png não é nenhum dos dois.
        assertThrows(InvalidAttachmentTypeException.class,
                () -> AttachmentTypePolicy.validate("resposta.png", "application/pdf"));
    }

    @ParameterizedTest
    @NullSource
    void refusesAMissingMimeType(String mimeType) {
        assertThrows(InvalidAttachmentTypeException.class,
                () -> AttachmentTypePolicy.validate("resposta.pdf", mimeType));
    }

    @Test
    void refusesAMissingFileName() {
        assertThrows(InvalidAttachmentTypeException.class,
                () -> AttachmentTypePolicy.validate(null, "application/pdf"));
    }

    @Test
    void namesTheRefusedTypeSoTheScreenCanExplainIt() {
        var error = assertThrows(InvalidAttachmentTypeException.class,
                () -> AttachmentTypePolicy.validate("virus.exe", "application/x-msdownload"));

        assertTrue(error.getMessage().contains("application/x-msdownload"));
        assertEquals(422, error.httpStatus());
    }
}
