package com.massseat.app.repository;

import com.massseat.app.entity.enums.FileUploadStatus;
import com.massseat.app.entity.FileUploadTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface FileUploadTrackRepository extends JpaRepository<FileUploadTrack, Long> {

    @Query("""
            SELECT f
            FROM FileUploadTrack f
            WHERE f.status = :status
            AND f.createdAt < :cutoff
            """)
    List<FileUploadTrack> findExpiredTemporaryFiles(
            @Param("status") FileUploadStatus status,
            @Param("cutoff") Instant cutoff
    );

    void deleteByFileKey(String fileKey);

    @Modifying
    @Query("DELETE FROM FileUploadTrack f WHERE f.fileKey IN :fileKeys")
    int deleteAllByFileKeyIn(@Param("fileKeys") List<String> fileKeys);
}
