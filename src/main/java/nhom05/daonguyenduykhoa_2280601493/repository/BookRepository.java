package nhom05.daonguyenduykhoa_2280601493.repository;

import nhom05.daonguyenduykhoa_2280601493.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Tìm kiếm theo tên sách (tự động tạo query)
    List<Book> findByTitleContaining(String title);
    
    // Tìm kiếm theo tác giả
    List<Book> findByAuthor(String author);
    
    // Tìm kiếm theo category
    List<Book> findByCategory(String category);
    
    // Tìm sách có giá trong khoảng
    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);
    
    // Tìm sách theo tên và sắp xếp theo giá
    List<Book> findByTitleContainingOrderByPriceAsc(String title);
    
    // Custom query với @Query - Tìm sách theo tên hoặc tác giả
    @Query("SELECT b FROM Book b WHERE b.title LIKE %?1% OR b.author LIKE %?1% OR b.category.name LIKE %?1%")
    List<Book> searchBooks(@Param("keyword") String keyword);
    
    // Native SQL query - Tìm top 5 sách đắt nhất
    @Query(value = "SELECT TOP 5 * FROM books ORDER BY price DESC", nativeQuery = true)
    List<Book> findTop5MostExpensiveBooks();
    
    // Đếm số sách theo category
    Long countByCategory(String category);
}
