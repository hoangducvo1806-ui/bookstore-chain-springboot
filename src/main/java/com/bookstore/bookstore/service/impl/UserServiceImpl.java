package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.UserProfileDto;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.UserRepository;
import com.bookstore.bookstore.service.CloudinaryService;
import com.bookstore.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public User updateProfile(User user, UserProfileDto dto) {
        user.setFullName(dto.getFullName().trim());
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            user.setPhone(dto.getPhone().trim());
        }
        return userRepository.save(user);
    }

    @Override
    public User updateAvatar(User user, MultipartFile avatarFile) {
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadImage(avatarFile, "avatars");
                user.setAvatarUrl(avatarUrl);
                return userRepository.save(user);
            } catch (Exception ex) {
                log.error("Lỗi khi tải ảnh đại diện lên Cloudinary: {}", ex.getMessage());
                throw new RuntimeException("Không thể tải ảnh đại diện: " + ex.getMessage());
            }
        }
        return user;
    }
}
