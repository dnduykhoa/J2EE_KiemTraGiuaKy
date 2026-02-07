package nhom05.daonguyenduykhoa_2280601493.controller;

import nhom05.daonguyenduykhoa_2280601493.service.CartService;
import nhom05.daonguyenduykhoa_2280601493.service.UserService;
import nhom05.daonguyenduykhoa_2280601493.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    // Hiển thị trang checkout
    @GetMapping("/checkout")
    public String showCheckout(Model model, HttpSession session) {
        var cart = cartService.getCart(session);
        var totalPrice = cartService.getTotalPrice(session);
        var totalQuantity = cartService.getTotalQuantity(session);
        
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalQuantity", totalQuantity);
        
        return "order/checkout";
    }
    
    // Xử lý đặt hàng
    @PostMapping("/checkout")
    public String processCheckout(HttpSession session, 
                                 RedirectAttributes redirectAttributes,
                                 String customerName,
                                 String phone,
                                 String address,
                                 String note) {
        var cart = cartService.getCart(session);
        
        if (cart.getCartItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        
        var totalPrice = cartService.getTotalPrice(session);
        var totalQuantity = cartService.getTotalQuantity(session);
        
        // Lưu đơn hàng với thông tin khách hàng
        var order = cartService.saveCart(session);
        if (order != null) {
            order.setCustomerName(customerName);
            order.setPhone(phone);
            order.setAddress(address);
            order.setNote(note);
            orderRepository.save(order);
        }
        
        // Thêm thông tin vào redirect
        redirectAttributes.addFlashAttribute("totalPrice", totalPrice);
        redirectAttributes.addFlashAttribute("totalQuantity", totalQuantity);
        
        // Xóa giỏ hàng
        cartService.removeCart(session);
        
        return "redirect:/orders/success";
    }
    
    // Hiển thị trang đặt hàng thành công
    @GetMapping("/success")
    public String orderSuccess() {
        return "order/success";
    }
    
    // Hiển thị danh sách đơn hàng
    @GetMapping
    @Transactional(readOnly = true)
    public String getAllOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        var currentUser = userService.findByUsername(username);
        
        var orders = java.util.Collections.<nhom05.daonguyenduykhoa_2280601493.model.Order>emptyList();
        
        // ADMIN xem tất cả đơn hàng, USER chỉ xem đơn hàng của mình
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            orders = orderRepository.findAll();
        } else if (currentUser != null) {
            orders = orderRepository.findByUserOrderByOrderDateDesc(currentUser);
        }
        
        // Force load lazy associations
        orders.forEach(order -> {
            order.getOrderItems().forEach(item -> {
                if (item.getBook() != null) {
                    item.getBook().getTitle(); // Touch to initialize
                }
            });
        });
        
        model.addAttribute("orders", orders);
        model.addAttribute("isAdmin", auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN")));
        return "order/list";
    }
}