package nhom05.daonguyenduykhoa_2280601493.controller;

import nhom05.daonguyenduykhoa_2280601493.service.*;
import nhom05.daonguyenduykhoa_2280601493.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Controller
public class BookController {
    @Autowired
    private BookService bookService;
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ExcelImportService excelImportService;

    // ========== TRANG CHO USER - XEM SẢN PHẨM (PUBLIC) ========== 
@GetMapping("/books")
// Bỏ dòng này: @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
public String userBookList(@RequestParam(defaultValue = "0") int pageNo, Model model) {
    List<Book> books = bookService.getAllBooks();
    int pageSize = 9;
    int totalBooks = books.size();
    int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
    
    int start = pageNo * pageSize;
    int end = Math.min(start + pageSize, totalBooks);
    List<Book> pagedBooks = books.subList(start, end);
    
    model.addAttribute("books", pagedBooks);
    model.addAttribute("currentPage", pageNo);
    model.addAttribute("totalPages", totalPages);
    return "book/user-list";
}


    // ========== TRANG CHO ADMIN - QUẢN LÝ ĐẦY ĐỦ ========== 
    @GetMapping("/admin/books")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminBookManagement(@RequestParam(defaultValue = "0") int pageNo, Model model) {
        List<Book> books = bookService.getAllBooks();
        int pageSize = 10; // Hiển thị dạng table
        int totalBooks = books.size();
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);
        
        int start = pageNo * pageSize;
        int end = Math.min(start + pageSize, totalBooks);
        List<Book> pagedBooks = books.subList(start, end);
        
