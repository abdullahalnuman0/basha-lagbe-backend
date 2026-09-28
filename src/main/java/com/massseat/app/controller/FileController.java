package com.massseat.app.controller;


import com.massseat.app.dto.file.FileUploadResponse;
import com.massseat.app.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@Tag(
        name = "03. Files",
        description = "Image upload / delete / get; returns a URL usable in other requests"
)
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "misc") String folder
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileStorageService.store(file, folder));
    }


    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam("fileKey") String fileKey
    ) {

        fileStorageService.delete(fileKey);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteAll(
            @RequestBody List<String> fileKeys
    ) {
        fileStorageService.deleteAll(fileKeys);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{*fileKey}")
    public ResponseEntity<StreamingResponseBody> getFile(@PathVariable String fileKey) {

        ResponseInputStream<GetObjectResponse> inputStream =
                fileStorageService.getFile(fileKey);

        GetObjectResponse response = inputStream.response();

        StreamingResponseBody body = outputStream -> {
            try (inputStream) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .contentLength(response.contentLength())
                .body(body);
    }

    @GetMapping("/test/list")
    public ResponseEntity<String> testList() {

        fileStorageService.testListObjects();

        return ResponseEntity.ok("Check logs");
    }

}
