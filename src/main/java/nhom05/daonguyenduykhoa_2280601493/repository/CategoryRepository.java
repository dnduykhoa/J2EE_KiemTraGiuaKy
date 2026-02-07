package nhom05.daonguyenduykhoa_2280601493.repository;

import nhom05.daonguyenduykhoa_2280601493.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
