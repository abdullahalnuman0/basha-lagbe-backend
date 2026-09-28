package com.massseat.app.scheduler;

import com.massseat.app.entity.FileUploadTrack;
import com.massseat.app.entity.enums.FileUploadStatus;
import com.massseat.app.repository.FileUploadTrackRepository;
import com.massseat.app.service.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class FileExpireScheduler {

    private final FileUploadTrackRepository repository;
    private final FileStorageService fileStorageService;

    @Transactional
//    @Scheduled(fixedRate = 60 * 60 * 1000)
//    @Scheduled(fixedRate = 120000)
    @Scheduled(fixedRate = 300000)
    public void cleanupTemporaryFiles() {

        log.info("Cleaning up temporary files");

//        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant cutoff = Instant.now().minus(2, ChronoUnit.MINUTES);

        List<FileUploadTrack> files =
                repository.findExpiredTemporaryFiles(FileUploadStatus.TEMPORARY, cutoff);

        log.info("Total files expired: {}", files.size());

        for (FileUploadTrack file : files) {

            try {
                fileStorageService.delete(file.getFileKey());
                repository.delete(file);

            } catch (Exception e) {
                log.error("Failed to cleanup file: {}", file.getFileKey(), e);
            }
        }
    }
}
