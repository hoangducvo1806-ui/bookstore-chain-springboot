package com.bookstore.bookstore;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VendorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @WithMockUser(username = "vendor@gmail.com", roles = {"VENDOR", "USER"})
    @DisplayName("Vendor: Xem danh sách sách tại chi nhánh của mình /vendor/books")
    void testVendorBooksList() throws Exception {
        mockMvc.perform(get("/vendor/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("bookPage"));
    }

    @Test
    @WithMockUser(username = "vendor@gmail.com", roles = {"VENDOR", "USER"})
    @DisplayName("Vendor: Mở trang thêm sách mới /vendor/books/new")
    void testVendorNewBookForm() throws Exception {
        mockMvc.perform(get("/vendor/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/book-form"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("conditions"));
    }

    @Test
    @WithMockUser(username = "vendor@gmail.com", roles = {"VENDOR", "USER"})
    @DisplayName("Vendor: Mở trang chỉnh sửa sách /vendor/books/{id}/edit")
    void testVendorEditBookForm() throws Exception {
        Book sampleBook = bookRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có sách nào!"));

        mockMvc.perform(get("/vendor/books/" + sampleBook.getId() + "/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendor/book-form"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("categoryId"));
    }

    @Test
    @WithMockUser(username = "vendor@gmail.com", roles = {"VENDOR", "USER"})
    @DisplayName("Vendor: Ẩn/Hiện (toggle) trạng thái sách POST /vendor/books/{id}/toggle")
    void testVendorToggleBook() throws Exception {
        Book sampleBook = bookRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Không có sách nào!"));

        // Toggle lần 1
        mockMvc.perform(post("/vendor/books/" + sampleBook.getId() + "/toggle")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendor/books"))
                .andExpect(flash().attributeExists("successMessage"));

        // Toggle lại lần 2 để khôi phục trạng thái ban đầu cho các test khác
        mockMvc.perform(post("/vendor/books/" + sampleBook.getId() + "/toggle")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
