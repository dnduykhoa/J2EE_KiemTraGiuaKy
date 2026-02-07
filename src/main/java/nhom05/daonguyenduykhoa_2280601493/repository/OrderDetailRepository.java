package nhom05.daonguyenduykhoa_2280601493.repository;

import nhom05.daonguyenduykhoa_2280601493.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

}