        model.addAttribute("books", pagedBooks);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", totalPages);
        return "book/list";
    }

    // Hiển thị form thêm sách (ADMIN)
    @GetMapping("/books/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    // Xử lý thêm sách (ADMIN)
    @PostMapping("/books/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String addBook(Model model, @Valid @ModelAttribute Book book, BindingResult bindingResult, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                                    .stream()
                                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/uploads/images/books/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                File file = new File(uploadDir + fileName);
                imageFile.transferTo(file);
                book.setImageUrl("/images/books/" + fileName);
                System.out.println("File uploaded successfully: " + uploadDir + fileName);
            } catch (Exception e) {
                System.err.println("Error uploading file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Nếu không upload ảnh, dùng ảnh mặc định
            book.setImageUrl("/images/books/default-book.svg");
        }
        bookService.addBook(book);
        return "redirect:/admin/books";
    }

    // Import books from Excel (ADMIN)
    @PostMapping("/books/import-excel")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String importBooksFromExcel(@RequestParam("excelFile") MultipartFile excelFile, 
                                      RedirectAttributes redirectAttributes) {
        if (excelFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn file Excel để import");
            return "redirect:/books/add";
        }
        
        String fileName = excelFile.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            redirectAttributes.addFlashAttribute("errorMessage", "File không đúng định dạng. Vui lòng chọn file Excel (.xlsx hoặc .xls)");
            return "redirect:/books/add";
        }
        
        try {
            List<Book> books = excelImportService.importBooksFromExcel(excelFile);
            
            if (books.isEmpty()) {
                redirectAttributes.addFlashAttribute("warningMessage", "Không có sản phẩm nào được import. Vui lòng kiểm tra lại file Excel");
                return "redirect:/books/add";
            }
            
            // Save all books
            int successCount = 0;
            for (Book book : books) {
                try {
                    bookService.addBook(book);
                    successCount++;
                } catch (Exception e) {
                    System.err.println("Error saving book: " + book.getTitle() + " - " + e.getMessage());
                }
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Import thành công " + successCount + " sản phẩm từ file Excel");
            return "redirect:/admin/books";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi import file Excel: " + e.getMessage());
            return "redirect:/books/add";
        }
    }

    // Download Excel Template (ADMIN)
    @GetMapping("/books/download-template")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> downloadExcelTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            // Get categories from database
            List<Category> categories = categoryService.getAllCategories();
            
            // ===== SHEET 1: Dữ liệu sách =====
            Sheet dataSheet = workbook.createSheet("Dữ Liệu Sách");
            
            // Style for instruction box
            CellStyle instructionStyle = workbook.createCellStyle();
            Font instructionFont = workbook.createFont();
            instructionFont.setBold(true);
            instructionFont.setFontHeightInPoints((short) 11);
            instructionFont.setColor(IndexedColors.DARK_RED.getIndex());
            instructionStyle.setFont(instructionFont);
            instructionStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            instructionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            instructionStyle.setBorderBottom(BorderStyle.MEDIUM);
            instructionStyle.setBorderTop(BorderStyle.MEDIUM);
            instructionStyle.setBorderLeft(BorderStyle.MEDIUM);
            instructionStyle.setBorderRight(BorderStyle.MEDIUM);
            instructionStyle.setWrapText(true);
            
            // Add instruction rows at top
            int currentRow = 0;
            Row instructionRow1 = dataSheet.createRow(currentRow++);
            Cell instCell1 = instructionRow1.createCell(0);
            instCell1.setCellValue("⚠️ HƯỚNG DẪN QUAN TRỌNG: Đọc kỹ trước khi nhập dữ liệu!");
            instCell1.setCellStyle(instructionStyle);
            dataSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));
            
            Row instructionRow2 = dataSheet.createRow(currentRow++);
            Cell instCell2 = instructionRow2.createCell(0);
            instCell2.setCellValue("1. Các cột có dấu (*) là BẮT BUỘC phải nhập\n2. Xem sheet 'Danh Sách Danh Mục' để biết tên danh mục chính xác\n3. Giá phải nhập là SỐ (không có chữ, không có dấu phẩy)\n4. Có thể xóa 3 dòng dữ liệu mẫu và nhập dữ liệu của bạn\n5. Di chuột vào tiêu đề cột để xem ghi chú chi tiết");
            instCell2.setCellStyle(instructionStyle);
            dataSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 4));
            instructionRow2.setHeightInPoints(80);
            
            // Empty row
            currentRow++;
            
            // Create header row with styling
            Row headerRow = dataSheet.createRow(currentRow++);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            String[] headers = {"Tên Sách *", "Tác Giả *", "Giá (VNĐ) *", "Danh Mục *", "Đường dẫn ảnh"};
            
            // Build category list for comment
            StringBuilder categoryList = new StringBuilder("Nhập CHÍNH XÁC một trong các danh mục sau:\n\n");
            if (categories.isEmpty()) {
                categoryList.append("(Chưa có danh mục trong hệ thống)");
            } else {
                for (int i = 0; i < categories.size(); i++) {
                    categoryList.append("  ").append(i + 1).append(". ").append(categories.get(i).getName()).append("\n");
                }
            }
            categoryList.append("\nLưu ý: Copy chính xác tên danh mục, có dấu!");
            
            String[] notes = {
                "Nhập tên sách (bắt buộc)\n\nVí dụ: Clean Code\n       Lập trình Java cơ bản",
                "Nhập tên tác giả (bắt buộc)\n\nVí dụ: Robert C. Martin\n       Nguyễn Văn A",
                "Nhập giá bằng SỐ (bắt buộc)\n\nVí dụ: 250000\n       150000\n\nLưu ý: Không có dấu phẩy, không có chữ",
                categoryList.toString(),
                "Đường dẫn ảnh (tùy chọn, có thể để trống)\n\nVí dụ: /images/books/clean-code.jpg\n\nNếu không có ảnh, để trống cột này"
            };
            
            // Create comment helper
            CreationHelper factory = workbook.getCreationHelper();
            Drawing<?> drawing = dataSheet.createDrawingPatriarch();
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                dataSheet.setColumnWidth(i, 6000);
                
                // Add comment to cell
                ClientAnchor anchor = factory.createClientAnchor();
                anchor.setCol1(i);
                anchor.setCol2(i + 4);
                anchor.setRow1(currentRow - 1);
                anchor.setRow2(currentRow + 6);
                Comment comment = drawing.createCellComment(anchor);
                RichTextString str = factory.createRichTextString(notes[i]);
                comment.setString(str);
                comment.setAuthor("Hệ thống");
                cell.setCellComment(comment);
            }
            
            // Add sample data with styling
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            CellStyle priceStyle = workbook.createCellStyle();
            priceStyle.cloneStyleFrom(dataStyle);
            priceStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
            
            String firstCategoryName = categories.isEmpty() ? "Programming" : categories.get(0).getName();
            
            // Note row before data
            Row noteRow = dataSheet.createRow(currentRow++);
            CellStyle noteRowStyle = workbook.createCellStyle();
            Font noteRowFont = workbook.createFont();
            noteRowFont.setBold(true);
            noteRowFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            noteRowStyle.setFont(noteRowFont);
            noteRowStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            noteRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            noteRowStyle.setBorderBottom(BorderStyle.MEDIUM);
            noteRowStyle.setBorderTop(BorderStyle.MEDIUM);
            noteRowStyle.setBorderLeft(BorderStyle.MEDIUM);
            noteRowStyle.setBorderRight(BorderStyle.MEDIUM);
            
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("👇 BẮT ĐẦU NHẬP DỮ LIỆU TỪ DÒNG NÀY (Dòng " + (currentRow + 1) + ") - Có thể xóa 3 dòng mẫu bên dưới và nhập dữ liệu của bạn");
            noteCell.setCellStyle(noteRowStyle);
            dataSheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(currentRow - 1, currentRow - 1, 0, 4));
            noteRow.setHeightInPoints(30);
            
            String[][] sampleData = {
                {"Clean Code", "Robert C. Martin", "250000", firstCategoryName, "/images/books/clean-code.jpg"},
                {"Lập trình Java", "Nguyễn Văn A", "180000", firstCategoryName, "/images/books/java.jpg"},
                {"Cơ sở dữ liệu", "Trần Thị B", "120000", firstCategoryName, ""}
            };
            
            for (int i = 0; i < sampleData.length; i++) {
                Row row = dataSheet.createRow(currentRow++);
                for (int j = 0; j < sampleData[i].length; j++) {
                    Cell cell = row.createCell(j);
                    if (j == 2) { // Price column
                        cell.setCellValue(Double.parseDouble(sampleData[i][j]));
                        cell.setCellStyle(priceStyle);
                    } else {
                        cell.setCellValue(sampleData[i][j]);
                        cell.setCellStyle(dataStyle);
                    }
                }
            }
            
            // ===== SHEET 2: Danh sách danh mục =====
            Sheet categorySheet = workbook.createSheet("Danh Sách Danh Mục");
            
            // Title row
            Row titleRow = categorySheet.createRow(0);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setBorderBottom(BorderStyle.MEDIUM);
            titleStyle.setBorderTop(BorderStyle.MEDIUM);
            titleStyle.setBorderLeft(BorderStyle.MEDIUM);
            titleStyle.setBorderRight(BorderStyle.MEDIUM);
            
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("📋 DANH SÁCH TẤT CẢ DANH MỤC TRONG HỆ THỐNG");
            titleCell.setCellStyle(titleStyle);
            categorySheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
            titleRow.setHeightInPoints(25);
            
            // Instruction row
            Row catInstRow = categorySheet.createRow(1);
            CellStyle catInstStyle = workbook.createCellStyle();
            Font catInstFont = workbook.createFont();
            catInstFont.setBold(true);
            catInstFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            catInstStyle.setFont(catInstFont);
            catInstStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            catInstStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            catInstStyle.setWrapText(true);
            catInstStyle.setBorderBottom(BorderStyle.THIN);
            catInstStyle.setBorderTop(BorderStyle.THIN);
            catInstStyle.setBorderLeft(BorderStyle.THIN);
            catInstStyle.setBorderRight(BorderStyle.THIN);
            
            Cell catInstCell = catInstRow.createCell(0);
            catInstCell.setCellValue("⚠️ Khi nhập dữ liệu ở sheet 'Dữ Liệu Sách', cột 'Danh Mục' phải COPY CHÍNH XÁC tên từ danh sách bên dưới (bao gồm cả dấu, viết hoa/thường)");
            catInstCell.setCellStyle(catInstStyle);
            categorySheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 1));
            catInstRow.setHeightInPoints(45);
            
            // Empty row
            categorySheet.createRow(2);
            
            // Header for category sheet
            Row catHeaderRow = categorySheet.createRow(3);
            CellStyle catHeaderStyle = workbook.createCellStyle();
            Font catHeaderFont = workbook.createFont();
            catHeaderFont.setBold(true);
            catHeaderFont.setFontHeightInPoints((short) 12);
            catHeaderFont.setColor(IndexedColors.WHITE.getIndex());
            catHeaderStyle.setFont(catHeaderFont);
            catHeaderStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            catHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            catHeaderStyle.setBorderBottom(BorderStyle.THIN);
            catHeaderStyle.setBorderTop(BorderStyle.THIN);
            catHeaderStyle.setBorderLeft(BorderStyle.THIN);
            catHeaderStyle.setBorderRight(BorderStyle.THIN);
            catHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
            
            Cell catHeaderCell1 = catHeaderRow.createCell(0);
            catHeaderCell1.setCellValue("STT");
            catHeaderCell1.setCellStyle(catHeaderStyle);
            categorySheet.setColumnWidth(0, 2000);
            
            Cell catHeaderCell2 = catHeaderRow.createCell(1);
            catHeaderCell2.setCellValue("TÊN DANH MỤC (Copy chính xác tên này)");
            catHeaderCell2.setCellStyle(catHeaderStyle);
            categorySheet.setColumnWidth(1, 10000);
            
            // Add all categories
            CellStyle catDataStyle = workbook.createCellStyle();
            catDataStyle.setBorderBottom(BorderStyle.THIN);
            catDataStyle.setBorderTop(BorderStyle.THIN);
            catDataStyle.setBorderLeft(BorderStyle.THIN);
            catDataStyle.setBorderRight(BorderStyle.THIN);
            catDataStyle.setAlignment(HorizontalAlignment.LEFT);
            
            CellStyle catNumStyle = workbook.createCellStyle();
            catNumStyle.cloneStyleFrom(catDataStyle);
            catNumStyle.setAlignment(HorizontalAlignment.CENTER);
            
            int rowIdx = 4;
            if (categories.isEmpty()) {
                Row row = categorySheet.createRow(rowIdx);
                Cell cell = row.createCell(1);
                cell.setCellValue("(Chưa có danh mục trong hệ thống. Vui lòng thêm danh mục trước)");
                cell.setCellStyle(catDataStyle);
            } else {
                for (int i = 0; i < categories.size(); i++) {
                    Row row = categorySheet.createRow(rowIdx++);
                    
                    Cell numCell = row.createCell(0);
                    numCell.setCellValue(i + 1);
                    numCell.setCellStyle(catNumStyle);
                    
                    Cell nameCell = row.createCell(1);
                    nameCell.setCellValue(categories.get(i).getName());
                    nameCell.setCellStyle(catDataStyle);
                }
            }
            
            // ===== SHEET 3: Hướng dẫn chi tiết =====
            Sheet guideSheet = workbook.createSheet("Hướng Dẫn Chi Tiết");
            guideSheet.setColumnWidth(0, 18000);
            
            CellStyle guideTitleStyle = workbook.createCellStyle();
            Font guideTitleFont = workbook.createFont();
            guideTitleFont.setBold(true);
            guideTitleFont.setFontHeightInPoints((short) 16);
            guideTitleFont.setColor(IndexedColors.DARK_RED.getIndex());
            guideTitleStyle.setFont(guideTitleFont);
            guideTitleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle stepStyle = workbook.createCellStyle();
            Font stepFont = workbook.createFont();
            stepFont.setBold(true);
            stepFont.setFontHeightInPoints((short) 12);
            stepFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            stepStyle.setFont(stepFont);
            
            CellStyle normalStyle = workbook.createCellStyle();
            Font normalFont = workbook.createFont();
            normalFont.setFontHeightInPoints((short) 11);
            normalStyle.setFont(normalFont);
            normalStyle.setWrapText(true);
            
            CellStyle noteStyle = workbook.createCellStyle();
            Font noteFont = workbook.createFont();
            noteFont.setBold(true);
            noteFont.setFontHeightInPoints((short) 11);
            noteFont.setColor(IndexedColors.DARK_RED.getIndex());
            noteStyle.setFont(noteFont);
            noteStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            noteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            noteStyle.setWrapText(true);
            
            String[] guideTexts = {
                "📖 HƯỚNG DẪN IMPORT SÁCH TỪ FILE EXCEL",
                "",
                "🔹 BƯỚC 1: Xem danh sách danh mục",
                "   • Chuyển sang sheet 'Danh Sách Danh Mục'",
                "   • Xem tất cả các danh mục có sẵn trong hệ thống",
                "   • Ghi nhớ hoặc copy tên danh mục bạn muốn sử dụng",
                "",
                "🔹 BƯỚC 2: Chuẩn bị dữ liệu",
                "   • Quay lại sheet 'Dữ Liệu Sách'",
                "   • Đọc kỹ phần hướng dẫn màu vàng ở đầu sheet",
                "   • Di chuột vào các tiêu đề cột để xem ghi chú chi tiết",
                "   • Xóa 3 dòng dữ liệu mẫu (nếu không cần)",
                "",
                "🔹 BƯỚC 3: Nhập dữ liệu sách",
                "   • Bắt đầu nhập từ dòng thứ 4 (sau tiêu đề)",
                "   • Cột 'Tên Sách': Nhập tên đầy đủ của sách",
                "   • Cột 'Tác Giả': Nhập tên tác giả",
                "   • Cột 'Giá': Chỉ nhập SỐ (vd: 250000), không nhập chữ",
                "   • Cột 'Danh Mục': COPY CHÍNH XÁC từ sheet 'Danh Sách Danh Mục'",
                "   • Cột 'Đường dẫn ảnh': Có thể để trống nếu không có",
                "",
                "🔹 BƯỚC 4: Lưu file",
                "   • Lưu file Excel (.xlsx)",
                "   • Đặt tên file dễ nhớ (vd: danh_sach_sach_import.xlsx)",
                "",
                "🔹 BƯỚC 5: Import vào hệ thống",
                "   • Vào trang web > Thêm Sách",
                "   • Chọn tab 'Import từ Excel'",
                "   • Click 'Chọn File' và chọn file Excel vừa tạo",
                "   • Nhấn 'Import từ Excel'",
                "   • Đợi hệ thống xử lý và hiển thị kết quả",
                "",
                "⚠️ LƯU Ý CỰC KỲ QUAN TRỌNG:",
                "❌ KHÔNG xóa hoặc sửa dòng tiêu đề (dòng có màu xanh dương)",
                "❌ KHÔNG đổi tên các sheet",
                "❌ KHÔNG thay đổi thứ tự các cột",
                "✅ Giá phải là SỐ thuần túy: 250000 (đúng) ❌ 250,000 (sai) ❌ 250000đ (sai)",
                "✅ Tên danh mục phải CHÍNH XÁC 100%: 'Programming' (đúng) ❌ 'programming' (sai) ❌ 'Programing' (sai)",
                "✅ Có thể nhập nhiều sách cùng lúc (nhiều dòng)",
                "✅ Cột 'Đường dẫn ảnh' có thể để trống",
                "",
                "❓ XỬ LÝ LỖI:",
                "• Nếu import thất bại, kiểm tra lại:",
                "  1. Tên danh mục có đúng không?",
                "  2. Giá có phải là số không?",
                "  3. Các cột bắt buộc (*) đã điền đủ chưa?",
                "",
                "📞 HỖ TRỢ:",
                "Nếu gặp vấn đề, vui lòng liên hệ quản trị viên hệ thống."
            };
            
            int guideRowIdx = 0;
            for (String text : guideTexts) {
                Row row = guideSheet.createRow(guideRowIdx++);
                Cell cell = row.createCell(0);
                cell.setCellValue(text);
                
                if (text.contains("📖 HƯỚNG DẪN")) {
                    cell.setCellStyle(guideTitleStyle);
                    row.setHeightInPoints(25);
                } else if (text.startsWith("🔹 BƯỚC") || text.contains("⚠️ LƯU Ý") || text.contains("❓ XỬ LÝ") || text.contains("📞 HỖ TRỢ")) {
                    cell.setCellStyle(stepStyle);
                    row.setHeightInPoints(20);
                } else if (text.startsWith("❌") || text.startsWith("✅")) {
                    cell.setCellStyle(noteStyle);
                    row.setHeightInPoints(30);
                } else {
                    cell.setCellStyle(normalStyle);
                }
            }
            
            workbook.write(outputStream);
            byte[] bytes = outputStream.toByteArray();
            
            HttpHeaders headersResponse = new HttpHeaders();
            headersResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headersResponse.setContentDispositionFormData("attachment", "mau_import_sach.xlsx");
            headersResponse.setContentLength(bytes.length);
            
            return ResponseEntity.ok()
                    .headers(headersResponse)
                    .body(bytes);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Hiển thị form sửa sách (ADMIN)
    @GetMapping("/books/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var book = bookService.getBookById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/edit";
    }

    // Xử lý cập nhật sách (ADMIN)
    @PostMapping("/books/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateBook(@PathVariable Long id, @Valid @ModelAttribute Book book, BindingResult bindingResult, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                                    .stream()
                                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        var existingBook = bookService.getBookById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                if (existingBook.getImageUrl() != null && !existingBook.getImageUrl().isEmpty() 
                    && !existingBook.getImageUrl().contains("default-book")) {
                    String oldImagePath = System.getProperty("user.dir") + "/uploads" + existingBook.getImageUrl();
                    File oldFile = new File(oldImagePath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                        System.out.println("Old file deleted: " + oldImagePath);
                    }
                }
                
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                String uploadDir = System.getProperty("user.dir") + "/uploads/images/books/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                
                File file = new File(uploadDir + fileName);
                imageFile.transferTo(file);
                book.setImageUrl("/images/books/" + fileName);
                System.out.println("File uploaded successfully: " + uploadDir + fileName);
            } catch (Exception e) {
                System.err.println("Error uploading file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            book.setImageUrl(existingBook.getImageUrl());
        }
        bookService.updateBook(id, book);
        return "redirect:/admin/books";
    }

    // Xóa sách (ADMIN)
    @GetMapping("/books/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteBook(@PathVariable Long id) {
        bookService.getBookById(id)
                .ifPresentOrElse(book -> bookService.deleteBookById(id), 
                        () -> { throw new IllegalArgumentException("Book not found");});
        return "redirect:/admin/books";
    }

    // Thêm vào giỏ hàng (USER)
    @PostMapping("/books/add-to-cart")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String addToCart(HttpSession session, @RequestParam Long id, @RequestParam String name, @RequestParam Double price, @RequestParam(defaultValue = "1") int quantity, @RequestParam(required = false) String imageUrl) {
        var cart = cartService.getCart(session);
        Item item = new Item();
        item.setBookId(id);
        item.setBookName(name);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setImageUrl(imageUrl);
        cart.addItem(item);
        cartService.updateCart(session, cart);
        
        // Cập nhật số lượng vào session để hiển thị trên navbar
        session.setAttribute("cartCount", cartService.getTotalQuantity(session));
        
        return "redirect:/books";
    }

    // Tìm kiếm cũng cho phép public
@GetMapping("/books/search")
// Bỏ: @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
public String searchBooksUser(@NotNull Model model, @RequestParam String keyword, 
                            @RequestParam(defaultValue = "0") Integer pageNo) {
    List<Book> searchResults = bookService.searchBooks(keyword);
    int pageSize = 9;
    model.addAttribute("books", searchResults);
    model.addAttribute("currentPage", pageNo);
    model.addAttribute("totalPages", (int) Math.ceil((double) searchResults.size() / pageSize));
    model.addAttribute("keyword", keyword);
    return "book/user-list";
}
    
    // Tìm kiếm cho ADMIN
    @GetMapping("/admin/books/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String searchBooksAdmin(@NotNull Model model, @RequestParam String keyword, 
                                @RequestParam(defaultValue = "0") Integer pageNo) {
        List<Book> searchResults = bookService.searchBooks(keyword);
        int pageSize = 10;
        model.addAttribute("books", searchResults);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", (int) Math.ceil((double) searchResults.size() / pageSize));
        model.addAttribute("keyword", keyword);
        return "book/admin-list";
    }
}