package vn.edu.gdu.springjpalab.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity // (1) Đánh dấu đây là Entity JPA
@Table(name = "products") // (2) Ánh xạ đến bảng "products" trong DB
public class Product {
    @Id // (3) Đánh dấu trường này là khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // (4) Tự động tăng
    private Long id;
    @Column(name = "product_name", nullable = false, length = 150)
    private String name; // (5) Ánh xạ đến cột "product_name"
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // (6) Kiểu tiền tệ dùng BigDecimal
    @Column(name = "sku", unique = true, nullable = false, length = 50)
    private String sku; // (7) Mã sản phẩm - duy nhất
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    // ── 1. Constructor không tham số (BẮT BUỘC cho JPA) ──
    protected Product() {
    }
    // ── 2. Constructor có tham số (tiện lợi cho lập trình viên) ──
    public Product(String name, BigDecimal price, String sku) {
        this.name = name;
        this.price = price;
        this.sku = sku;
        this.createdAt = LocalDateTime.now();
    }
    // ── 3. Getter và Setter ──
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getSku() {
        return sku;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price +
                ", sku='" + sku + "', createdAt=" + createdAt + "}";
    }
}