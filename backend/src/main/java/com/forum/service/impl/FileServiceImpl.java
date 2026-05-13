package com.forum.service.impl;

import com.forum.common.ResultCode;
import com.forum.exception.BusinessException;
import com.forum.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private final Path baseDir;

    public FileServiceImpl(@Value("${file.upload-path:./uploads}") String uploadPath) {
        this.baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    @Override
    public String uploadImage(MultipartFile file, String scene) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 jpg/png/gif/webp 格式");
        }

        String ext = getExtension(file.getOriginalFilename());
        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = dateDir + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "." + ext;

        String sceneDir = (scene == null || scene.isBlank()) ? "image" : scene;
        Path dir = baseDir.resolve(sceneDir);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target);
        } catch (IOException e) {
            log.error("文件保存失败: dir={}, fileName={}", dir, fileName, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        return "/uploads/" + sceneDir + "/" + fileName;
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }
}
