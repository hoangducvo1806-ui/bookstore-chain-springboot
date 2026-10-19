package com.bookstore.bookstore.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Service quản lý upload/delete hình ảnh và video lên Cloudinary.
 * Dùng chung cho:
 * - TV1: Avatar người dùng, hình ảnh & video đánh giá sản phẩm (review).
 * - TV2: Hình ảnh sách cũ, banner & logo shop.
 * - TV3: Banner chương trình khuyến mãi, logo thương hiệu.
 */
public interface CloudinaryService {

    /**
     * Tải lên một file ảnh lên Cloudinary
     * @param file File ảnh người dùng gửi lên
     * @param folder Thư mục con trên Cloudinary (vd: "books", "avatars", "reviews", "shops")
     * @return URL ảnh an toàn (secure_url)
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Tải lên nhiều file ảnh cùng lúc
     * @param files Danh sách file ảnh
     * @param folder Thư mục lưu trữ
     * @return Danh sách URL ảnh đã tải lên
     */
    List<String> uploadMultipleImages(List<MultipartFile> files, String folder);

    /**
     * Tải lên một file video lên Cloudinary (đáp ứng yêu cầu đề tài: review kèm video)
     * @param file File video người dùng gửi lên
     * @param folder Thư mục lưu trữ
     * @return URL video an toàn (secure_url)
     */
    String uploadVideo(MultipartFile file, String folder);

    /**
     * Xóa file trên Cloudinary bằng publicId
     * @param publicId Mã định danh của file trên Cloudinary (vd: "bookstore/books/xyz")
     */
    void deleteFile(String publicId);

    /**
     * Xóa file trên Cloudinary trực tiếp bằng URL
     * @param fileUrl URL của ảnh/video cần xóa
     */
    void deleteFileByUrl(String fileUrl);
}
