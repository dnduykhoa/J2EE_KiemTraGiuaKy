package nhom05.daonguyenduykhoa_2280601493.repository;

import nhom05.daonguyenduykhoa_2280601493.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
