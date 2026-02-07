package nhom05.daonguyenduykhoa_2280601493.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;
import org.hibernate.Hibernate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    @Size(min = 1, max = 50, message = "Tiêu đề phải từ 1 đến 255 ký tự")
    private String title;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    @Size(min = 1, max = 50, message = "Tác giả phải từ 1 đến 255 ký tự")
    @NotBlank(message = "Tác giả không được để trống")
    private String author;
    
    @Column(nullable = false)
    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price;
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String imageUrl;
    
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @NotNull(message = "Danh mục không được để trống")
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Book book = (Book) o;
        return getId() != null && Objects.equals(getId(), book.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}