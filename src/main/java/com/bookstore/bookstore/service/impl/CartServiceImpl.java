package com.bookstore.bookstore.service.impl;

import com.bookstore.bookstore.dto.CartShopGroupDto;
import com.bookstore.bookstore.entity.Book;
import com.bookstore.bookstore.entity.Cart;
import com.bookstore.bookstore.entity.CartItem;
import com.bookstore.bookstore.entity.Shop;
import com.bookstore.bookstore.entity.User;
import com.bookstore.bookstore.repository.BookRepository;
import com.bookstore.bookstore.repository.CartItemRepository;
import com.bookstore.bookstore.repository.CartRepository;
import com.bookstore.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    @Override
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartShopGroupDto> getCartGroupedByShop(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, CartShopGroupDto> groupMap = new LinkedHashMap<>();

        for (CartItem item : cart.getItems()) {
            Book book = item.getBook();
            Shop shop = book.getShop();
            Long shopId = shop != null ? shop.getId() : 0L;

            CartShopGroupDto group = groupMap.computeIfAbsent(shopId, id -> CartShopGroupDto.builder()
                    .shop(shop)
                    .items(new ArrayList<>())
                    .build());

            BigDecimal itemSubtotal = book.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            boolean isOutOfStock = book.getStock() < item.getQuantity();

            CartShopGroupDto.CartItemViewDto itemView = CartShopGroupDto.CartItemViewDto.builder()
                    .id(item.getId())
                    .book(book)
                    .quantity(item.getQuantity())
                    .price(book.getPrice())
                    .subtotal(itemSubtotal)
                    .outOfStock(isOutOfStock)
                    .build();

            group.getItems().add(itemView);
        }

        return new ArrayList<>(groupMap.values());
    }

    @Override
    public void addToCart(User user, Long bookId, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách!"));

        if (!book.getActive()) {
            throw new IllegalArgumentException("Sách này hiện tạm dừng kinh doanh!");
        }

        if (book.getStock() <= 0) {
            throw new IllegalArgumentException("Sách này đã hết hàng!");
        }

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndBook(cart, book);

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > book.getStock()) {
                throw new IllegalArgumentException("Tồn kho chỉ còn " + book.getStock() + " cuốn, bạn đã có " 
                        + existingItem.getQuantity() + " cuốn trong giỏ!");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            if (quantity > book.getStock()) {
                throw new IllegalArgumentException("Số lượng thêm vào giỏ (" + quantity 
                        + ") vượt quá số lượng tồn kho (" + book.getStock() + " cuốn)!");
            }
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .book(book)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newItem);
        }
    }

    @Override
    public void updateQuantity(User user, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Mục giỏ hàng không tồn tại!"));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên giỏ hàng này!");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        Book book = item.getBook();
        if (quantity > book.getStock()) {
            throw new IllegalArgumentException("Số lượng yêu cầu (" + quantity 
                    + ") vượt quá tồn kho hiện tại (" + book.getStock() + " cuốn)!");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Override
    public void removeFromCart(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Mục giỏ hàng không tồn tại!"));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên giỏ hàng này!");
        }

        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null && cart.getItems() != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalItemsCount(User user) {
        if (user == null) return 0;
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getItems() == null) return 0;
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartSubtotal(User user) {
        if (user == null) return BigDecimal.ZERO;
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null || cart.getItems() == null) return BigDecimal.ZERO;

        return cart.getItems().stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
