package nhom05.daonguyenduykhoa_2280601493.apicontroller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nhom05.daonguyenduykhoa_2280601493.model.Book;
import nhom05.daonguyenduykhoa_2280601493.service.BookService;
import nhom05.daonguyenduykhoa_2280601493.service.CategoryService;
import nhom05.daonguyenduykhoa_2280601493.service.CartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController("apiBookController")
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor

public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;
    private final CartService cartService;

    // Lây tất cả sách
    @GetMapping("/")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // Lấy sách theo id
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id).orElse(null);
    }

    // Thêm sách mới
    @PostMapping("/")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookService.addBook(book);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    // Tìm kiếm sách theo từ khóa (title hoặc author)
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String keyword) {
        return bookService.searchBooks(keyword);
    }

    // Lấy sách theo category
    @GetMapping("/category/{categoryId}")
    public List<Book> getBooksByCategory(@PathVariable Long categoryId) {
        return bookService.getBooksByCategory(categoryId.toString());
    }

    // Cập nhật sách
    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book updateBook) {
        return bookService.updateBook(id, updateBook);      
    }

    // Xóa sách theo id
    @DeleteMapping("/{id}")
    public boolean deleteBookById(@PathVariable Long id) {
        return bookService.deleteBookById(id);
    }
}
