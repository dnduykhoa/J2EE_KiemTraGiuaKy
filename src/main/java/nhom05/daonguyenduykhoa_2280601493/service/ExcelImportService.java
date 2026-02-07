package nhom05.daonguyenduykhoa_2280601493.service;

import nhom05.daonguyenduykhoa_2280601493.model.Book;
import nhom05.daonguyenduykhoa_2280601493.model.Category;
import nhom05.daonguyenduykhoa_2280601493.repository.CategoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class ExcelImportService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Import books from Excel file
     * Expected format:
     * Column 0: Title (String)
     * Column 1: Author (String)
     * Column 2: Price (Double)
     * Column 3: Category Name (String)
     * Column 4: Image URL (String) - Optional
     */
    public List<Book> importBooksFromExcel(MultipartFile file) throws IOException {
        List<Book> books = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            
            // Find header row (look for "Tên Sách" or "Title")
            int headerRowIndex = -1;
            int rowNumber = 0;
            while (rows.hasNext() && headerRowIndex == -1) {
                Row currentRow = rows.next();
                Cell firstCell = currentRow.getCell(0);
                if (firstCell != null) {
                    String cellValue = getCellValueAsString(firstCell);
                    if (cellValue.contains("Tên Sách") || cellValue.contains("Title")) {
                        headerRowIndex = rowNumber;
                        break;
                    }
                }
                rowNumber++;
            }
            
            if (headerRowIndex == -1) {
                throw new IOException("Không tìm thấy dòng tiêu đề. Vui lòng sử dụng file mẫu.");
            }
            
            // Skip to data rows (skip header and any note rows)
            rowNumber = headerRowIndex + 1;
            
            // Skip note row if exists (check for "BẮT ĐẦU NHẬP")
            if (rows.hasNext()) {
                Row nextRow = rows.next();
                Cell firstCell = nextRow.getCell(0);
                if (firstCell != null && getCellValueAsString(firstCell).contains("BẮT ĐẦU NHẬP")) {
                    rowNumber++;
                } else {
                    // Reset iterator to this row since it's data
                    rowNumber++;
                }
            }
            
            // Process data rows
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNumber++;
                
                try {
                    // Check if row is empty
                    Cell firstCell = currentRow.getCell(0);
                    if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
                        continue; // Skip empty rows
                    }
                    
                    Book book = new Book();
                    
                    // Title (Column 0)
                    Cell titleCell = currentRow.getCell(0);
                    if (titleCell == null || titleCell.getCellType() == CellType.BLANK) {
                        System.out.println("Skipping row " + rowNumber + ": Missing title");
                        continue;
                    }
                    book.setTitle(getCellValueAsString(titleCell));
                    
                    // Author (Column 1)
                    Cell authorCell = currentRow.getCell(1);
                    if (authorCell == null || authorCell.getCellType() == CellType.BLANK) {
                        System.out.println("Skipping row " + rowNumber + ": Missing author");
                        continue;
                    }
                    book.setAuthor(getCellValueAsString(authorCell));
                    
                    // Price (Column 2)
                    Cell priceCell = currentRow.getCell(2);
                    if (priceCell == null || priceCell.getCellType() == CellType.BLANK) {
                        System.out.println("Skipping row " + rowNumber + ": Missing price");
                        continue;
                    }
                    book.setPrice(getCellValueAsDouble(priceCell));
                    
                    // Category (Column 3)
                    Cell categoryCell = currentRow.getCell(3);
                    if (categoryCell == null || categoryCell.getCellType() == CellType.BLANK) {
                        System.out.println("Skipping row " + rowNumber + ": Missing category");
                        continue;
                    }
                    String categoryName = getCellValueAsString(categoryCell);
                    Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);
                    if (categoryOpt.isEmpty()) {
                        System.out.println("Skipping row " + rowNumber + ": Category not found: " + categoryName);
                        continue;
                    }
                    book.setCategory(categoryOpt.get());
                    
                    // Image URL (Column 4) - Optional
                    Cell imageUrlCell = currentRow.getCell(4);
                    if (imageUrlCell != null && imageUrlCell.getCellType() != CellType.BLANK) {
                        String imageUrl = getCellValueAsString(imageUrlCell);
                        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                            book.setImageUrl(imageUrl);
                        } else {
                            // Nếu để trống, dùng ảnh mặc định
                            book.setImageUrl("/images/books/default-book.svg");
                        }
                    } else {
                        // Nếu không có cột ảnh, dùng ảnh mặc định
                        book.setImageUrl("/images/books/default-book.svg");
                    }
                    
                    books.add(book);
                    
                } catch (Exception e) {
                    System.out.println("Error processing row " + rowNumber + ": " + e.getMessage());
                    // Continue with next row
                }
            }
        }
        
        return books;
    }
    
    /**
     * Get cell value as String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert numeric to string without scientific notation
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
    
    /**
     * Get cell value as Double
     */
    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot convert string to number: " + cell.getStringCellValue());
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (IllegalStateException e) {
                    String stringValue = cell.getStringCellValue().trim();
                    return Double.parseDouble(stringValue);
                }
            default:
                throw new IllegalArgumentException("Cannot convert cell type to number: " + cell.getCellType());
        }
    }
}
