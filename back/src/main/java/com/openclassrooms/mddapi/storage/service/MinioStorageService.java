package com.openclassrooms.mddapi.storage.service;

import com.openclassrooms.mddapi.storage.exception.StorageException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minIoClient;

    @Value("${app.minio.bucket-name}")
    private String bucketName;

    /**
     * Uploads a file to the MinIO object storage.
     *
     * <p>Generates a unique filename, uploads the file to the configured MinIO
     * bucket, and returns the generated filename for future references.</p>
     *
     * @param file               the file to upload
     * @param resourceType       the type of resource the file belongs to, such as
     *                           {@code article} or {@code user}
     * @param resourceIdentifier the identifier of the resource, such as an
     *                           article slug or a username
     * @return the generated filename of the uploaded file
     * @throws StorageException if the file upload fails
     */
    @Override
    public String upload(MultipartFile file, String resourceType, String resourceIdentifier) {
        String filename = formatStoragePath(resourceType, resourceIdentifier, file.getOriginalFilename());

        try {
            minIoClient.putObject(
                    PutObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return filename;

        } catch (MinioException e) {
            log.error("## Error/MinIoStorageService ## Unable to send a file", e.getMessage());
            throw new StorageException("Unable to send a file");
        } catch (Exception e) {
            log.error("## Error/MinIoStorageService ## Unable to send a file", e.getMessage());
            throw new StorageException("Unable to send a file");
        }
    }

    /**
     * Checks whether a file exists in the MinIO storage.
     *
     * <p>Attempts to retrieve metadata for the specified object. If the object
     * exists, the method returns {@code true}; otherwise, it returns {@code false}.</p>
     *
     * @param resourceType       the type of resource the file belongs to, such as
     *                           {@code article} or {@code user}
     * @param resourceIdentifier the identifier of the resource, such as an
     *                           article slug or a username
     * @param originalFileName   the original name of the file
     * @return {@code true} if the file exists, otherwise {@code false}
     */
    @Override
    public boolean exists(final String resourceType, final String resourceIdentifier, final String originalFileName) {
        String filename = formatStoragePath(resourceType, resourceIdentifier, originalFileName);

        try {
            minIoClient.statObject(
                    StatObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());
            return true;
        } catch (MinioException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes a file from the MinIO object storage.
     *
     * <p>Removes the specified object from the configured MinIO bucket.</p>
     *
     * @param filename the name of the file to delete
     * @throws StorageException if the file deletion fails
     */
    @Override
    public void delete(String filename) {

        try {
            minIoClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build());

        } catch (MinioException e) {
            log.error("## Error/MinIoStorageService ## Unable to send a file", e.getMessage());
            throw new StorageException("Unable to send a file");
        } catch (Exception e) {
            log.error("## Error/MinIoStorageService ## Unable to remove file", e.getMessage());
            throw new StorageException("Unable to delete file " + filename);
        }
    }


    /**
     * Formats the storage path for a file associated with a resource.
     *
     * <p>{@code {fileType}/{resourceIdentifier}/{uuid}-{originalFilename}}</p>
     *
     * <p>and an original filename {@code image.jpg}, the resulting path could be:
     * {@code article/my-first-article/550e8400-e29b-41d4-a716-446655440000-image.jpg}</p>
     *
     * @param resourceType       the type of resource the file belongs to, such as
     *                           {@code article} or {@code user}
     * @param resourceIdentifier the identifier of the resource, such as an
     *                           article slug or a username
     * @param originalFileName   the original name of the file
     * @return the formatted storage path containing a unique UUID-based filename
     */
    private String formatStoragePath(String resourceType, String resourceIdentifier, String originalFileName) {
        return String.format(
                "%s/%s/%s-%s",
                resourceType,
                resourceIdentifier,
                UUID.randomUUID(),
                originalFileName
        );
    }
}
