package com.bookstore.bookstore.service;

import com.bookstore.bookstore.dto.AiChatRequest;
import com.bookstore.bookstore.dto.AiChatResponse;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.ShippingCarrier;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.ShippingCarrierRepository;
import com.bookstore.bookstore.repository.ShopRepository;
import com.bookstore.bookstore.repository.VoucherRepository;
import com.bookstore.bookstore.service.impl.AiChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ShippingCarrierRepository shippingCarrierRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @Test
    void testChatGreeting() {
        AiChatRequest request = AiChatRequest.builder().message("Xin chào").build();
        AiChatResponse response = aiChatService.chat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Trợ lý AI của Chuỗi Cửa Hàng Sách Cũ Bookly"));
        assertNotNull(response.getQuickReplies());
        assertTrue(response.getQuickReplies().size() > 0);
    }

    @Test
    void testChatShippingInquiry() {
        ShippingCarrier carrier = ShippingCarrier.builder()
                .id(1L)
                .name("Giao Hàng Nhanh")
                .shippingFee(BigDecimal.valueOf(25000))
                .description("Giao siêu tốc 24h")
                .active(true)
                .build();

        when(shippingCarrierRepository.findByActiveTrue()).thenReturn(List.of(carrier));

        AiChatRequest request = AiChatRequest.builder().message("Phí ship giao hàng bao nhiêu?").build();
        AiChatResponse response = aiChatService.chat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Giao Hàng Nhanh"));
        assertTrue(response.getReply().contains("25.000"));
    }

    @Test
    void testChatConditionInquiry() {
        Book book = Book.builder()
                .id(1L)
                .title("Dế Mèn Phiêu Lưu Ký")
                .author("Tô Hoài")
                .price(BigDecimal.valueOf(45000))
                .originalPrice(BigDecimal.valueOf(80000))
                .bookCondition(BookCondition.LIKE_NEW)
                .active(true)
                .build();

        when(bookRepository.findByActiveTrue(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        AiChatRequest request = AiChatRequest.builder().message("Giải thích tình trạng sách cũ như mới 99%").build();
        AiChatResponse response = aiChatService.chat(request);

        assertNotNull(response);
        assertTrue(response.getReply().contains("Như mới (99%)"));
        assertTrue(response.getReply().contains("Rất tốt (90%)"));
    }

    @Test
    void testChatBookRecommendationByPrice() {
        Category cat = Category.builder().id(1L).name("Văn học").slug("van-hoc").build();
        Book cheapBook = Book.builder()
                .id(10L)
                .title("Mắt Biếc")
                .author("Nguyễn Nhật Ánh")
                .price(BigDecimal.valueOf(45000))
                .category(cat)
                .bookCondition(BookCondition.VERY_GOOD)
                .active(true)
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        when(bookRepository.findByActiveTrue(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(cheapBook)));

        AiChatRequest request = AiChatRequest.builder().message("Tìm sách văn học dưới 50k").build();
        AiChatResponse response = aiChatService.chat(request);

        assertNotNull(response);
        assertNotNull(response.getSuggestedBooks());
        assertFalse(response.getSuggestedBooks().isEmpty());
        assertEquals("Mắt Biếc", response.getSuggestedBooks().get(0).getTitle());
    }
}
