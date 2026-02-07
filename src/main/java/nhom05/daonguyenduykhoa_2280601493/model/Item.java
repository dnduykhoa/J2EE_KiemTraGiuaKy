package nhom05.daonguyenduykhoa_2280601493.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private Long bookId;
    private String bookName;
    private Double price;
    private int quantity;
    private String imageUrl;
}

