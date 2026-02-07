package nhom05.daonguyenduykhoa_2280601493.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;

import java.util.Date;
import java.util.Optional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import nhom05.daonguyenduykhoa_2280601493.model.Cart;
import nhom05.daonguyenduykhoa_2280601493.model.Item;
import nhom05.daonguyenduykhoa_2280601493.model.Order;
import nhom05.daonguyenduykhoa_2280601493.model.OrderDetail;
import nhom05.daonguyenduykhoa_2280601493.repository.BookRepository;
import nhom05.daonguyenduykhoa_2280601493.repository.OrderDetailRepository;
import nhom05.daonguyenduykhoa_2280601493.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final String CART_SESSION_KEY = "cart";

    private final BookRepository bookRepository;

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;
    
    private final UserService userService;

    // Lấy giỏ hàng từ session, nếu chưa có thì tạo mới
    public Cart getCart(@NotNull HttpSession session) {
        return Optional.ofNullable((Cart) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return cart;
                });
    }

    // Cập nhật giỏ hàng trong session
    public void updateCart(@NotNull HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    // Xóa giỏ hàng khỏi session
    public void removeCart(@NotNull HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    // Tính tổng số lượng và tổng giá trị trong giỏ hàng
    public int getTotalQuantity(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }

    // Tính tổng giá trị trong giỏ hàng
    public double getTotalPrice(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public Order saveCart(@NotNull HttpSession session) {
        var cart = getCart(session);
        if (cart.getCartItems().isEmpty()) {
            return null;
        }
        
        // Lấy user hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        var currentUser = userService.findByUsername(username);
        
        var order = new Order();
        order.setOrderDate(new Date(new Date().getTime()));
        order.setTotal(getTotalPrice(session));
        order.setUser(currentUser); // Set user cho order
        orderRepository.save(order);

        cart.getCartItems().forEach(item -> {
            var items = new OrderDetail();
            items.setOrder(order);
            items.setQuantity(item.getQuantity());
            items.setBook(bookRepository.findById(item.getBookId()).orElseThrow());
            
            // Lưu giá sản phẩm vào OrderDetail
            var book = bookRepository.findById(item.getBookId()).orElseThrow();
            items.setPrice(book.getPrice());
            
            orderDetailRepository.save(items);
        });
        
        return order;
    }
}
