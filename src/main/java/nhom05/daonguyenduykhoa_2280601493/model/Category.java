package nhom05.daonguyenduykhoa_2280601493.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.Hibernate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 50, nullable = false, columnDefinition = "NVARCHAR(255)")
    @Size(min = 1, max = 50, message = "Tên danh mục phải từ 1 đến 50 ký tự")
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ToString.Exclude
    @JsonIgnore
    private List<Book> books = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) 
            return false;
        Category category = (Category) o;
        return getId() != null && Objects.equals(getId(),
        category.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
