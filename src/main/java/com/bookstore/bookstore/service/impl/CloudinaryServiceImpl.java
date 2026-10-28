package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private boolean isRealCloudinaryConfigured() {
        String apiKey = cloudinary.config.apiKey;
        String cloudName = cloudinary.config.cloudName;
        return apiKey != null && !apiKey.isBlank()
                && !apiKey.startsWith("your_")
                && !apiKey.equals("local-mode")
                && !apiKey.equals("1234567890")
                && !apiKey.equals("123456789012345")
                && cloudName != null && !cloudName.startsWith("your_");
    }

    private String saveFileLocally(MultipartFile file, String folder) {
        try {
            String safeFolder = (folder != null && !folder.isBlank()) ? folder.replaceAll("[^a-zA-Z0-9_-]", "") : "general";
            Path dir = Paths.get("uploads", safeFolder).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String ext = ".jpg";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf(".")).replaceAll("[^a-zA-Z0-9.]", "");
            }
            String filename = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String localUrl = "/uploads/" + safeFolder + "/" + filename;
            log.info("Đã lưu file thành công tại: {}", localUrl);
            return localUrl;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file tải lên: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được để trống!");
        }

        if (isRealCloudinaryConfigured()) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", "bookstore/" + folder,
                        "resource_type", "image"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                log.info("Tải ảnh lên Cloudinary thành công: {}", secureUrl);
                return secureUrl;
            } catch (Exception e) {
                log.warn("Cloudinary không khả dụng ({}), tự động chuyển sang lưu trữ cục bộ.", e.getMessage());
            }
        }
        return saveFileLocally(file, folder);
    }

    @Override
    public List<String> uploadMultipleImages(List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return urls;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                urls.add(uploadImage(file, folder));
            }
        }
        return urls;
    }

    @Override
    public String uploadVideo(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File video tải lên không được để trống!");
        }

        if (isRealCloudinaryConfigured()) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", "bookstore/" + folder,
                        "resource_type", "video"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                log.info("Tải video lên Cloudinary thành công: {}", secureUrl);
                return secureUrl;
            } catch (Exception e) {
                log.warn("Cloudinary video không khả dụng ({}), tự động chuyển sang lưu trữ cục bộ.", e.getMessage());
            }
        }
        return saveFileLocally(file, folder);
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        if (isRealCloudinaryConfigured()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Đã xóa file trên Cloudinary với publicId: {}", publicId);
            } catch (Exception e) {
                log.warn("Bỏ qua xóa file trên Cloudinary: {}", e.getMessage());
            }
        }
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        if (fileUrl.startsWith("/uploads/")) {
            try {
                Path p = Paths.get(fileUrl.substring(1)).toAbsolutePath().normalize();
                Files.deleteIfExists(p);
            } catch (Exception ignored) {
            }
            return;
        }
        try {
            int uploadIndex = fileUrl.indexOf("/upload/");
            if (uploadIndex != -1) {
                String sub = fileUrl.substring(uploadIndex + 8);
                if (sub.matches("^v\\d+/.*")) {
                    sub = sub.substring(sub.indexOf("/") + 1);
                }
                int dotIndex = sub.lastIndexOf(".");
                String publicId = (dotIndex != -1) ? sub.substring(0, dotIndex) : sub;
                deleteFile(publicId);
            }
        } catch (Exception e) {
            log.warn("Không thể trích xuất publicId từ URL {}: {}", fileUrl, e.getMessage());
        }
    }
}
