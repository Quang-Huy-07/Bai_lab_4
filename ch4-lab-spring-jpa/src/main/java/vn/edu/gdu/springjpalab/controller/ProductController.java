package vn.edu.gdu.springjpalab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.gdu.springjpalab.entity.Product;
import vn.edu.gdu.springjpalab.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    // Constructor Injection (tiêm phụ thuộc qua Constructor)
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    // ── 1. API lấy danh sách tất cả sản phẩm ──
    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    // ── 2. API lấy sản phẩm theo ID ──
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // ── 3. API thêm mới sản phẩm ──
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
    // ── 4. API cập nhật sản phẩm ──
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setSku(productDetails.getSku());
            Product updatedProduct = productRepository.save(product);
            return ResponseEntity.ok(updatedProduct);
        }
        return ResponseEntity.notFound().build();
    }
    // ── 5. API xóa sản phẩm ──
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    // ── 6. API đếm tổng số sản phẩm ──
    @GetMapping("/count")
    public long countProducts() {
        return productRepository.count();
    }
}