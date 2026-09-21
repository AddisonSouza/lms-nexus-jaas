package br.edu.lms.module.storage.infrastructure;

import br.edu.lms.module.storage.domain.exception.FileNotFoundException;
import br.edu.lms.module.storage.domain.model.RetrievedFile;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.model.StoredFile;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class S3StorageAdapter implements StoragePort {

    private static final int UUID_LENGTH = 36;
    private static final String ORIGINAL_NAME_METADATA = "original-name";

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    S3StorageAdapter(
            S3Client s3Client,
            @ConfigProperty(name = "storage.bucket", defaultValue = "lms-dev") String bucket,
            @ConfigProperty(name = "storage.public-base-url", defaultValue = "http://localhost:9000") String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public StoredFile store(InputStream content, String filename, String mimeType, long sizeBytes, StorageContext context) {
        LocalDate now = LocalDate.now();
        String key = "%s/%d/%02d/%s-%s".formatted(
                context.name().toLowerCase(),
                now.getYear(),
                now.getMonthValue(),
                UUID.randomUUID(),
                sanitize(filename));

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(mimeType)
                        .contentLength(sizeBytes)
                        // A chave leva o nome sanitizado; o original fica aqui para o
                        // `Content-Disposition` devolver acento e espaço como vieram.
                        .metadata(Map.of(ORIGINAL_NAME_METADATA, filename))
                        .build(),
                RequestBody.fromInputStream(content, sizeBytes));

        log.info("Stored file: key={}, size={}", key, sizeBytes);
        return new StoredFile(key, filename, mimeType, sizeBytes);
    }

    @Override
    public RetrievedFile retrieve(String fileKey) {
        try {
            var response = s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileKey)
                            .build());
            var head = response.response();
            return new RetrievedFile(
                    new StoredFile(fileKey, originalNameOf(head, fileKey), head.contentType(), head.contentLength()),
                    response);
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException(fileKey);
        }
    }

    /**
     * O nome original vem do metadado gravado no upload. Objetos anteriores a
     * este campo caem no nome embutido na chave, já sanitizado.
     */
    private String originalNameOf(software.amazon.awssdk.services.s3.model.GetObjectResponse head, String fileKey) {
        var stored = head.metadata() != null ? head.metadata().get(ORIGINAL_NAME_METADATA) : null;
        return stored != null && !stored.isBlank() ? stored : originalNameFrom(fileKey);
    }

    /** A chave é `{contexto}/{ano}/{mês}/{uuid}-{nome}`; devolve a cauda sem o UUID. */
    private String originalNameFrom(String fileKey) {
        var tail = fileKey.substring(fileKey.lastIndexOf('/') + 1);
        var separator = tail.indexOf('-', UUID_LENGTH - 1);
        return separator >= 0 && separator < tail.length() - 1
                ? tail.substring(separator + 1)
                : tail;
    }

    @Override
    public void delete(String fileKey) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build());
        log.info("Deleted file: key={}", fileKey);
    }

    @Override
    public String getPublicUrl(String fileKey) {
        return "%s/%s/%s".formatted(publicBaseUrl, bucket, fileKey);
    }

    private String sanitize(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
