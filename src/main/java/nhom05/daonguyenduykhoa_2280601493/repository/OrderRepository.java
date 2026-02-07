package nhom05.daonguyenduykhoa_2280601493.repository;

import nhom05.daonguyenduykhoa_2280601493.model.Order;
import nhom05.daonguyenduykhoa_2280601493.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
}
