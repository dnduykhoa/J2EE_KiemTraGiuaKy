package nhom05.daonguyenduykhoa_2280601493.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class ExceptionController {
    
    @RequestMapping("/errorpage")
    public String handleError(HttpServletRequest request) {
        return Optional
            .ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
            .map(status -> Integer.parseInt(status.toString()))
            .filter(status -> status == 404 || status == 500 || status == 403)
            .map(status -> "error/" + status)
            .orElse("error/500");
    }
}