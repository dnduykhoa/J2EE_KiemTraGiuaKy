package nhom05.daonguyenduykhoa_2280601493.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private List<Item> cartItems = new ArrayList<>();
    
    public void addItem(Item item) {
        boolean isExist = cartItems.stream()
            .filter(i -> Objects.equals(i.getBookId(), item.getBookId()))
            .findFirst()
            .map(i -> {
                i.setQuantity(i.getQuantity() + item.getQuantity());
                return true;
            })
            .orElse(false);
        if (!isExist) {
            cartItems.add(item);
        }
    }

    public void removeItem(Long bookId) {
        cartItems.removeIf(i -> Objects.equals(i.getBookId(), bookId));
    }

    public void updateItems(Long bookId, int quantity) {
        cartItems.stream()
            .filter(i -> Objects.equals(i.getBookId(), bookId))
            .forEach(item -> item.setQuantity(quantity));
    }
}
