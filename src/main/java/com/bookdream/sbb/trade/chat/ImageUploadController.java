package com.bookdream.sbb.trade.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
public class ImageUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/api/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("imageFile") MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미지 파일이 없습니다."));
        }

        String originalFileName = imageFile.getOriginalFilename();
        String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        File destFile = new File(Paths.get(uploadDir, savedFileName).toString());

        try {
            // 폴더가 없으면 생성
            destFile.getParentFile().mkdirs();
            // 파일 저장
            imageFile.transferTo(destFile);

            String imageUrl = "/static/image/" + savedFileName;
            logger.info("이미지 업로드 성공: {}", imageUrl);

            // 성공 시 이미지 URL을 JSON 형태로 반환
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            logger.error("이미지 파일 저장 실패", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "이미지 저장에 실패했습니다."));
        }
    }
}