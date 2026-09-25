package br.edu.lms.module.storage.application.usecase;

import br.edu.lms.module.storage.domain.exception.FileNotFoundException;
import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.RetrievedFile;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.FileAccessPort;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServeFileUseCaseTest {

    static final String TASK_KEY = "task_attachment/2026/09/uuid-prova.pdf";
    static final FileRequester REQUESTER = new FileRequester("user-1", "org-1", "ALUNO");

    @Mock StoragePort storagePort;
    @Mock Instance<FileAccessPort> accessPorts;
    @Mock FileAccessPort taskAccess;

    ServeFileUseCase sut;

    @BeforeEach
    void setUp() {
        sut = new ServeFileUseCase(storagePort, accessPorts);
    }

    @Test
    void servesTheFile_whenTheOwningModuleAllows() {
        var file = mock(RetrievedFile.class);
        when(accessPorts.stream()).thenReturn(Stream.of(taskAccess));
        when(taskAccess.context()).thenReturn(StorageContext.TASK_ATTACHMENT);
        when(taskAccess.canRead(TASK_KEY, REQUESTER)).thenReturn(true);
        when(storagePort.retrieve(TASK_KEY)).thenReturn(file);

        assertThat(sut.execute(TASK_KEY, REQUESTER)).isSameAs(file);
    }

    @Test
    void returnsNotFound_whenTheOwningModuleDenies() {
        when(accessPorts.stream()).thenReturn(Stream.of(taskAccess));
        when(taskAccess.context()).thenReturn(StorageContext.TASK_ATTACHMENT);
        when(taskAccess.canRead(TASK_KEY, REQUESTER)).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(TASK_KEY, REQUESTER)).isInstanceOf(FileNotFoundException.class);
        verifyNoInteractions(storagePort);
    }

    @Test
    void returnsNotFound_whenNoModuleHandlesTheContext() {
        when(accessPorts.stream()).thenReturn(Stream.of(taskAccess));
        when(taskAccess.context()).thenReturn(StorageContext.TASK_ATTACHMENT);

        assertThatThrownBy(() -> sut.execute("lesson_material/2026/09/uuid-aula.pdf", REQUESTER))
                .isInstanceOf(FileNotFoundException.class);
        verifyNoInteractions(storagePort);
    }

    @Test
    void returnsNotFound_whenTheKeyHasNoKnownContext() {
        assertThatThrownBy(() -> sut.execute("outro/2026/09/uuid-x.pdf", REQUESTER))
                .isInstanceOf(FileNotFoundException.class);
        verifyNoInteractions(storagePort);
    }

    @Test
    void returnsNotFound_whenTheRequesterHasNoOrganization() {
        var withoutOrg = new FileRequester("user-1", null, null);

        assertThatThrownBy(() -> sut.execute(TASK_KEY, withoutOrg)).isInstanceOf(FileNotFoundException.class);
        verifyNoInteractions(storagePort, accessPorts);
    }
}
