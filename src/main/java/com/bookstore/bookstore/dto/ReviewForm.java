package com.bookstore.bookstore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewForm {

    @NotNull(message = "Thiếu thông tin sách cần đánh giá")
    private Long bookId;

    @NotNull(message = "Thiếu thông tin đơn hàng")
    private Long orderId;

    @NotNull(message = "Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private Integer rating;

    @NotBlank(message = "Nội dung nhận xét không được để trống")
    @Size(min = 50, max = 2000, message = "Nhận xét phải có độ dài tối thiểu 50 ký tự để mô tả chi tiết tình trạng sách")
    private String comment;

    private List<MultipartFile> imageFiles;
    private MultipartFile videoFile;
}
