package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tải lên không được để trống!");
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "bookstore/" + folder,
                    "resource_type", "image"
            ));
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Tải ảnh lên Cloudinary thành công: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh lên Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Tải ảnh lên Cloudinary thất bại: " + e.getMessage(), e);
        }
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

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "bookstore/" + folder,
                    "resource_type", "video"
            ));
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Tải video lên Cloudinary thành công: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Lỗi khi tải video lên Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Tải video lên Cloudinary thất bại: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa file trên Cloudinary với publicId: {}", publicId);
        } catch (IOException e) {
            log.error("Lỗi khi xóa file trên Cloudinary: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // URL Cloudinary dạng: https://res.cloudinary.com/<cloud>/image/upload/v12345/bookstore/folder/file.jpg
            int uploadIndex = fileUrl.indexOf("/upload/");
            if (uploadIndex != -1) {
                String sub = fileUrl.substring(uploadIndex + 8);
                // Bỏ qua version tiền tố v1234567/ nếu có
                if (sub.matches("^v\\d+/.*")) {
                    sub = sub.substring(sub.indexOf("/") + 1);
                }
                // Bỏ đuôi mở rộng file (.jpg, .png, .mp4...)
                int dotIndex = sub.lastIndexOf(".");
                String publicId = (dotIndex != -1) ? sub.substring(0, dotIndex) : sub;
                deleteFile(publicId);
            }
        } catch (Exception e) {
            log.warn("Không thể trích xuất publicId từ URL {}: {}", fileUrl, e.getMessage());
        }
    }
}
