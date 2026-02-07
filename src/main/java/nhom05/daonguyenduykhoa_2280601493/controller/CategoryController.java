package nhom05.daonguyenduykhoa_2280601493.controller;

import nhom05.daonguyenduykhoa_2280601493.service.*;
import nhom05.daonguyenduykhoa_2280601493.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách category
    @GetMapping
    public String getAllCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "category/list";
    }

    // Hiển thị form thêm category
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/add";
    }

    // Xử lý thêm category
    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category) {
        categoryService.addCategory(category);
        return "redirect:/categories";
    }

    // Hiển thị form sửa category
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "category/edit";
    }

    // Xử lý cập nhật category
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category) {
        categoryService.updateCategory(id, category);
        return "redirect:/categories";
    }

    // Xóa category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = categoryService.deleteCategoryById(id);
        
        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } else {
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Danh mục không tồn tại!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không thể xóa danh mục này vì còn " + category.getBooks().size() + " sách thuộc danh mục. Vui lòng xóa hoặc chuyển sách sang danh mục khác trước!");
            }
        }
        
        return "redirect:/categories";
    }
}
