package nhom05.daonguyenduykhoa_2280601493.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import nhom05.daonguyenduykhoa_2280601493.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findRoleById(Long id);
}
