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
                "💻 Sách về lập trình & code",
                "🔥 Sách bán chạy nhất",
                "📚 Gợi ý sách văn học hay",
                "💰 Sách giá rẻ dưới 60k",
                "✨ Sách tình trạng như mới 99%",
                "🚚 Chính sách vận chuyển",
                "🎟️ Có mã giảm giá nào không?"
        );
    }

    // ==========================================
    // BẢNG TỪ ĐỒNG NGHĨA & ALIAS THEO CHUYÊN MỤC
    // ==========================================
    private static final Map<String, List<String>> CATEGORY_ALIASES = new LinkedHashMap<>();
    static {
        CATEGORY_ALIASES.put("cong-nghe-thong-tin", List.of(
                "công nghệ thông tin", "cong nghe thong tin", "lập trình viên", "lập trình", "lap trinh",
                "clean architecture", "clean code", "cấu trúc dữ liệu", "cau truc du lieu", "kiến trúc phần mềm",
                "trí tuệ nhân tạo", "tri tue nhan tao", "machine learning", "deep learning", "design pattern",
                "the pragmatic programmer", "head first", "phần mềm", "phan mem", "thuật toán", "thuat toan",
                "algorithm", "database", "cơ sở dữ liệu", "co so du lieu", "backend", "frontend", "fullstack",
                "coding", "coder", "code", "developer", "dev", "cntt", "it", "python", "java", "c++", "c#",
                "javascript", "golang", "sql", "clrs", "web", "ai"
        ));

        CATEGORY_ALIASES.put("kinh-te-tai-chinh", List.of(
                "kinh tế & tài chính", "kinh tế", "kinh te", "tài chính", "tai chinh", "chứng khoán", "chung khoan",
                "khởi nghiệp", "khoi nghiep", "startup", "đầu tư", "dau tu", "kinh doanh", "quản trị", "quan tri",
                "marketing", "bán hàng", "ban hang", "sales", "làm giàu", "lam giau", "tiền tệ", "quản lý tài chính",
                "warren buffett", "người giàu có nhất thành babylon", "quốc gia khởi nghiệp", "từ tốt đến vĩ đại",
                "cha giàu cha nghèo", "tư duy triệu phú", "dốc hết trái tim"
        ));

        CATEGORY_ALIASES.put("tam-ly-ky-nang", List.of(
                "tâm lý & kỹ năng", "tâm lý học", "tâm lý", "tam ly", "kỹ năng sống", "kỹ năng", "ky nang",
                "phát triển bản thân", "giao tiếp", "giao tiep", "thói quen nguyên tử", "thói quen", "thoi quen",
                "atomic habits", "đắc nhân tâm", "dac nhan tam", "tư duy nhanh và chậm", "tư duy", "tu duy",
                "chữa lành", "chua lanh", "hạnh phúc", "thành đạt", "self help", "sống tích cực", "kỷ luật",
                "dám bị ghét", "hiểu về trái tim", "quẳng gánh lo", "hành trình về phương đông", "sức mạnh của hiện tại"
        ));

        CATEGORY_ALIASES.put("truyen-tranh-thieu-nhi", List.of(
                "truyện tranh & thiếu nhi", "truyện tranh", "truyen tranh", "thiếu nhi", "thieu nhi", "manga",
                "doraemon", "conan", "dế mèn phiêu lưu ký", "dế mèn", "de men", "hoạt hình", "anime",
                "one piece", "naruto", "dragon ball", "thần đồng đất việt", "truyện màu", "cổ tích", "truyện thiếu nhi"
        ));

        CATEGORY_ALIASES.put("van-hoc-viet-nam", List.of(
                "văn học việt nam", "van hoc viet nam", "văn học việt", "van hoc viet",
                "văn học", "van hoc", "tiểu thuyết", "tieu thuyet", "truyện ngắn", "truyen ngan",
                "thơ ca", "tho ca", "thơ", "vũ trọng phụng", "nam cao", "ngô tất tố", "nguyễn nhật ánh",
                "tô hoài", "xuân diệu", "số đỏ", "chí phèo", "tắt đèn", "lão hạc", "truyện kiều", "vợ nhặt",
                "nguyễn du", "nguyễn bính", "thơ mới", "đoàn giỏi", "đất rừng phương nam"
        ));

        CATEGORY_ALIASES.put("van-hoc-nuoc-ngoai", List.of(
                "văn học nước ngoài", "van hoc nuoc ngoai", "tiểu thuyết nước ngoài", "văn học dịch",
                "tiểu thuyết kinh điển", "kinh điển", "trinh thám", "trinh tham", "sherlock holmes",
                "haruki murakami", "nobel văn học", "văn học phương tây", "tiểu thuyết lãng mạn",
                "ông già và biển cả", "trăm năm cô đơn", "chiến tranh và hòa bình", "tội ác và hình phạt",
                "đại gia gatsby", "không gia đình", "chuông nguyện hồn ai", "hoàng tử bé"
        ));

        CATEGORY_ALIASES.put("khoa-hoc-lich-su", List.of(
                "khoa học - vũ trụ - lịch sử", "khoa học", "khoa hoc", "vũ trụ", "vu tru", "lịch sử việt nam",
                "lịch sử", "lich su", "vật lý", "vat ly", "thiên văn", "thien van", "tiến hóa", "tien hoa",
                "sử việt", "việt nam sử lược", "sapiens", "lược sử loài người", "lược sử vạn vật", "khảo cổ",
                "sinh học", "vũ trụ trong vỏ hạt dẻ", "stephen hawking"
        ));
    }

    // ==========================================
    // TƯ VẤN & GỢI Ý SÁCH (Book Recommendation)
    // ==========================================
    private AiChatResponse handleBookRecommendation(String originalQuery, String lower) {
        StringBuilder replyBuilder = new StringBuilder();

        // 1. Kiểm tra ngân sách (VD: "dưới 50k", "dưới 100000", "tầm 70k")
        BigDecimal maxPrice = extractMaxPrice(lower);

        // 2. Kiểm tra điều kiện sách (như mới, 90%, sưu tầm)
        BookCondition targetCondition = extractCondition(lower);

        // 3. Kiểm tra mong muốn sách bán chạy / mới nhất
        boolean wantBestSellers = lower.contains("bán chạy") || lower.contains("hot") || lower.contains("mua nhiều") || lower.contains("top sách");
        boolean wantNewArrivals = lower.contains("mới nhất") || lower.contains("vừa về") || lower.contains("mới nhập");

        // 4. Nhận diện danh mục sách thông minh qua danh sách & từ đồng nghĩa/alias
        List<Category> allCategories = categoryRepository.findAll();
        Category matchedCategory = detectCategory(lower, allCategories);

        // 5. Trích xuất từ khóa tìm kiếm có ý nghĩa
        List<String> meaningfulTokens = extractMeaningfulTokens(lower, matchedCategory);

        // 6. Lấy toàn bộ sách đang hoạt động để chấm điểm độ tương đồng
        List<Book> allActiveBooks = bookRepository.findByActiveTrue(PageRequest.of(0, 200)).getContent();
        List<ScoredBook> scoredBooks = new ArrayList<>();

        for (Book b : allActiveBooks) {
            int score = 0;
            String title = b.getTitle() != null ? b.getTitle().toLowerCase() : "";
            String author = b.getAuthor() != null ? b.getAuthor().toLowerCase() : "";
            String desc = b.getDescription() != null ? b.getDescription().toLowerCase() : "";
            Category bCat = b.getCategory();

            // Lọc ngân sách (nếu có yêu cầu)
            if (maxPrice != null && b.getPrice() != null && b.getPrice().compareTo(maxPrice) > 0) {
                continue;
            }

            // Lọc tình trạng sách (nếu có yêu cầu)
            if (targetCondition != null && b.getBookCondition() != targetCondition) {
                continue;
            }

            // Độ phù hợp danh mục
            if (matchedCategory != null) {
                boolean catMatch = (bCat != null && (bCat.getId().equals(matchedCategory.getId())
                        || (matchedCategory.getSlug() != null && matchedCategory.getSlug().startsWith("van-hoc")
                            && bCat.getSlug() != null && bCat.getSlug().startsWith("van-hoc"))));
                if (catMatch) {
                    score += 60; // Thưởng điểm rất cao cho chuyên mục phù hợp
                } else {
                    // Nếu người dùng đã chỉ định chuyên mục rõ ràng (như code/lập trình), sách khác chuyên mục BỊ LOẠI TRỪ TUYỆT ĐỐI
                    continue;
                }
            }

            // Tách các từ riêng biệt trong tiêu đề, tác giả, mô tả để so khớp chuẩn xác (tránh lỗi substring "căn" khớp "ăn")
            String[] titleWords = title.split("[^\\p{L}0-9\\+\\#]+");
            String[] authorWords = author.split("[^\\p{L}0-9\\+\\#]+");
            String[] descWords = desc.split("[^\\p{L}0-9\\+\\#]+");

            // Khớp các từ khóa tìm kiếm
            for (String token : meaningfulTokens) {
                if (token.contains(" ")) {
                    if (title.contains(token)) score += 40;
                    if (author.contains(token)) score += 25;
                    if (desc.contains(token)) score += 15;
                } else {
                    if (matchWord(titleWords, token)) score += 40;
                    if (matchWord(authorWords, token)) score += 25;
                    if (matchWord(descWords, token)) score += 15;
                }
            }

            // Khớp trọn vẹn cụm từ trong tiêu đề
            if (!meaningfulTokens.isEmpty()) {
                String fullPhrase = String.join(" ", meaningfulTokens);
                if (title.contains(fullPhrase)) {
                    score += 35;
                }
            }

            // Nếu người dùng chỉ muốn xem sách bán chạy hoặc sách mới về mà không có từ khóa
            if (wantBestSellers && score >= 0) {
                score += 30;
            }
            if (wantNewArrivals && score >= 0) {
                score += 30;
            }

            // Điểm cộng theo độ bán chạy (soldQuantity)
            if (score > 0 && b.getSoldQuantity() != null) {
                score += Math.min(b.getSoldQuantity() / 4, 15);
            }

            if (score > 0) {
                scoredBooks.add(new ScoredBook(b, score));
            }
        }

        // Sắp xếp theo điểm tương đồng cao nhất
        if (wantNewArrivals) {
            scoredBooks.sort((a, b) -> {
                LocalDateTime timeA = a.book.getCreatedAt() != null ? a.book.getCreatedAt() : LocalDateTime.MIN;
                LocalDateTime timeB = b.book.getCreatedAt() != null ? b.book.getCreatedAt() : LocalDateTime.MIN;
                return timeB.compareTo(timeA);
            });
        } else {
            scoredBooks.sort((a, b) -> {
                int cmp = Integer.compare(b.score, a.score);
                if (cmp != 0) return cmp;
                int soldA = a.book.getSoldQuantity() != null ? a.book.getSoldQuantity() : 0;
                int soldB = b.book.getSoldQuantity() != null ? b.book.getSoldQuantity() : 0;
                return Integer.compare(soldB, soldA);
            });
        }

        List<Book> finalBooks = scoredBooks.stream()
                .map(sb -> sb.book)
                .limit(6)
                .collect(Collectors.toList());

        // Tạo câu trả lời thông minh
        if (!finalBooks.isEmpty()) {
            replyBuilder.append("Dạ, em đã tìm thấy các tựa sách ");
            if (matchedCategory != null) {
                replyBuilder.append("về **").append(matchedCategory.getName()).append("** ");
            } else if (!meaningfulTokens.isEmpty()) {
                replyBuilder.append("phù hợp với từ khóa **\"").append(String.join(" ", meaningfulTokens)).append("\"** ");
            }
            replyBuilder.append("rất chuẩn xác theo yêu cầu của bạn nè:\n\n");

            if (matchedCategory != null) {
                replyBuilder.append("📚 **Chuyên mục:** ").append(matchedCategory.getName()).append("\n");
            }
            if (maxPrice != null) {
                replyBuilder.append("💰 **Mức giá:** Dưới ").append(CURRENCY_FORMAT.format(maxPrice)).append(" đ\n");
            }
            if (targetCondition != null) {
                replyBuilder.append("✨ **Tình trạng tuyển chọn:** ").append(targetCondition.getLabel()).append("\n");
            }
            replyBuilder.append("\nCác cuốn sách này đều đang có sẵn tại các chi nhánh chuỗi Bookly, được kiểm định kỹ lưỡng từng trang bìa và nội dung. Bạn có thể bấm vào từng cuốn sách bên dưới để xem ảnh chụp thật hoặc thêm vào giỏ nhé!");
        } else {
            // Không tìm thấy theo tiêu chí gắt gao -> Báo rõ và gợi ý sách bán chạy toàn chuỗi
            List<Book> fallbacks = bookRepository.findTop8ByActiveTrueOrderBySoldQuantityDesc();
            finalBooks = fallbacks.stream().limit(4).collect(Collectors.toList());

            replyBuilder.append("Rất tiếc là hiện tại chuỗi cửa hàng Bookly chưa có cuốn sách nào khớp với yêu cầu **\"").append(originalQuery).append("\"** của bạn.\n\n");
            replyBuilder.append("💡 **Các chuyên mục sách đang có sẵn phong phú tại Bookly:**\n");
            replyBuilder.append("- 💻 **Công nghệ thông tin & Lập trình** (*Clean Code, Kiến trúc sạch, Thuật toán, Python, SQL...*)\n");
            replyBuilder.append("- 📈 **Kinh tế & Đầu tư - Tài chính** (*Khởi nghiệp, Quản trị, Chứng khoán...*)\n");
            replyBuilder.append("- 🧠 **Tâm lý & Kỹ năng sống** (*Đắc nhân tâm, Atomic Habits, Tư duy nhanh & chậm...*)\n");
            replyBuilder.append("- 📖 **Văn học Việt Nam & Kinh điển nước ngoài** (*Số Đỏ, Dế Mèn, Trinh thám...*)\n");
            replyBuilder.append("- 🌌 **Khoa học - Vũ trụ - Lịch sử** (*Sapiens, Lược sử vạn vật...*)\n");
            replyBuilder.append("- 🎨 **Truyện tranh & Thiếu nhi** (*Manga, Doraemon, Conan...*)\n\n");
            replyBuilder.append("Bạn có thể tham khảo một số tựa sách **bán chạy nhất toàn chuỗi** dưới đây nhé:");
        }

        List<AiBookSuggestionDto> suggestionDtos = finalBooks.stream()
                .map(this::mapToSuggestionDto)
                .collect(Collectors.toList());

        List<String> nextPrompts = List.of(
                "💻 Sách về lập trình & code",
                "🔥 Xem sách bán chạy",
                "💰 Sách dưới 60k",
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

    private Category detectCategory(String lower, List<Category> allCategories) {
        // 1. So khớp trực tiếp tên hoặc slug
        for (Category cat : allCategories) {
            if (lower.contains(cat.getName().toLowerCase()) || (cat.getSlug() != null && lower.contains(cat.getSlug()))) {
                return cat;
            }
        }

        // 2. So khớp từ khóa / alias mở rộng
        for (Map.Entry<String, List<String>> entry : CATEGORY_ALIASES.entrySet()) {
            String slug = entry.getKey();
            for (String alias : entry.getValue()) {
                if (containsKeyword(lower, alias)) {
                    for (Category cat : allCategories) {
                        if (slug.equalsIgnoreCase(cat.getSlug())) {
                            return cat;
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean containsKeyword(String text, String keyword) {
        if (text == null || keyword == null || keyword.isEmpty()) return false;
        if (keyword.length() <= 3) {
            String regex = "(?i).*\\b" + Pattern.quote(keyword) + "\\b.*";
            return text.matches(regex);
        }
        return text.contains(keyword);
    }

    private List<String> extractMeaningfulTokens(String lower, Category matchedCategory) {
        String res = lower;
        if (matchedCategory != null) {
            res = res.replace(matchedCategory.getName().toLowerCase(), " ");
            if (matchedCategory.getSlug() != null) {
                res = res.replace(matchedCategory.getSlug().replace("-", " "), " ");
            }
        }

        // Loại bỏ từ dừng tiếng Việt thông dụng
        res = res.replaceAll("(?i)\\b(gợi ý|tư vấn|sách|cuốn|bộ|tập|tìm|cho tôi|cho mình|mình|tôi|em|anh|chị|bạn|muốn|cần|mua|bán|có|không|về|của|ở|tại|thể loại|loại|chuyên mục|danh mục|giá|bao nhiêu|tầm|khoảng|dưới|trên|rẻ|nhất|hay|nhé|nè|nào|đi|được|ạ|giúp|với|hỏi|cho ra|ra)\\b", " ");
        res = res.replaceAll("[0-9]+k?", " ");
        res = res.replaceAll("[^\\p{L}0-9\\+\\#\\s]", " ").trim();

        String[] parts = res.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String p : parts) {
            p = p.trim();
            if (p.length() >= 2 && !tokens.contains(p)) {
                tokens.add(p);
            }
        }
        return tokens;
    }

    private boolean matchWord(String[] words, String token) {
        if (words == null || token == null) return false;
        for (String w : words) {
            if (w.equalsIgnoreCase(token)) return true;
        }
        return false;
    }

    private static class ScoredBook {
        final Book book;
        final int score;

        ScoredBook(Book book, int score) {
            this.book = book;
            this.score = score;
        }
    }
}
