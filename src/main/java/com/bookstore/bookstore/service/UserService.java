package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.UserProfileDto;
import com.bookstore.bookstore.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User updateProfile(User user, UserProfileDto dto);

    User updateAvatar(User user, MultipartFile avatarFile);
}
