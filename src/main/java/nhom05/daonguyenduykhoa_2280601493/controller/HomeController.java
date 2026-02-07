package nhom05.daonguyenduykhoa_2280601493.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "book/index";
    }
    
    @GetMapping("/index")
    public String index() {
        return "book/index";
    }
}
