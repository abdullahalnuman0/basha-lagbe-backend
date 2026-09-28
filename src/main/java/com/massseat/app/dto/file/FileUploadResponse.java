package com.massseat.app.dto.file;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {
//    String fileName;
//    String url;
    String contentType;
    long size;
    String fileKey;
}
