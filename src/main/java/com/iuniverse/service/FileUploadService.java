package com.iuniverse.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.iuniverse.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile multipartFile) {
        try {
            Map uploadResult = cloudinary.uploader()
                    .upload(multipartFile.getBytes(),
                            ObjectUtils.asMap("resource_type", "auto"));

            return uploadResult.get("url").toString();
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw new FileUploadException("Cannot upload file onto Cloud: " + e.getMessage());
        }
    }
}