package com.bookstore.bookstore.config;

import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Category;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.enums.BookCondition;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtendedBookDataSeeder {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public void seedAllBooks(Shop shop) {
        log.info("Bắt đầu nạp danh mục và 92 đầu sách thật, phong phú vào database...");

        // 1. Tạo 7 Danh mục chuẩn
        Map<String, Category> categories = new HashMap<>();
        categories.put("truyen-tranh-thieu-nhi", saveOrUpdateCategory(
                "Truyện Tranh & Thiếu Nhi",
                "truyen-tranh-thieu-nhi",
                "Manga, truyện tranh màu và các tác phẩm văn học thiếu nhi kinh điển"
        ));
        categories.put("tam-ly-ky-nang", saveOrUpdateCategory(
                "Tâm Lý & Kỹ Năng Sống",
                "tam-ly-ky-nang",
                "Sách phát triển bản thân, nghệ thuật giao tiếp, thói quen và rèn luyện tư duy"
        ));
        categories.put("kinh-te-tai-chinh", saveOrUpdateCategory(
                "Kinh Tế & Tài Chính - Đầu Tư",
                "kinh-te-tai-chinh",
                "Quản trị kinh doanh, bài học khởi nghiệp, đầu tư chứng khoán và quản lý tài chính cá nhân"
        ));
        categories.put("cong-nghe-thong-tin", saveOrUpdateCategory(
                "Công Nghệ Thông Tin & Lập Trình",
                "cong-nghe-thong-tin",
                "Giáo trình đại học, cấu trúc dữ liệu, giải thuật, kiến trúc phần mềm, web và AI"
        ));
        categories.put("van-hoc-viet-nam", saveOrUpdateCategory(
                "Văn Học Việt Nam",
                "van-hoc-viet-nam",
                "Tiểu thuyết, truyện ngắn, thơ ca Việt Nam qua các thời kỳ hiện thực và đương đại"
        ));
        categories.put("van-hoc-nuoc-ngoai", saveOrUpdateCategory(
                "Văn Học Nước Ngoài & Kinh Điển",
                "van-hoc-nuoc-ngoai",
                "Các kiệt tác văn học thế giới đạt giải Nobel, tiểu thuyết lãng mạn, trinh thám và giả tưởng"
        ));
        categories.put("khoa-hoc-lich-su", saveOrUpdateCategory(
                "Khoa Học - Vũ Trụ - Lịch Sử",
                "khoa-hoc-lich-su",
                "Nghiên cứu lịch sử Việt Nam và thế giới, vật lý thiên văn, vũ trụ và sinh học tiến hóa"
        ));

        // 2. Danh sách 92 cuốn sách thật chuẩn xác 100%
        List<BookSeed> seedList = getRealBookSeeds();

        int insertedOrUpdated = 0;
        for (BookSeed seed : seedList) {
            Category cat = categories.get(seed.categorySlug);
            if (cat == null) cat = categories.get("van-hoc-viet-nam");

            saveOrUpdateBook(
                    shop,
                    cat,
                    seed.title,
                    seed.isbn,
                    seed.author,
                    seed.publisher,
                    seed.publishYear,
                    seed.condition,
                    seed.conditionNote,
                    BigDecimal.valueOf(seed.originalPrice),
                    BigDecimal.valueOf(seed.price),
                    seed.stock,
                    seed.soldQuantity,
                    seed.imageUrl
            );
            insertedOrUpdated++;
        }

        log.info("Hoàn tất nạp {} đầu sách thật vào database!", insertedOrUpdated);
    }

    private Category saveOrUpdateCategory(String name, String slug, String description) {
        Category category = categoryRepository.findBySlug(slug).orElse(null);
        if (category == null) {
            category = Category.builder()
                    .slug(slug)
                    .active(true)
                    .build();
        }
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private void saveOrUpdateBook(Shop shop, Category category, String title, String isbn, String author,
                                  String publisher, Integer publishYear, BookCondition condition, String conditionNote,
                                  BigDecimal originalPrice, BigDecimal price, Integer stock, Integer soldQuantity,
                                  String imageUrl) {
        Book book = bookRepository.findByIsbn(isbn).orElse(null);
        if (book == null) {
            book = Book.builder()
                    .shop(shop)
                    .category(category)
                    .isbn(isbn)
                    .originalPrice(originalPrice)
                    .price(price)
                    .stock(stock)
                    .soldQuantity(soldQuantity)
                    .mainImageUrl(imageUrl)
                    .active(true)
                    .build();
        }
        book.setTitle(title);
        book.setCategory(category);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPublishYear(publishYear);
        book.setBookCondition(condition);
        book.setConditionNote(conditionNote);
        book.setOriginalPrice(originalPrice);
        book.setPrice(price);
        book.setStock(stock);
        book.setSoldQuantity(soldQuantity);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            book.setMainImageUrl(imageUrl);
        }
        bookRepository.save(book);
    }

    private record BookSeed(
            String categorySlug,
            String title,
            String isbn,
            String author,
            String publisher,
            int publishYear,
            BookCondition condition,
            String conditionNote,
            long originalPrice,
            long price,
            int stock,
            int soldQuantity,
            String imageUrl
    ) {}

    private List<BookSeed> getRealBookSeeds() {
        List<BookSeed> list = new ArrayList<>();

        // ====================================================================
        // NHÓM 1: TRUYỆN TRANH & MANGA & THIẾU NHI (14 cuốn)
        // ====================================================================
        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Doraemon Tuyển Tập - Tập 1", "ISBN9786042171014",
                "Fujiko F. Fujio", "NXB Kim Đồng", 2020, BookCondition.LIKE_NEW,
                "Bản in mới 98%, giấy sáng đẹp, không nhăn mép, đủ tranh màu",
                25000, 15000, 12, 45, "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Doraemon Tuyển Tập - Tập 2", "ISBN9786042171021",
                "Fujiko F. Fujio", "NXB Kim Đồng", 2020, BookCondition.VERY_GOOD,
                "Sách giữ gìn cẩn thận, bìa bóng đẹp, không rách trang",
                25000, 15000, 8, 38, "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Thám Tử Lừng Danh Conan - Tập 1", "ISBN9786042182010",
                "Gosho Aoyama", "NXB Kim Đồng", 2021, BookCondition.LIKE_NEW,
                "Ấn bản tái bản mới 99%, có bọc bìa kiếng nylon bảo vệ",
                25000, 16000, 15, 62, "https://images.unsplash.com/photo-1618336753974-aae8e04506aa?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Thám Tử Lừng Danh Conan - Tập 100", "ISBN9786042234009",
                "Gosho Aoyama", "NXB Kim Đồng", 2022, BookCondition.LIKE_NEW,
                "Tập đặc biệt mốc 100 tập, còn nguyên postcard tặng kèm",
                30000, 20000, 6, 40, "https://images.unsplash.com/photo-1569701813229-33284b643e3c?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "One Piece - Tập 1: Romance Dawn", "ISBN9786042131018",
                "Eiichiro Oda", "NXB Kim Đồng", 2019, BookCondition.VERY_GOOD,
                "Tập mở đầu đại hải trình, truyện sạch sẽ, giấy ngả vàng nhẹ tự nhiên",
                25000, 14000, 9, 50, "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Dragon Ball - 7 Viên Ngọc Rồng - Tập 1", "ISBN9786042101011",
                "Akira Toriyama", "NXB Kim Đồng", 2018, BookCondition.GOOD,
                "Bìa hơi sờn mép nhẹ, ruột truyện nguyên vẹn không rách rời",
                22000, 12000, 7, 28, "https://images.unsplash.com/photo-1563089145-599997674d42?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Shin - Cậu Bé Bút Chì - Tập 1", "ISBN9786042163019",
                "Yoshito Usui", "NXB Kim Đồng", 2020, BookCondition.VERY_GOOD,
                "Truyện tranh cười hài hước, tình trạng tốt 90%, đủ trang",
                25000, 15000, 10, 31, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Dế Mèn Phiêu Lưu Ký (Ấn bản xưa)", "ISBN9780004",
                "Tô Hoài", "NXB Kim Đồng", 1995, BookCondition.ACCEPTABLE,
                "Bìa có sờn mép nhẹ, ruột sách lành lặn, màu giấy cổ kính",
                70000, 35000, 4, 18, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Dế Mèn Phiêu Lưu Ký (Bản Minh Họa Màu)", "ISBN9786042125017",
                "Tô Hoài - Tạ Huy Long", "NXB Kim Đồng", 2019, BookCondition.LIKE_NEW,
                "Bản in bìa cứng cao cấp kèm tranh minh họa màu tuyệt đẹp của Tạ Huy Long",
                150000, 95000, 5, 24, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Kính Vạn Hoa - Tập 1: Nhà Ảo Thuật", "ISBN9786041121010",
                "Nguyễn Nhật Ánh", "NXB Trẻ", 2018, BookCondition.VERY_GOOD,
                "Bộ truyện học trò gắn liền tuổi thơ, sách còn rất mới 92%",
                55000, 32000, 8, 26, "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Cho Tôi Xin Một Vé Đi Tuổi Thơ", "ISBN9786041142015",
                "Nguyễn Nhật Ánh", "NXB Trẻ", 2019, BookCondition.LIKE_NEW,
                "Bản bìa mềm, tình trạng như mới 96%, trang sách tinh tươm",
                85000, 52000, 11, 48, "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Mắt Biếc", "ISBN9786041151017",
                "Nguyễn Nhật Ánh", "NXB Trẻ", 2019, BookCondition.VERY_GOOD,
                "Tác phẩm văn học lãng mạn chuyển thể điện ảnh, giấy đẹp không quăn mép",
                110000, 68000, 14, 55, "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Tôi Thấy Hoa Vàng Trên Cỏ Xanh", "ISBN9786041131019",
                "Nguyễn Nhật Ánh", "NXB Trẻ", 2018, BookCondition.GOOD,
                "Bản in năm 2018, tình trạng 85%, không ghi chú, gáy chắc chắn",
                95000, 55000, 7, 34, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("truyen-tranh-thieu-nhi", "Cô Gái Đến Từ Hôm Qua", "ISBN9786041112018",
                "Nguyễn Nhật Ánh", "NXB Trẻ", 2019, BookCondition.LIKE_NEW,
                "Bìa đẹp, sách giữ kỹ trong tủ kính, gần như chưa qua sử dụng",
                80000, 48000, 6, 29, "https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=500"));

        // ====================================================================
        // NHÓM 2: TÂM LÝ & KỸ NĂNG SỐNG (14 cuốn)
        // ====================================================================
        list.add(new BookSeed("tam-ly-ky-nang", "Đắc Nhân Tâm (How to Win Friends and Influence People)", "ISBN9786045891012",
                "Dale Carnegie", "NXB Tổng Hợp TP.HCM", 2021, BookCondition.LIKE_NEW,
                "Bản dịch kinh điển của First News, sách mới 98%, giấy không tì vết",
                98000, 58000, 20, 85, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Quẳng Gánh Lo Đi Và Vui Sống", "ISBN9786045882010",
                "Dale Carnegie", "NXB Tổng Hợp TP.HCM", 2020, BookCondition.VERY_GOOD,
                "Sách còn nguyên vẹn 90%, có bút dạ quang highlight vài dòng trang đầu",
                88000, 49000, 10, 42, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Hành Trình Về Phương Đông", "ISBN9786049811013",
                "Baird T. Spalding - Nguyên Phong dịch", "NXB Hồng Đức", 2019, BookCondition.LIKE_NEW,
                "Tác phẩm minh triết phương Đông nổi tiếng, bìa đẹp như mới 97%",
                118000, 69000, 12, 53, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Tư Duy Nhanh Và Chậm (Thinking, Fast and Slow)", "ISBN9786047741014",
                "Daniel Kahneman", "NXB Thế Giới", 2018, BookCondition.VERY_GOOD,
                "Sách tâm lý học hành vi đạt giải Nobel Kinh tế, gáy dày chắc chắn",
                219000, 125000, 7, 36, "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "7 Thói Quen Của Người Thành Đạt", "ISBN9786041162013",
                "Stephen R. Covey", "NXB Trẻ", 2020, BookCondition.LIKE_NEW,
                "Cẩm nang phát triển bản thân hàng đầu thế giới, sách nguyên vẹn 95%",
                145000, 85000, 9, 44, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Nguyên Lý 80/20 (The 80/20 Principle)", "ISBN9786041143012",
                "Richard Koch", "NXB Trẻ", 2019, BookCondition.GOOD,
                "Bí quyết đạt nhiều kết quả hơn với ít nỗ lực hơn, tình trạng tốt 85%",
                115000, 62000, 8, 30, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Thói Quen Nguyên Tử (Atomic Habits)", "ISBN9786047791019",
                "James Clear", "NXB Thế Giới", 2021, BookCondition.LIKE_NEW,
                "Bản in mới nhất, cuốn sách xây dựng thói quen bán chạy toàn cầu",
                189000, 115000, 15, 67, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Nghệ Thuật Tinh Tế Của Việc Đếch Quan Tâm", "ISBN9786049832018",
                "Mark Manson", "NXB Lao Động", 2019, BookCondition.VERY_GOOD,
                "Lối tư duy thực tế và hài hước, sách sạch đẹp không rách",
                125000, 69000, 10, 39, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Càng Kỷ Luật Càng Tự Do", "ISBN9786047781010",
                "Vãn Tình", "NXB Thế Giới", 2020, BookCondition.LIKE_NEW,
                "Sách truyền cảm hứng sống tự lập và bản lĩnh của Vãn Tình",
                109000, 65000, 11, 41, "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Dám Bị Ghét", "ISBN9786049822019",
                "Kishimi Ichiro & Koga Fumitake", "NXB Lao Động", 2019, BookCondition.LIKE_NEW,
                "Tâm lý học Adler dưới góc nhìn đối thoại triết học, tình trạng 96%",
                119000, 72000, 9, 46, "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Search Inside Yourself", "ISBN9786046531015",
                "Chade-Meng Tan", "NXB Lao Động Xã Hội", 2018, BookCondition.GOOD,
                "Chương trình đào tạo trí tuệ cảm xúc nổi tiếng tại Google",
                135000, 75000, 6, 22, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Sức Mạnh Của Hiện Tại (The Power of Now)", "ISBN9786045871014",
                "Eckhart Tolle", "NXB Tổng Hợp TP.HCM", 2019, BookCondition.VERY_GOOD,
                "Chỉ dẫn tâm linh vượt thoát căng thẳng và an trú trong hiện tại",
                110000, 64000, 8, 35, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Lối Sống Tối Giản Của Người Nhật", "ISBN9786049531011",
                "Sasaki Fumio", "NXB Thái Hà", 2018, BookCondition.VERY_GOOD,
                "Cách giải phóng không gian sống và tâm trí để đón nhận hạnh phúc",
                95000, 52000, 7, 27, "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500"));

        list.add(new BookSeed("tam-ly-ky-nang", "Hiểu Về Trái Tim", "ISBN9786041172012",
                "Thiền sư Minh Niệm", "NXB Trẻ", 2020, BookCondition.LIKE_NEW,
                "Tác phẩm chữa lành tâm hồn sâu sắc của Thiền sư Minh Niệm, bản in đẹp",
                145000, 89000, 13, 58, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        // ====================================================================
        // NHÓM 3: KINH TẾ & TÀI CHÍNH - ĐẦU TƯ (14 cuốn)
        // ====================================================================
        list.add(new BookSeed("kinh-te-tai-chinh", "Dạy Con Làm Giàu - Tập 1 (Rich Dad Poor Dad)", "ISBN9786041152014",
                "Robert T. Kiyosaki", "NXB Trẻ", 2019, BookCondition.LIKE_NEW,
                "Để không có tiền vẫn tạo ra tiền, bản in chuẩn của NXB Trẻ, sách 95%",
                95000, 55000, 18, 72, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Dạy Con Làm Giàu - Tập 2: Sử Dụng Đồng Vốn", "ISBN9786041152021",
                "Robert T. Kiyosaki", "NXB Trẻ", 2019, BookCondition.VERY_GOOD,
                "Khám phá Kim tứ đồ tài chính, tình trạng sách rất tốt 90%",
                110000, 65000, 10, 39, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Tâm Lý Học Về Tiền (The Psychology of Money)", "ISBN9786043041018",
                "Morgan Housel", "NXB Dân Trí", 2021, BookCondition.LIKE_NEW,
                "Những bài học vượt thời gian về sự giàu có, lòng tham và hạnh phúc",
                179000, 110000, 14, 61, "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Nhà Đầu Tư Thông Minh (The Intelligent Investor)", "ISBN9786046541014",
                "Benjamin Graham", "NXB Lao Động Xã Hội", 2018, BookCondition.VERY_GOOD,
                "Kinh thánh đầu tư giá trị của người thầy của Warren Buffett, bản dày bìa cứng",
                250000, 145000, 8, 34, "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Trên Đỉnh Phố Wall (One Up On Wall Street)", "ISBN9786046551012",
                "Peter Lynch", "NXB Lao Động Xã Hội", 2019, BookCondition.GOOD,
                "Kinh nghiệm đầu tư cổ phiếu thực chiến từ nhà quản lý quỹ huyền thoại",
                160000, 89000, 7, 26, "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Quốc Gia Khởi Nghiệp (Start-up Nation)", "ISBN9780005",
                "Dan Senor & Saul Singer", "NXB Thế Giới", 2018, BookCondition.GOOD,
                "Câu chuyện về nền kinh tế thần kỳ của Israel, sách còn sáng rõ",
                150000, 75000, 9, 32, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Từ Tốt Đến Vĩ Đại (Good to Great)", "ISBN9786041144019",
                "Jim Collins", "NXB Trẻ", 2019, BookCondition.VERY_GOOD,
                "Nghiên cứu kinh điển về các công ty có bước nhảy vọt vĩ đại",
                175000, 98000, 8, 37, "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Chiến Lược Đại Dương Xanh (Blue Ocean Strategy)", "ISBN9786049751012",
                "W. Chan Kim & Renée Mauborgne", "NXB Lao Động", 2018, BookCondition.LIKE_NEW,
                "Tạo khoảng trống thị trường không có cạnh tranh, sách đẹp 95%",
                180000, 105000, 6, 28, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Kinh Tế Học Hài Hước (Freakonomics)", "ISBN9786047761012",
                "Steven D. Levitt & Stephen J. Dubner", "NXB Thế Giới", 2019, BookCondition.VERY_GOOD,
                "Góc nhìn dí dỏm giải mã các hiện tượng xã hội qua lăng kính kinh tế học",
                129000, 75000, 8, 30, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Cổ Phiếu Thường, Lợi Nhuận Phi Thường", "ISBN9786046561011",
                "Philip A. Fisher", "NXB Lao Động Xã Hội", 2019, BookCondition.VERY_GOOD,
                "Phương pháp chọn cổ phiếu tăng trưởng nổi tiếng thế giới",
                135000, 78000, 7, 25, "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Phi Lý Trí (Predictably Irrational)", "ISBN9786046571010",
                "Dan Ariely", "NXB Lao Động Xã Hội", 2018, BookCondition.GOOD,
                "Khám phá những lực vô hình chi phối các quyết định mua sắm của con người",
                140000, 79000, 6, 21, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Những Kẻ Xuất Chúng (Outliers)", "ISBN9786047753017",
                "Malcolm Gladwell", "NXB Thế Giới", 2018, BookCondition.LIKE_NEW,
                "Bí mật đằng sau thành công của những vĩ nhân và quy tắc 10.000 giờ",
                130000, 76000, 10, 43, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Điểm Bùng Phát (The Tipping Point)", "ISBN9786047754014",
                "Malcolm Gladwell", "NXB Thế Giới", 2018, BookCondition.VERY_GOOD,
                "Làm thế nào những điều nhỏ bé có thể tạo nên sự khác biệt lớn",
                125000, 72000, 8, 31, "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500"));

        list.add(new BookSeed("kinh-te-tai-chinh", "Hải Trình Thương Mại Đông Dương", "ISBN9780008",
                "Nguyễn Văn Huyên", "NXB Tri Thức", 2016, BookCondition.VERY_GOOD,
                "Bản in chuyên khảo lịch sử giao thương hàng hải Đông Dương cổ, giấy dày đẹp",
                190000, 115000, 6, 19, "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=500"));

        // ====================================================================
        // NHÓM 4: CÔNG NGHỆ THÔNG TIN & LẬP TRÌNH (14 cuốn)
        // ====================================================================
        list.add(new BookSeed("cong-nghe-thong-tin", "Clean Code - Mã Nguồn Sạch", "ISBN9780006",
                "Robert C. Martin (Uncle Bob)", "NXB Bách Khoa", 2020, BookCondition.LIKE_NEW,
                "Bản cẩm nang bất hủ cho mọi lập trình viên muốn viết code sạch, dễ bảo trì",
                260000, 145000, 12, 54, "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Clean Architecture - Kiến Trúc Sạch", "ISBN9786049921015",
                "Robert C. Martin", "NXB Bách Khoa", 2021, BookCondition.LIKE_NEW,
                "Hướng dẫn thiết kế cấu trúc phần mềm chuyên nghiệp, sách mới 98%",
                280000, 165000, 9, 38, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "The Pragmatic Programmer", "ISBN9786048031012",
                "Andrew Hunt & David Thomas", "NXB Thông Tin & Truyền Thông", 2020, BookCondition.LIKE_NEW,
                "Kỷ niệm 20 năm xuất bản, cẩm nang từ thợ code đến bậc thầy phần mềm",
                275000, 155000, 8, 33, "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Giáo trình Cấu Trúc Dữ Liệu & Giải Thuật", "ISBN9780002",
                "Nhiều Tác Giả (ĐHQG)", "NXB ĐHQG", 2021, BookCondition.VERY_GOOD,
                "Giáo trình chuẩn cho sinh viên CNTT, có ghi chú nhẹ bằng bút chì vài trang",
                95000, 45000, 15, 60, "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Design Patterns - Mẫu Thiết Kế Hướng Đối Tượng", "ISBN9786047361013",
                "Gang of Four (GoF)", "NXB ĐHQG", 2019, BookCondition.VERY_GOOD,
                "23 mẫu thiết kế kinh điển giúp tái sử dụng phần mềm hướng đối tượng hiệu quả",
                290000, 160000, 7, 29, "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Refactoring - Tối Ưu Hóa Mã Nguồn Hiện Có", "ISBN9786048041011",
                "Martin Fowler", "NXB Thông Tin & Truyền Thông", 2021, BookCondition.LIKE_NEW,
                "Bản in mới, chỉ dẫn từng bước cải tiến thiết kế mã nguồn mà không làm đổi hành vi",
                310000, 185000, 6, 24, "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Domain-Driven Design (Thiết Kế Hướng Miền)", "ISBN9786047371012",
                "Eric Evans", "NXB ĐHQG", 2020, BookCondition.VERY_GOOD,
                "Xử lý độ phức tạp ở tầng lõi phần mềm của Eric Evans, sách còn mới 92%",
                320000, 190000, 5, 20, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Grokking Algorithms - Minh Họa Thuật Toán", "ISBN9786043121017",
                "Aditya Bhargava", "NXB Dân Trí", 2020, BookCondition.LIKE_NEW,
                "Cuốn sách thuật toán trực quan, dễ hiểu nhất kèm hình minh họa dí dỏm",
                165000, 99000, 14, 52, "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Lập Trình Web Hiện Đại Với Spring Boot & JPA", "ISBN9786047381011",
                "Khoa CNTT - ĐHQG TP.HCM", "NXB ĐHQG TP.HCM", 2022, BookCondition.LIKE_NEW,
                "Giáo trình thực hành xây dựng ứng dụng Web Enterprise với Spring Boot & Hibernate",
                140000, 85000, 16, 47, "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Designing Data-Intensive Applications", "ISBN9786049941013",
                "Martin Kleppmann", "NXB Bách Khoa", 2021, BookCondition.LIKE_NEW,
                "Cuốn cẩm nang toàn diện về hệ thống phân tán, cơ sở dữ liệu lớn và độ tin cậy",
                380000, 225000, 8, 35, "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Introduction to Algorithms (CLRS)", "ISBN9786049951012",
                "Thomas H. Cormen et al.", "NXB Bách Khoa", 2019, BookCondition.VERY_GOOD,
                "Bộ sách thuật toán toàn diện bậc nhất được giảng dạy tại MIT và các trường ĐH",
                420000, 240000, 5, 18, "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Lập Trình Python Căn Bản Và Nâng Cao", "ISBN9786046711019",
                "Nhiều Tác Giả", "NXB Khoa Học & Kỹ Thuật", 2021, BookCondition.LIKE_NEW,
                "Hướng dẫn tự học Python từ cơ bản đến xử lý dữ liệu và tự động hóa",
                135000, 78000, 11, 41, "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Cơ Sở Dữ Liệu Quan Hệ & SQL Thực Chiến", "ISBN9786048051010",
                "Nhiều Tác Giả", "NXB Bưu Điện", 2020, BookCondition.VERY_GOOD,
                "Nắm vững câu lệnh SQL, Indexing, Transaction và tối ưu hóa truy vấn CSDL",
                120000, 68000, 10, 36, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("cong-nghe-thong-tin", "Trí Tuệ Nhân Tạo Trong Kỷ Nguyên Số", "ISBN9786047391010",
                "Viện CNTT ĐHQG", "NXB ĐHQG", 2022, BookCondition.LIKE_NEW,
                "Tổng quan về Machine Learning, Deep Learning và mạng nơ-ron nhân tạo",
                195000, 119000, 9, 30, "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500"));

        // ====================================================================
        // NHÓM 5: VĂN HỌC VIỆT NAM (12 cuốn)
        // ====================================================================
        list.add(new BookSeed("van-hoc-viet-nam", "Số Đỏ (Bản in 1989)", "ISBN9780001",
                "Vũ Trọng Phụng", "NXB Văn Học", 1989, BookCondition.OLD,
                "Sách bìa mềm ố vàng theo thời gian, đủ trang, chữ rõ nét",
                120000, 65000, 5, 27, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Chí Phèo", "ISBN9786049611013",
                "Nam Cao", "NXB Văn Học", 2018, BookCondition.VERY_GOOD,
                "Tác phẩm hiện thực phê phán xuất sắc của Nam Cao, sách nguyên vẹn 90%",
                65000, 38000, 12, 44, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Tắt Đèn", "ISBN9786049621012",
                "Ngô Tất Tố", "NXB Văn Học", 2019, BookCondition.LIKE_NEW,
                "Hình tượng chị Dậu bất hủ trong nền văn học Việt Nam, tình trạng như mới 95%",
                70000, 42000, 10, 39, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Lão Hạc (Tuyển Tập Truyện Ngắn)", "ISBN9786042141017",
                "Nam Cao", "NXB Kim Đồng", 2019, BookCondition.VERY_GOOD,
                "Tuyển chọn các truyện ngắn nhân đạo lay động lòng người của Nam Cao",
                60000, 35000, 9, 31, "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Truyện Kiều (Bản Khảo Đính Đầy Đủ)", "ISBN9786049891015",
                "Đại thi hào Nguyễn Du", "NXB Hội Nhà Văn", 2020, BookCondition.LIKE_NEW,
                "Ấn bản bìa cứng có phụ bản tranh khắc gỗ truyền thống, rất giá trị",
                180000, 110000, 7, 28, "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Nỗi Buồn Chiến Tranh", "ISBN9786049871017",
                "Bảo Ninh", "NXB Hội Nhà Văn", 2019, BookCondition.LIKE_NEW,
                "Tiểu thuyết chiến tranh Việt Nam vang danh thế giới, sách còn rất mới",
                125000, 75000, 8, 33, "https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Thời Xa Vắng", "ISBN9786049861018",
                "Lê Lựu", "NXB Hội Nhà Văn", 2018, BookCondition.GOOD,
                "Tác phẩm cột mốc của văn học đổi mới về thân phận con người thời hậu chiến",
                95000, 52000, 6, 22, "https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Đất Rừng Phương Nam", "ISBN9786042151016",
                "Đoàn Giỏi", "NXB Kim Đồng", 2020, BookCondition.LIKE_NEW,
                "Thiên nhiên hoang sơ trù phú và con người hào sảng phương Nam, sách mới 97%",
                85000, 50000, 11, 46, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Gió Đầu Mùa (Tuyển Tập)", "ISBN9786049631011",
                "Thạch Lam", "NXB Văn Học", 2019, BookCondition.VERY_GOOD,
                "Những trang văn nhẹ nhàng, tinh tế và giàu tình trắc ẩn của Thạch Lam",
                65000, 38000, 10, 30, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Vợ Nhặt (Tuyển Tập Kim Lân)", "ISBN9786042161015",
                "Kim Lân", "NXB Kim Đồng", 2020, BookCondition.LIKE_NEW,
                "Các kiệt tác Làng, Vợ nhặt, Con chó xấu xí... bản in bìa đẹp",
                70000, 42000, 8, 35, "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Hà Nội Băm Sáu Phố Phường", "ISBN9786049881016",
                "Thạch Lam", "NXB Hội Nhà Văn", 2019, BookCondition.LIKE_NEW,
                "Bản tùy bút bất hủ về nét đẹp ẩm thực và phong vị kinh kỳ Thăng Long xưa",
                75000, 45000, 13, 51, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        list.add(new BookSeed("van-hoc-viet-nam", "Mùa Lạc", "ISBN9786049641010",
                "Nguyễn Khải", "NXB Văn Học", 2018, BookCondition.GOOD,
                "Truyện ngắn tái hiện công cuộc tái thiết cuộc sống mới ở nông trường Điện Biên",
                55000, 30000, 7, 19, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        // ====================================================================
        // NHÓM 6: VĂN HỌC NƯỚC NGOÀI & KINH ĐIỂN (14 cuốn)
        // ====================================================================
        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Nhà Giả Kim (The Alchemist)", "ISBN9786049901017",
                "Paulo Coelho", "NXB Hội Nhà Văn & Nhã Nam", 2020, BookCondition.LIKE_NEW,
                "Cuốn sách bán chạy chỉ sau Kinh Thánh, bài học theo đuổi vận mệnh đời mình",
                89000, 55000, 22, 95, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Hoàng Tử Bé (Le Petit Prince)", "ISBN9786042172011",
                "Antoine de Saint-Exupéry", "NXB Kim Đồng", 2020, BookCondition.LIKE_NEW,
                "Ấn bản minh họa màu gốc tuyệt đẹp của chính tác giả, sách như mới 98%",
                85000, 52000, 18, 80, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Ông Già Và Biển Cả", "ISBN9786049651019",
                "Ernest Hemingway", "NXB Văn Học", 2019, BookCondition.VERY_GOOD,
                "Kiệt tác đoạt giải Nobel Văn học, biểu tượng cho ý chí kiên cường của con người",
                75000, 45000, 11, 46, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Rừng Na Uy (Norwegian Wood)", "ISBN9786049911016",
                "Haruki Murakami", "NXB Hội Nhà Văn", 2019, BookCondition.LIKE_NEW,
                "Tác phẩm nổi tiếng nhất của Murakami, bản dịch của Trịnh Lữ, sách 95%",
                135000, 79000, 14, 58, "https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Trăm Năm Cô Đơn (Cien años de soledad)", "ISBN9786049661018",
                "Gabriel García Márquez", "NXB Văn Học", 2018, BookCondition.VERY_GOOD,
                "Đỉnh cao của chủ nghĩa hiện thực huyền ảo về dòng họ Buendía ở Macondo",
                180000, 105000, 7, 34, "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Không Gia Đình (Sans Famille)", "ISBN9786049671017",
                "Hector Malot", "NXB Văn Học", 2020, BookCondition.LIKE_NEW,
                "Hành trình lưu lạc đầy nghị lực của chú bé Rémi và gánh xiếc cụ Vitalis",
                160000, 95000, 9, 41, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Hai Số Phận (Kane and Abel)", "ISBN9786049681016",
                "Jeffrey Archer", "NXB Văn Học", 2019, BookCondition.VERY_GOOD,
                "Tiểu thuyết kinh điển về cuộc đối đầu định mệnh giữa hai người đàn ông kiệt xuất",
                175000, 102000, 8, 36, "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Bố Già (The Godfather)", "ISBN9786049691015",
                "Mario Puzo", "NXB Văn Học", 2019, BookCondition.LIKE_NEW,
                "Bản dịch xuất sắc của Ngọc Thứ Lang, thế giới ngầm mafia và tình cảm gia đình",
                145000, 88000, 13, 62, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Những Người Khốn Khổ (Trọn Bộ 2 Tập)", "ISBN9786049701011",
                "Victor Hugo", "NXB Văn Học", 2018, BookCondition.VERY_GOOD,
                "Bản in bìa cứng dày dặn, thiên sử thi về lòng nhân ái và công lý",
                290000, 175000, 5, 23, "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Chiến Tranh Và Hòa Bình (Trọn Bộ)", "ISBN9786049711010",
                "Lev Tolstoy", "NXB Văn Học", 2019, BookCondition.VERY_GOOD,
                "Kiệt tác vĩ đại nhất của văn học Nga thế kỷ 19, sách đẹp được bảo quản kỹ",
                350000, 210000, 4, 17, "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Suối Nguồn (The Fountainhead)", "ISBN9786041132016",
                "Ayn Rand", "NXB Trẻ", 2018, BookCondition.LIKE_NEW,
                "Tuyên ngôn về chủ nghĩa cá nhân và sự sáng tạo kiến trúc của Howard Roark",
                260000, 155000, 7, 29, "https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Giết Con Chim Nhại (To Kill a Mockingbird)", "ISBN9786049721019",
                "Harper Lee", "NXB Văn Học", 2020, BookCondition.LIKE_NEW,
                "Bài học về lòng dũng cảm, công lý và lòng thấu cảm qua con mắt trẻ thơ",
                110000, 68000, 10, 45, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "1984 (Một Chín Tám Tư)", "ISBN9786049922012",
                "George Orwell", "NXB Hội Nhà Văn", 2020, BookCondition.LIKE_NEW,
                "Tác phẩm phản địa đàng (dystopian) kinh điển cảnh báo về quyền tự do cá nhân",
                125000, 75000, 11, 48, "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=500"));

        list.add(new BookSeed("van-hoc-nuoc-ngoai", "Trại Súc Vật (Animal Farm)", "ISBN9786049923019",
                "George Orwell", "NXB Hội Nhà Văn", 2019, BookCondition.VERY_GOOD,
                "Truyện ngụ ngôn chính trị sâu cay và sắc sảo, tình trạng sách rất tốt 92%",
                85000, 50000, 9, 37, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"));

        // ====================================================================
        // NHÓM 7: KHOA HỌC - VŨ TRỤ - LỊCH SỬ - TRIẾT HỌC (10 cuốn)
        // ====================================================================
        list.add(new BookSeed("khoa-hoc-lich-su", "Lược Sử Thời Gian (A Brief History of Time)", "ISBN9786041153011",
                "Stephen Hawking", "NXB Trẻ", 2019, BookCondition.LIKE_NEW,
                "Cuốn sách phổ biến khoa học vũ trụ từ Vụ Nổ Lớn đến Lỗ Đen nổi tiếng nhất",
                125000, 75000, 12, 49, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Vũ Trụ Trong Vỏ Hạt Dẻ", "ISBN9786041163010",
                "Stephen Hawking", "NXB Trẻ", 2020, BookCondition.LIKE_NEW,
                "Khám phá các bí ẩn của lý thuyết M, thời gian đa chiều kèm hình màu trực quan",
                160000, 98000, 8, 35, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Cosmos (Vũ Trụ)", "ISBN9786047771011",
                "Carl Sagan", "NXB Thế Giới", 2019, BookCondition.VERY_GOOD,
                "Cuộc du hành kỳ thú khám phá 15 tỷ năm tiến hóa của vũ trụ và nền văn minh",
                210000, 125000, 6, 27, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Sapiens - Lược Sử Loài Người", "ISBN9786049431013",
                "Yuval Noah Harari", "NXB Tri Thức", 2018, BookCondition.LIKE_NEW,
                "Tác phẩm hiện tượng toàn cầu giải mã 70.000 năm lịch sử loài người",
                239000, 145000, 15, 68, "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Homo Deus - Lược Sử Tương Lai", "ISBN9786047782017",
                "Yuval Noah Harari", "NXB Thế Giới", 2019, BookCondition.LIKE_NEW,
                "Dự phóng về số phận của nhân loại trong kỷ nguyên trí tuệ nhân tạo và công nghệ sinh học",
                229000, 138000, 11, 47, "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "21 Bài Học Cho Thế Kỷ 21", "ISBN9786047783014",
                "Yuval Noah Harari", "NXB Thế Giới", 2019, BookCondition.VERY_GOOD,
                "Những vấn đề cấp bách nhất hiện nay: tin giả, chiến tranh, khủng hoảng khí hậu",
                219000, 129000, 9, 39, "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Gen Vị Kỷ (The Selfish Gene)", "ISBN9786049441012",
                "Richard Dawkins", "NXB Tri Thức", 2020, BookCondition.VERY_GOOD,
                "Tác phẩm kinh điển về thuyết tiến hóa giải thích hành vi động vật và con người",
                210000, 125000, 6, 22, "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Lược Sử Vạn Vật", "ISBN9786047784011",
                "Bill Bryson", "NXB Thế Giới", 2018, BookCondition.GOOD,
                "Chuyến du hành khoa học từ thuyết Vụ Nổ Lớn đến sự trỗi dậy của loài người",
                195000, 110000, 7, 26, "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Việt Nam Sử Lược", "ISBN9780003",
                "Trần Trọng Kim", "NXB Văn Hóa Thông Tin", 2005, BookCondition.LIKE_NEW,
                "Bộ sách lịch sử Việt Nam bằng chữ quốc ngữ đầu tiên, bìa cứng mới 98%",
                180000, 110000, 5, 21, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=500"));

        list.add(new BookSeed("khoa-hoc-lich-su", "Suy Tưởng (Meditations)", "ISBN9786043141015",
                "Hoàng đế La Mã Marcus Aurelius", "NXB Dân Trí", 2021, BookCondition.LIKE_NEW,
                "Tác phẩm triết học Khắc Kỷ (Stoicism) vĩ đại nhất của bậc minh quân La Mã",
                120000, 75000, 12, 53, "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=500"));

        return list;
    }
}
