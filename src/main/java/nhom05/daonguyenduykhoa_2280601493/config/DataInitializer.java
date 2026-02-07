package nhom05.daonguyenduykhoa_2280601493.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import nhom05.daonguyenduykhoa_2280601493.repository.RoleRepository;
import nhom05.daonguyenduykhoa_2280601493.repository.UserRepository;
import nhom05.daonguyenduykhoa_2280601493.model.Role;
import nhom05.daonguyenduykhoa_2280601493.model.User;

import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Tạo tài khoản admin mặc định nếu chưa có
        User adminUser = userRepository.findByUsername("admin");
        if (adminUser == null) {
            // Lấy role ADMIN từ database
            Role adminRole = roleRepository.findRoleById(1L);
            
            if (adminRole != null) {
                adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setEmail("admin@hutech.edu.vn");
                adminUser.setFullName("Administrator");
                adminUser.setPassword(new BCryptPasswordEncoder().encode("admin123"));
                adminUser.setProvider("LOCAL");
                
                // Gán role ADMIN
                adminUser.setRoles(new HashSet<>());
                adminUser.getRoles().add(adminRole);
                
                userRepository.save(adminUser);
                
                System.out.println("========================================");
                System.out.println("Tài khoản admin đã được tạo:");
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
                System.out.println("========================================");
            } else {
                System.err.println("Lỗi: Không tìm thấy role ADMIN trong database!");
            }
        }
    }
}