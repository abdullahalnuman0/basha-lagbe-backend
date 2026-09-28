package com.massseat.app.entity;

import com.massseat.app.entity.enums.FileUploadStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "file_uploads_tracking"
)
public class FileUploadTrack extends BaseEntity {

    @Column(nullable = false,unique = true)
    private String fileKey;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileUploadStatus status = FileUploadStatus.TEMPORARY;

}
