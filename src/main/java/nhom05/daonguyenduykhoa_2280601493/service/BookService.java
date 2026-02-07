package nhom05.daonguyenduykhoa_2280601493.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import nhom05.daonguyenduykhoa_2280601493.model.Book;
import nhom05.daonguyenduykhoa_2280601493.repository.BookRepository;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    // Lấy tất cả sách
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Thêm sách mới
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    // Tìm sách theo id
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    // Xóa sách theo id
    public boolean deleteBookById(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Cập nhật sách
    public Book updateBook(Long id, Book updateBook) {
        return bookRepository.findById(id)
            .map(book -> {
                book.setTitle(updateBook.getTitle());
                book.setAuthor(updateBook.getAuthor());
                book.setPrice(updateBook.getPrice());
                book.setCategory(updateBook.getCategory());
                book.setImageUrl(updateBook.getImageUrl());
                return bookRepository.save(book);
            })
            .orElse(null);
    }

    // Tìm kiếm sách theo từ khóa
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword);
    }

    // Tìm sách theo category
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    // Tìm sách theo khoảng giá
    public List<Book> getBooksByPriceRange(Double minPrice, Double maxPrice) {
        return bookRepository.findByPriceBetween(minPrice, maxPrice);
    }

    // Lấy top 5 sách đắt nhất
    public List<Book> getTop5MostExpensiveBooks() {
        return bookRepository.findTop5MostExpensiveBooks();
    }
}