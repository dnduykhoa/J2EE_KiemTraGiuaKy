package nhom05.daonguyenduykhoa_2280601493.service;

import nhom05.daonguyenduykhoa_2280601493.model.Category;
import nhom05.daonguyenduykhoa_2280601493.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existingCategory = categoryRepository.findById(id).orElse(null);
        if (existingCategory != null) {
            existingCategory.setName(category.getName());
            return categoryRepository.save(existingCategory);
        }
        return null;
    }

    public boolean deleteCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return false;
        }
        
        // Kiểm tra xem có sách nào thuộc danh mục này không
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            return false; // Không được xóa nếu còn sách
        }
        
        categoryRepository.deleteById(id);
        return true;
    }
}
