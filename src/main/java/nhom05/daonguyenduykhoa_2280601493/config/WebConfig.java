package nhom05.daonguyenduykhoa_2280601493.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images from external directory
        String uploadPath = System.getProperty("user.dir") + "/uploads/images/books/";
        registry.addResourceHandler("/images/books/**")
                .addResourceLocations("file:" + uploadPath)
                .addResourceLocations("classpath:/static/images/books/");
    }
}
