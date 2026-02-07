package nhom05.daonguyenduykhoa_2280601493.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.ArrayList;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_date")
    private Date orderDate = new Date();

    @Column(name = "total")
    @Positive(message = "Tổng tiền phải lớn hơn 0")
    private Double total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderDetail> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;
    
    @Column(name = "note")
    private String note;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Order order = (Order) o;
        return getId() != null && Objects.equals(getId(), order.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
