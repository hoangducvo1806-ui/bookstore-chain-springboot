package com.bookstore.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopRegisterDto {

    @NotBlank(message = "Tên cửa hàng không được để trống")
    @Size(max = 150, message = "Tên cửa hàng tối đa 150 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại cửa hàng không được để trống")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @NotBlank(message = "Địa chỉ cửa hàng không được để trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @Size(max = 1000, message = "Mô tả cửa hàng tối đa 1000 ký tự")
    private String description;

    @Size(max = 500, message = "Tiểu sử (bio) tối đa 500 ký tự")
    private String bio;
}
