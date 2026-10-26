package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.AiBookSuggestionDto;
import com.bookstore.bookstore.dto.AiChatRequest;
import com.bookstore.bookstore.dto.AiChatResponse;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.ShippingCarrier;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.Voucher;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.CategoryRepository;
import com.bookstore.bookstore.repository.ShippingCarrierRepository;
import com.bookstore.bookstore.repository.ShopRepository;
import com.bookstore.bookstore.repository.VoucherRepository;
import com.bookstore.bookstore.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final ShippingCarrierRepository shippingCarrierRepository;
    private final VoucherRepository voucherRepository;
    private final ShopRepository shopRepository;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    @Transactional(readOnly = true)
    public AiChatResponse chat(AiChatRequest request) {
        String rawMessage = (request != null && request.getMessage() != null) ? request.getMessage().trim() : "";
        if (rawMessage.isEmpty()) {
            return buildWelcomeResponse();
        }

        String lower = rawMessage.toLowerCase();

        // 1. Chào hỏi & giới thiệu
        if (isGreeting(lower)) {
            return buildWelcomeResponse();
        }

        // 2. Hỏi về tình trạng sách cũ (Đặc thù Bookly)
        if (isConditionInquiry(lower)) {
            return handleConditionInquiry();
        }

        // 3. Hỏi về phí vận chuyển & đối tác giao hàng
        if (isShippingInquiry(lower)) {
            return handleShippingInquiry();
        }

        // 4. Hỏi về chính sách đổi trả / hoàn tiền / khiếu nại
        if (isReturnInquiry(lower)) {
            return handleReturnInquiry();
        }

        // 5. Hỏi về khuyến mãi / Voucher
        if (isVoucherInquiry(lower)) {
            return handleVoucherInquiry();
        }

        // 6. Hỏi về chi nhánh / chuỗi cửa hàng
        if (isShopInquiry(lower)) {
            return handleShopInquiry();
        }

        // 7. Tư vấn & gợi ý sách theo tiêu chí (Thể loại, Tác giả, Giá, Bán chạy, v.v.)
        return handleBookRecommendation(rawMessage, lower);
    }

    @Override
    public List<String> getQuickPrompts() {
        return List.of(
                "🔥 Sách bán chạy nhất",
                "📚 Gợi ý sách văn học hay",
                "💰 Sách giá rẻ dưới 60k",
                "✨ Sách tình trạng như mới 99%",
                "🚚 Chính sách vận chuyển",
                "📖 Giải thích các cấp độ sách cũ",
                "🎟️ Có mã giảm giá nào không?"
        );
    }

    // ==========================================
    // TƯ VẤN & GỢI Ý SÁCH (Book Recommendation)
    // ==========================================
    private AiChatResponse handleBookRecommendation(String originalQuery, String lower) {
        List<Book> matchedBooks = new ArrayList<>();
        StringBuilder replyBuilder = new StringBuilder();

        // Kiểm tra ngân sách (VD: "dưới 50k", "dưới 100000", "tầm 70k")
        BigDecimal maxPrice = extractMaxPrice(lower);

        // Kiểm tra điều kiện sách (như mới, 90%, sưu tầm)
        BookCondition targetCondition = extractCondition(lower);

        // Kiểm tra sách bán chạy
        boolean wantBestSellers = lower.contains("bán chạy") || lower.contains("hot") || lower.contains("mua nhiều") || lower.contains("top sách");

        // Kiểm tra sách mới về
        boolean wantNewArrivals = lower.contains("mới nhất") || lower.contains("vừa về") || lower.contains("mới nhập");

        // Tìm kiếm theo danh mục (Category)
        List<Category> allCategories = categoryRepository.findAll();
        Category matchedCategory = null;
        for (Category cat : allCategories) {
            if (lower.contains(cat.getName().toLowerCase()) || (cat.getSlug() != null && lower.contains(cat.getSlug()))) {
                matchedCategory = cat;
                break;
            }
        }

        // Lọc danh sách sách từ database
        List<Book> allActiveBooks = bookRepository.findByActiveTrue(PageRequest.of(0, 100)).getContent();
        final Category finalCategory = matchedCategory;

        matchedBooks = allActiveBooks.stream()
                .filter(b -> {
                    // Lọc theo giá
                    if (maxPrice != null && b.getPrice() != null && b.getPrice().compareTo(maxPrice) > 0) {
                        return false;
                    }
                    // Lọc theo tình trạng
                    if (targetCondition != null && b.getBookCondition() != targetCondition) {
                        return false;
                    }
                    // Lọc theo danh mục
                    if (finalCategory != null && b.getCategory() != null && !b.getCategory().getId().equals(finalCategory.getId())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Nếu người dùng có từ khóa tự do (tên sách, tác giả cụ thể)
        String cleanedKeywords = extractSearchKeywords(lower, matchedCategory);
        if (!cleanedKeywords.isEmpty() && matchedBooks.size() > 0) {
            List<Book> keywordMatched = matchedBooks.stream()
                    .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(cleanedKeywords))
                            || (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(cleanedKeywords))
                            || (b.getDescription() != null && b.getDescription().toLowerCase().contains(cleanedKeywords)))
                    .collect(Collectors.toList());

            if (!keywordMatched.isEmpty()) {
                matchedBooks = keywordMatched;
            }
        }

        // Sắp xếp
        if (wantBestSellers) {
            matchedBooks.sort((a, b) -> Integer.compare(
                    b.getSoldQuantity() != null ? b.getSoldQuantity() : 0,
                    a.getSoldQuantity() != null ? a.getSoldQuantity() : 0));
        } else if (wantNewArrivals) {
            matchedBooks.sort((a, b) -> {
                LocalDateTime timeA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                LocalDateTime timeB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                return timeB.compareTo(timeA);
            });
        }

        // Giới hạn hiển thị 4-6 cuốn đẹp nhất
        List<Book> topBooks = matchedBooks.stream().limit(6).collect(Collectors.toList());

        // Tạo câu trả lời thông minh
        if (!topBooks.isEmpty()) {
            replyBuilder.append("Dạ, em đã tìm thấy các tựa sách rất phù hợp với yêu cầu của bạn nè:\n\n");
            if (matchedCategory != null) {
                replyBuilder.append("📚 **Chuyên mục:** ").append(matchedCategory.getName()).append("\n");
            }
            if (maxPrice != null) {
                replyBuilder.append("💰 **Mức giá:** Dưới ").append(CURRENCY_FORMAT.format(maxPrice)).append(" đ\n");
            }
            if (targetCondition != null) {
                replyBuilder.append("✨ **Tình trạng tuyển chọn:** ").append(targetCondition.getLabel()).append("\n");
            }
            replyBuilder.append("\nCác cuốn sách này đều đang có sẵn tại các chi nhánh Bookly, đã được kiểm duyệt chất lượng từng trang bìa và nội dung. Bạn có thể bấm vào sách bên dưới để xem ảnh chụp thực tế hoặc thêm vào giỏ nhé!");
        } else {
            // Không tìm thấy theo tiêu chí gắt gao -> Gợi ý các cuốn sách bán chạy nổi bật nhất
            List<Book> fallbacks = bookRepository.findTop8ByActiveTrueOrderBySoldQuantityDesc();
            topBooks = fallbacks.stream().limit(4).collect(Collectors.toList());

            replyBuilder.append("Rất tiếc là hiện tại chi nhánh Bookly chưa có cuốn sách chính xác theo từ khóa **\"").append(originalQuery).append("\"**.\n\n");
            replyBuilder.append("💡 **Gợi ý dành cho bạn:**\n");
            replyBuilder.append("- Bạn có thể thử tìm bằng tên tác giả, tên chuyên mục chung chung hơn (như *Văn học, Kinh tế, Kỹ năng, Lịch sử...*).\n");
            replyBuilder.append("- Hoặc tham khảo một số tựa sách **bán chạy & được độc giả đánh giá rất cao** dưới đây nhé:");
        }

        List<AiBookSuggestionDto> suggestionDtos = topBooks.stream()
                .map(this::mapToSuggestionDto)
                .collect(Collectors.toList());

        List<String> nextPrompts = List.of(
                "🔥 Xem sách bán chạy",
                "💰 Sách dưới 50k",
                "✨ Sách như mới 99%",
                "🚚 Phí vận chuyển thế nào?"
        );

        return AiChatResponse.builder()
                .reply(replyBuilder.toString())
                .suggestedBooks(suggestionDtos)
                .quickReplies(nextPrompts)
                .build();
    }

    // ==========================================
    // CÁC CHỦ ĐỀ HỎI ĐÁP CHÍNH SÁCH & CỬA HÀNG
    // ==========================================
    private AiChatResponse buildWelcomeResponse() {
        String reply = "Xin chào bạn! 👋 Mình là **Trợ lý AI của Chuỗi Cửa Hàng Sách Cũ Bookly**.\n\n" +
                "Mình có thể hỗ trợ bạn:\n" +
                "- 🔍 **Tìm & gợi ý sách** theo thể loại, tác giả, mức giá mong muốn (ví dụ: *\"Sách văn học dưới 70k\"*).\n" +
                "- 📖 **Tư vấn tình trạng sách cũ:** Giải thích rõ các mức độ như mới 99%, rất tốt 90%, sách sưu tầm...\n" +
                "- 🚚 **Thông tin vận chuyển:** Tra cứu phí ship, thời gian giao của các nhà vận chuyển (GHN, GHTK, Viettel Post...).\n" +
                "- 🎟️ **Mã ưu đãi:** Cập nhật các voucher giảm giá đang có hiệu lực.\n\n" +
                "Bạn đang cần tìm cuốn sách nào hoặc muốn mình hỗ trợ gì không?";

        return AiChatResponse.builder()
                .reply(reply)
                .suggestedBooks(Collections.emptyList())
                .quickReplies(getQuickPrompts())
                .build();
    }

    private AiChatResponse handleConditionInquiry() {
        String reply = "📖 **Tiêu chuẩn phân loại tình trạng sách cũ tại Bookly:**\n\n" +
                "Để đảm bảo quyền lợi và sự minh bạch tuyệt đối cho quý độc giả, tất cả sách tại chuỗi cửa hàng Bookly đều trải qua kiểm định thủ công 5 bước và được gắn nhãn cụ thể:\n\n" +
                "1. **Như mới (99%):** Sách gần như chưa qua sử dụng, gáy phẳng phiu, không nếp gấp, giấy trắng đẹp, không ghi chú.\n" +
                "2. **Rất tốt (90%):** Sách được bảo quản kỹ, có thể có chút dấu vết thời gian nhẹ ở mép bìa, trang ruột sạch đẹp 100%, không mất trang.\n" +
                "3. **Tốt (75% - 85%):** Sách có dấu vết đã đọc, giấy có thể ngả vàng nhẹ tự nhiên, không rách rưới, chữ rõ ràng dễ đọc.\n" +
                "4. **Chấp nhận được (60% - 70%):** Bìa có nếp gấp hoặc sờn gáy, giấy ố vàng theo năm tháng nhưng nội dung đầy đủ vẹn nguyên.\n" +
                "5. **Cũ / Sưu tầm:** Những ấn bản xưa, xuất bản nhiều thập kỷ trước, có giá trị lưu giữ và sưu tầm quý hiếm.\n\n" +
                "📸 *Mỗi cuốn sách trên website đều có ảnh chụp thực tế tình trạng và ghi chú cụ thể từ từng chi nhánh để bạn an tâm tuyệt đối.*";

        List<Book> likeNewBooks = bookRepository.findByActiveTrue(PageRequest.of(0, 4)).getContent();
        List<AiBookSuggestionDto> dtoList = likeNewBooks.stream()
                .filter(b -> b.getBookCondition() == BookCondition.LIKE_NEW || b.getBookCondition() == BookCondition.VERY_GOOD)
                .limit(3)
                .map(this::mapToSuggestionDto)
                .collect(Collectors.toList());

        return AiChatResponse.builder()
                .reply(reply)
                .suggestedBooks(dtoList)
                .quickReplies(List.of("✨ Xem sách như mới 99%", "🔍 Chính sách đổi trả sách", "🔥 Sách bán chạy nhất"))
                .build();
    }

    private AiChatResponse handleShippingInquiry() {
        List<ShippingCarrier> carriers = shippingCarrierRepository.findByActiveTrue();
        StringBuilder reply = new StringBuilder("🚚 **Chính sách & Chi phí Vận chuyển tại Bookly:**\n\n");
        reply.append("Hệ thống kết nối trực tiếp với các đơn vị vận chuyển hàng đầu để giao sách tận tay bạn:\n\n");

        if (carriers.isEmpty()) {
            reply.append("- **Giao Hàng Nhanh / Giao Hàng Tiết Kiệm / Viettel Post**: Cước phí đồng giá chỉ từ 20.000đ - 35.000đ tùy khoảng cách.\n");
        } else {
            for (ShippingCarrier carrier : carriers) {
                String fee = carrier.getShippingFee() != null ? CURRENCY_FORMAT.format(carrier.getShippingFee()) + " đ" : "Theo bảng giá chuẩn";
                reply.append("• **").append(carrier.getName()).append("**: Cước phí từ **").append(fee).append("**");
                if (carrier.getDescription() != null && !carrier.getDescription().isBlank()) {
                    reply.append(" (").append(carrier.getDescription()).append(")");
                }
                reply.append("\n");
            }
        }

        reply.append("\n⏱️ **Thời gian giao dự kiến:**\n");
        reply.append("- Nội thành/Cùng tỉnh với chi nhánh: 1 - 2 ngày làm việc.\n");
        reply.append("- Liên tỉnh/Toàn quốc: 2 - 4 ngày làm việc.\n\n");
        reply.append("📦 Đơn sách được bọc chống sốc kỹ lưỡng để tránh cấn góc hay hư hỏng trong quá trình vận chuyển!");

        return AiChatResponse.builder()
                .reply(reply.toString())
                .suggestedBooks(Collections.emptyList())
                .quickReplies(List.of("🔍 Chính sách đổi trả", "🎟️ Có mã freeship không?", "🔥 Gợi ý sách hay"))
                .build();
    }

    private AiChatResponse handleReturnInquiry() {
        String reply = "🛡️ **Chính sách Đổi trả & Bảo vệ người mua tại Bookly:**\n\n" +
                "Bookly cam kết bảo vệ quyền lợi trọn vẹn cho bạn khi mua sách cũ:\n\n" +
                "1. **Đồng kiểm khi nhận hàng:** Bạn được quyền mở bọc bưu phẩm kiểm tra sách trước khi thanh toán.\n" +
                "2. **Đổi trả trong 48 giờ:** Nếu sách nhận được có lỗi sai khác nghiêm trọng so với mô tả (mất trang, rách nát không báo trước, giao sai tựa sách), bạn được **đổi mới hoặc hoàn tiền 100%**.\n" +
                "3. **Hỗ trợ trực tiếp:** Mỗi đơn hàng gắn liền với từng chi nhánh người bán. Bạn có thể nhắn tin trực tiếp với chủ tiệm sách hoặc gọi hotline để được hỗ trợ trong 5 phút.";

        return AiChatResponse.builder()
                .reply(reply)
                .suggestedBooks(Collections.emptyList())
                .quickReplies(List.of("📖 Các mức độ tình trạng sách", "🚚 Phí vận chuyển", "📚 Tìm sách văn học"))
                .build();
    }

    private AiChatResponse handleVoucherInquiry() {
        List<Voucher> vouchers = voucherRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActive()))
                .filter(v -> v.getEndDate() == null || v.getEndDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        StringBuilder reply = new StringBuilder("🎟️ **Mã giảm giá & Ưu đãi đang có hiệu lực:**\n\n");

        if (vouchers.isEmpty()) {
            reply.append("Hiện tại các chương trình tặng voucher toàn hệ thống đang chuẩn bị mở đợt mới. Bạn hãy nhập mã chào mừng **BOOKLYNEW** hoặc theo dõi giỏ hàng để nhận ưu đãi từ từng chi nhánh sách cũ nhé!\n");
        } else {
            for (Voucher v : vouchers) {
                reply.append("• Mã: `").append(v.getCode()).append("` — **").append(v.getTitle()).append("**\n");
                if ("PERCENTAGE".equalsIgnoreCase(v.getDiscountType())) {
                    reply.append("  Giảm ").append(v.getDiscountValue()).append("%");
                    if (v.getMaxDiscountAmount() != null) {
                        reply.append(" (Tối đa ").append(CURRENCY_FORMAT.format(v.getMaxDiscountAmount())).append(" đ)");
                    }
                } else {
                    reply.append("  Giảm trực tiếp ").append(CURRENCY_FORMAT.format(v.getDiscountValue())).append(" đ");
                }
                if (v.getMinOrderAmount() != null && v.getMinOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
                    reply.append(" cho đơn từ ").append(CURRENCY_FORMAT.format(v.getMinOrderAmount())).append(" đ");
                }
                reply.append(".\n");
            }
        }

        reply.append("\n👉 Bạn có thể áp dụng mã này ở bước thanh toán để được giảm giá ngay lập tức nhé!");

        return AiChatResponse.builder()
                .reply(reply.toString())
                .suggestedBooks(Collections.emptyList())
                .quickReplies(List.of("🔥 Gợi ý sách bán chạy", "💰 Sách dưới 50k", "🚚 Phí ship như thế nào?"))
                .build();
    }

    private AiChatResponse handleShopInquiry() {
        List<Shop> shops = shopRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder reply = new StringBuilder("🏪 **Mạng lưới chi nhánh chuỗi nhà sách Bookly:**\n\n");
        reply.append("Bookly là mô hình chuỗi cửa hàng sách cũ liên kết nhiều chi nhánh uy tín:\n\n");

        if (shops.isEmpty()) {
            reply.append("Hệ thống có các chi nhánh tại TP. Hồ Chí Minh, Hà Nội và Đà Nẵng với hàng chục ngàn đầu sách phong phú.");
        } else {
            for (Shop s : shops) {
                reply.append("📍 **").append(s.getName()).append("**\n");
                if (s.getAddress() != null && !s.getAddress().isBlank()) {
                    reply.append("   Địa chỉ: ").append(s.getAddress()).append("\n");
                }
                if (s.getPhone() != null && !s.getPhone().isBlank()) {
                    reply.append("   Hotline: ").append(s.getPhone()).append("\n");
                }
            }
        }

        reply.append("\nBạn có thể đặt hàng online từ bất kỳ chi nhánh nào, sách sẽ được bọc gửi chuyển phát nhanh tới tận nhà!");

        return AiChatResponse.builder()
                .reply(reply.toString())
                .suggestedBooks(Collections.emptyList())
                .quickReplies(List.of("📚 Tìm sách văn học", "🔥 Sách bán chạy", "🚚 Phí vận chuyển"))
                .build();
    }

    // ==========================================
    // HELPER & PARSING METHODS
    // ==========================================
    private AiBookSuggestionDto mapToSuggestionDto(Book book) {
        String catName = book.getCategory() != null ? book.getCategory().getName() : "Sách tổng hợp";
        String shopName = book.getShop() != null ? book.getShop().getName() : "Bookly Store";
        String conditionLabel = book.getBookCondition() != null ? book.getBookCondition().getLabel() : "Tốt (85%)";

        return AiBookSuggestionDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .originalPrice(book.getOriginalPrice())
                .condition(book.getBookCondition() != null ? book.getBookCondition().name() : "")
                .conditionLabel(conditionLabel)
                .imageUrl(book.getMainImageUrl() != null ? book.getMainImageUrl() : "/images/default-book.png")
                .categoryName(catName)
                .shopName(shopName)
                .build();
    }

    private boolean isGreeting(String lower) {
        return lower.matches(".*(xin chào|chào bạn|hello|hi ai|bạn là ai|ai ơi|alo|hey).*")
                || lower.equals("chào") || lower.equals("hi") || lower.equals("hello");
    }

    private boolean isConditionInquiry(String lower) {
        return lower.contains("tình trạng") || lower.contains("chất lượng sách") || lower.contains("như mới")
                || lower.contains("99%") || lower.contains("90%") || lower.contains("sách cũ là sao")
                || lower.contains("phân loại sách") || lower.contains("cấp độ sách");
    }

    private boolean isShippingInquiry(String lower) {
        return lower.contains("vận chuyển") || lower.contains("phí ship") || lower.contains("giao hàng")
                || lower.contains("ship bao nhiêu") || lower.contains("bao lâu nhận được") || lower.contains("đơn vị ship");
    }

    private boolean isReturnInquiry(String lower) {
        return lower.contains("đổi trả") || lower.contains("hoàn tiền") || lower.contains("bảo hành")
                || lower.contains("sách rách") || lower.contains("khiếu nại") || lower.contains("kiểm hàng");
    }

    private boolean isVoucherInquiry(String lower) {
        return lower.contains("voucher") || lower.contains("mã giảm") || lower.contains("khuyến mãi")
                || lower.contains("ưu đãi") || lower.contains("freeship") || lower.contains("giảm giá");
    }

    private boolean isShopInquiry(String lower) {
        return lower.contains("chi nhánh") || lower.contains("cửa hàng") || lower.contains("ở đâu")
                || lower.contains("địa chỉ") || lower.contains("shop");
    }

    private BigDecimal extractMaxPrice(String lower) {
        // Tìm pattern: dưới 50k, duoi 100k, dưới 100000, giá dưới 80.000...
        Pattern p = Pattern.compile("(dưới|duoi|tầm|tam|khoảng|khoang|<|<=)\\s*(\\d+)\\s*(k|nghìn|ngan|000)?");
        Matcher m = p.matcher(lower);
        if (m.find()) {
            try {
                long num = Long.parseLong(m.group(2));
                String unit = m.group(3);
                if (unit != null && (unit.equalsIgnoreCase("k") || unit.equalsIgnoreCase("nghìn") || unit.equalsIgnoreCase("ngan"))) {
                    num *= 1000;
                } else if (num < 1000) {
                    num *= 1000; // Người dùng nhập "dưới 50" -> hiểu là 50.000đ
                }
                return BigDecimal.valueOf(num);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private BookCondition extractCondition(String lower) {
        if (lower.contains("như mới") || lower.contains("99%")) {
            return BookCondition.LIKE_NEW;
        }
        if (lower.contains("rất tốt") || lower.contains("90%")) {
            return BookCondition.VERY_GOOD;
        }
        if (lower.contains("tốt") || lower.contains("80%") || lower.contains("85%")) {
            return BookCondition.GOOD;
        }
        if (lower.contains("sưu tầm") || lower.contains("hiếm")) {
            return BookCondition.OLD;
        }
        return null;
    }

    private String extractSearchKeywords(String lower, Category matchedCategory) {
        String res = lower;
        if (matchedCategory != null) {
            res = res.replace(matchedCategory.getName().toLowerCase(), "");
        }
        res = res.replaceAll("(gợi ý|tư vấn|sách|tìm|cho tôi|mình muốn mua|cuốn|bộ|dưới|khoảng|giá|rẻ|nhất|hay|có|không|ở|nhé|nè|nào|đi|được)", "");
        res = res.replaceAll("[0-9]+k?", "");
        res = res.replaceAll("[^\\p{L}\\s]", " ").trim();
        return res;
    }
}
