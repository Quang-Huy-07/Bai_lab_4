package vn.edu.gdu.springjpalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.gdu.springjpalab.entity.Product;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
// Spring Data JPA tự động cung cấp các phương thức CRUD:
// - save(entity) : Thêm mới hoặc cập nhật
// - findById(id) : Tìm theo khóa chính
// - findAll() : Lấy tất cả bản ghi
// - deleteById(id) : Xóa theo khóa chính
// - count() : Đếm tổng số bản ghi
// - existsById(id) : Kiểm tra tồn tại
}