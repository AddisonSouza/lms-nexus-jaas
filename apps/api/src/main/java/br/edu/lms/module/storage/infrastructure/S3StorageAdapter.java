package br.edu.lms.module.storage.infrastructure;

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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class S3StorageAdapter implements StoragePort {

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
                        .build(),
                RequestBody.fromInputStream(content, sizeBytes));

        log.info("Stored file: key={}, size={}", key, sizeBytes);
        return new StoredFile(key, filename, mimeType, sizeBytes);
    }

    @Override
    public InputStream retrieve(String fileKey) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .build());
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
