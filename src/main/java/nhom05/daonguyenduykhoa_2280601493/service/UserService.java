package nhom05.daonguyenduykhoa_2280601493.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;

import nhom05.daonguyenduykhoa_2280601493.repository.UserRepository;
import nhom05.daonguyenduykhoa_2280601493.repository.RoleRepository;
import nhom05.daonguyenduykhoa_2280601493.model.User;
import nhom05.daonguyenduykhoa_2280601493.constants.Role;

import java.util.HashSet;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        
        // Khởi tạo roles nếu chưa có
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        
        // Gán role USER mặc định cho đăng ký thủ công
        var userRole = roleRepository.findRoleById(Role.USER.value);
        if (userRole != null) {
            user.getRoles().add(userRole);
        }
        
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
    public void saveOauthUser(String email, @NotNull String name) {
        User existingUser = userRepository.findByUsername(email);
        if (existingUser != null) {
            return;
        }
        
        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setFullName(name);
        user.setPassword(new BCryptPasswordEncoder().encode(email));
        user.setProvider("GOOGLE");
        
        // Khởi tạo roles
        user.setRoles(new HashSet<>());
        
        // Gán role USER mặc định cho đăng nhập Google
        var userRole = roleRepository.findRoleById(Role.USER.value);
        if (userRole != null) {
            user.getRoles().add(userRole);
        }
        userRepository.save(user);
    }
}