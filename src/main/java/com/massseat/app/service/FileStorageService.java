package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.dto.file.FileUploadResponse;
import com.massseat.app.entity.FileUploadTrack;
import com.massseat.app.repository.FileUploadTrackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_SUB_DIR = Set.of(
            "avatars", "properties", "misc"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final S3Client s3Client;
    private final AppProperties.CloudflareR2Properties r2Properties;
    private final FileUploadTrackRepository repository;

    public FileStorageService(
            S3Client s3Client,
            AppProperties appProperties,
            FileUploadTrackRepository repository
    ) {
        this.s3Client = s3Client;
        this.r2Properties = appProperties.getR2Properties();
        this.repository = repository;
    }

    public FileUploadResponse store(MultipartFile file, String folder) {

        // --- validate the file ---
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());

        String fileName = UUID.randomUUID() + extension;

        // --- sub dir identify ---
        String cleanFolder = sanitizeFolder(folder);

        String fileKey = cleanFolder + "/" + fileName;

        try {

            byte[] fileBytes = file.getBytes();

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(r2Properties.getBucketName())
                            .key(fileKey)
                            .contentType(file.getContentType())
//                            .contentLength(file.getSize())
                            .contentLength((long) fileBytes.length)
                            .build();

            s3Client.putObject(
                    request,
//                    RequestBody.fromInputStream(
//                            file.getInputStream(),
//                            file.getSize()
//                    )
                    RequestBody.fromBytes(fileBytes)
            );

//            String url = buildPublicUrl(fileKey);

            // --- tracking file ---
            repository.save(FileUploadTrack.builder()
                    .fileKey(fileKey)
                    .build());

            return FileUploadResponse.builder()
//                    .fileName(fileName)
                    .fileKey(fileKey)
//                    .url(url)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to upload file",
                    e
            );
        }
    }

    @Transactional
    public void delete(String fileKey) {

        if (!StringUtils.hasText(fileKey))
            throw new IllegalArgumentException("File key cannot be empty");
        deleteFileFromR2(fileKey);
        // -- delete track ---
        repository.deleteByFileKey(fileKey);
    }

    @Async
    @Transactional
    public void deleteAll(List<String> fileKeys) {
        deleteAllFileFromR2(fileKeys);
        int deleted = repository.deleteAllByFileKeyIn(fileKeys);
        log.info("Deleted all files from the R2 database & track {}\n-> Total delete: {}", fileKeys,deleted);
    }

    @Async
    public void deleteAllFileFromR2(List<String> fileKeys) {
        fileKeys.forEach(this::deleteFileFromR2);
    }


    @Async
    @Transactional
    public void removeFromTrack(List<String> fileKeys) {
        fileKeys.forEach(repository::deleteByFileKey);
    }

    public ResponseInputStream<GetObjectResponse> getFile(String fileKey) {

        String normalizedKey = fileKey.startsWith("/")
                ? fileKey.substring(1)
                : fileKey;

        log.info(
                "Getting file from R2. Bucket=[{}], Key=[{}]",
                r2Properties.getBucketName(),
                normalizedKey
        );

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(normalizedKey)
                .build();

        return s3Client.getObject(request);
    }

    public void testListObjects() {

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(r2Properties.getBucketName())
                .prefix("usr/")
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        log.info(
                "R2 objects found: {}",
                response.contents().size()
        );

        response.contents().forEach(object ->
                log.info(
                        "R2 OBJECT -> key=[{}], size=[{}]",
                        object.key(),
                        object.size()
                )
        );
    }

    private void deleteFileFromR2(String fileKey) {
        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(r2Properties.getBucketName())
                        .key(fileKey)
                        .build();

        s3Client.deleteObject(request);
    }


    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Maximum file size is 10 MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new IllegalArgumentException(
                    "Only JPEG, PNG, WEBP and GIF images are allowed"
            );
        }
    }


    private String getExtension(String originalFileName) {

        if (!StringUtils.hasText(originalFileName)) {
            return "";
        }

        String extension =
                StringUtils.getFilenameExtension(originalFileName);

        if (!StringUtils.hasText(extension)) {
            return "";
        }

        return "." + extension.toLowerCase();
    }


    private String sanitizeFolder(String folder) {

        if (!StringUtils.hasText(folder)) {
            return "misc";
        }

        String clean = folder
                .trim()
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");

        if (clean.contains("..")) {
            throw new IllegalArgumentException(
                    "Invalid folder"
            );
        }

        return clean.isBlank() || !ALLOWED_SUB_DIR.contains(clean)
                ? "misc"
                : clean;
    }


    private String buildPublicUrl(String fileKey) {

        String baseUrl =
                r2Properties.getPublicUrl();

        if (baseUrl.endsWith("/")) {
            baseUrl =
                    baseUrl.substring(
                            0,
                            baseUrl.length() - 1
                    );
        }

        return baseUrl + "/" + fileKey;
    }


}
