package com.neighbus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String saveFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + "/src/main/resources/static/img/" + subDir + "/";
            String binUploadDir = projectPath + "/bin/main/static/img/" + subDir + "/";

            // 디렉토리 생성
            File srcFolder = new File(uploadDir);
            if (!srcFolder.exists()) {
                srcFolder.mkdirs();
            }
            File binFolder = new File(binUploadDir);
            if (!binFolder.exists()) {
                binFolder.mkdirs();
            }

            // 파일명 생성 (UUID 사용)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;

            // src 폴더에 저장
            File srcDest = new File(uploadDir + savedFilename);
            file.transferTo(srcDest);

            // bin 폴더에 복사
            File binDest = new File(binUploadDir + savedFilename);
            Files.copy(srcDest.toPath(), binDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            logger.info("File saved: {}", savedFilename);
            return savedFilename;
        } catch (Exception e) {
            logger.error("Failed to save file", e);
            return null;
        }
    }
}
