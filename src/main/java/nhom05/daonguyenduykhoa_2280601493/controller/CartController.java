package nhom05.daonguyenduykhoa_2280601493.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import nhom05.daonguyenduykhoa_2280601493.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    // Hiển thị giỏ hàng
    @GetMapping
    public String showCart(HttpSession session, Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
        model.addAttribute("totalPrice", cartService.getTotalPrice(session));
        
        // Cập nhật số lượng trong session
        session.setAttribute("cartCount", cartService.getTotalQuantity(session));
        
        return "cart/cart";  // Đổi từ "book/cart" thành "cart/cart"
    }

    // Xóa một mục khỏi giỏ hàng
    @GetMapping("/remove/{id}")
    public String removeCart(HttpSession session, @PathVariable Long id) {
        var cart = cartService.getCart(session);
        cart.removeItem(id);
        
        // Cập nhật số lượng trong session
        session.setAttribute("cartCount", cartService.getTotalQuantity(session));
        
        return "redirect:/cart";
    }

    // Cập nhật số lượng mục trong giỏ hàng
    @GetMapping("/update/{id}/{quantity}")
    public String updateCart(HttpSession session, @PathVariable Long id, @PathVariable int quantity) {
        var cart = cartService.getCart(session);
        cart.updateItems(id, quantity);
        
        // Cập nhật số lượng trong session
        session.setAttribute("cartCount", cartService.getTotalQuantity(session));
        
        return "redirect:/cart";
    }

    // Xóa toàn bộ giỏ hàng
    @GetMapping("/clear")
    public String clearCart(HttpSession session) {
        cartService.removeCart(session);
        
        // Cập nhật số lượng trong session về 0
        session.setAttribute("cartCount", 0);
        
        return "redirect:/cart";
    }
}