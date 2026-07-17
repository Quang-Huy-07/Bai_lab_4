## 1. Phân biệt JDBC và ORM: 3 hạn chế cốt lõi của JDBC truyền thống

### Hạn chế 1: Lặp lại quá nhiều mã nguồn (Boilerplate Code)
* **Giải thích:** JDBC yêu cầu viết thủ công rất nhiều mã thiết lập như: mở/đóng kết nối, tạo Statement, đóng ResultSet và xử lý ngoại lệ `SQLException`.
* **Ví dụ:** Để lấy một danh sách sản phẩm, JDBC cần khoảng 15-20 dòng mã lặp đi lặp lại cho các bước đóng mở resource. Trong khi đó, ORM (như Hibernate) rút gọn chỉ còn 1 dòng lệnh duy nhất (`session.list()` hoặc hàm kế thừa từ Repository).

### Hạn chế 2: Ánh xạ dữ liệu thủ công (Manual Object-Relational Mapping)
* **Giải thích:** JDBC không tự chuyển đổi dữ liệu từ bảng thành đối tượng Java. Lập trình viên phải tự đọc từng cột từ cơ sở dữ liệu (`ResultSet`) rồi gán thủ công vào đối tượng qua các hàm `getXxx()`, `setXxx()`.
* **Ví dụ:** 
  ```java
  Product p = new Product();
  p.setId(rs.getLong("id"));
  p.setName(rs.getString("name"));
  ```
  ORM tự động thực hiện quá trình ánh xạ (mapping) toàn bộ hàng dữ liệu thành đối tượng Java tương ứng một cách tự động.

### Hạn chế 3: Phụ thuộc vào hệ quản trị CSDL cụ thể (SQL Dialect)
* **Giải thích:** Câu lệnh SQL viết trong JDBC thường dính chặt với cú pháp riêng của một hệ CSDL (như MySQL, Oracle). Nếu chuyển dự án sang CSDL khác (ví dụ: chuyển từ MySQL dùng `LIMIT` sang SQL Server dùng `OFFSET-FETCH` để phân trang), mã nguồn sẽ bị lỗi và phải sửa đổi thủ công hàng loạt.
* **Ví dụ:** Với JDBC, lập trình viên phải tự viết các câu lệnh SQL tĩnh cố định. Với ORM, hệ thống tự động sinh câu lệnh SQL tương thích dựa trên cấu hình thuộc tính `dialect` của hệ CSDL đích mà không cần sửa bất kỳ dòng mã Java nào.

## 2. Kiến trúc phân tầng: Quan hệ giữa Spring Data JPA, JPA, Hibernate và JDBC

### Sơ đồ phân tầng kiến trúc:

    [ Spring Data JPA ]  <-- Tầng trừu tượng cao nhất (Cung cấp JpaRepository)
      │
      ▼
    [ JPA ]         <-- Tầng đặc tả(Quy định các Annotation & Interface chuẩn)
      │
      ▼
    [ Hibernate ]       <-- Tầng ORM Engine (Thực thi và chuyển đổi Object sang SQL)
      │
      ▼
    [ JDBC ]         <-- Tầng kết nối thấp nhất (Giao tiếp trực tiếp với CSDL)

* **Spring Data JPA:** Là một thư viện của Spring Framework, cung cấp các interface tiện ích (như `JpaRepository`) giúp rút gọn tối đa việc viết mã CRUD mà không cần viết mã triển khai cụ thể.
* **JPA (Java Persistence API):** Là một **đặc tả (Specification)**, tập hợp các quy định và giao diện chuẩn của Java (như `@Entity`, `@Id`) chứ không chứa mã thực thi trực tiếp.
* **Hibernate:** Là một **ORM Framework** cụ thể, đóng vai trò triển khai (Implementation) cho đặc tả JPA. Nó trực tiếp xử lý chuyển đổi thực thể thành SQL.
* **JDBC:** Là thư viện cốt lõi của Java dùng để kết nối và gửi câu lệnh SQL thuần túy tới CSDL.

### Tại sao Spring Data JPA không thể hoạt động nếu không có Hibernate?
Spring Data JPA bản chất chỉ là một lớp trừu tượng (Abstraction) nằm ở trên cùng để tạo sự tiện lợi cho lập trình viên. Bản thân nó không có khả năng tự dịch chuyển đối tượng thành các bảng hay tự sinh câu lệnh SQL. Nó bắt buộc phải dựa vào một thư viện ORM cụ thể làm "động cơ" bên dưới để thực thi các quy tắc của JPA. Mặc định trong Spring Boot, **Hibernate** chính là công nghệ được chọn để đảm nhận vai trò thực thi này.

## 3. Constructor không tham số trong Entity class

### Tại sao Entity class bắt buộc phải có?
Hibernate sử dụng kỹ thuật **Java Reflection** để khởi tạo các đối tượng Entity một cách tự động khi lấy dữ liệu từ CSDL lên. Lúc này, Hibernate sẽ gọi constructor mặc định (không tham số) để tạo ra vỏ bọc đối tượng trước, sau đó mới dùng các phương thức setter hoặc truy cập trực tiếp để đổ dữ liệu vào các thuộc tính.

### Điều gì xảy ra nếu lập trình viên quên khai báo?
* Nếu lớp không có bất kỳ constructor nào, Java sẽ tự sinh một constructor không tham số mặc định $\rightarrow$ Ứng dụng vẫn chạy bình thường.
* Nếu lập trình viên đã viết một constructor có tham số nhưng **quên khai báo lại constructor không tham số**, Java sẽ không tự sinh constructor mặc định nữa.
* **Hậu quả:** Khi truy vấn dữ liệu, Hibernate sẽ ném ra ngoại lệ `InstantiationException` hoặc `NoSuchMethodException` kèm thông báo lỗi dạng: *“No default constructor for entity”* và ứng dụng sẽ bị sập.
* 
## 4. Chế độ ddl-auto nguy hiểm trong môi trường Production

### Chế độ nguy hiểm nhất:
Chế độ `create`, `create-drop` và **`update`** cực kỳ nguy hiểm khi chạy trên môi trường thực tế (Production).

### Giải thích tại sao:
* `create` và `create-drop`: Sẽ thực thi lệnh `DROP TABLE` để xóa sạch toàn bộ các bảng hiện có mỗi khi ứng dụng khởi động lại, làm **mất trắng dữ liệu** thật của khách hàng.
* `update`: Dù không xóa bảng nhưng nó tự động thay đổi cấu trúc bảng (thêm cột, đổi kiểu dữ liệu). Trên Production, việc tự động sửa đổi cấu trúc này rất dễ làm khóa bảng (lock table), gây treo hệ thống hoặc xung đột dữ liệu ngoài ý muốn.

### Chế độ phù hợp cho Production:
* **`none`** (hoặc loại bỏ hoàn toàn thuộc tính này): Không cho phép Hibernate can thiệp vào cấu trúc CSDL.
* **`validate`**: Chỉ kiểm tra xem cấu trúc bảng dưới CSDL có khớp với Entity class không, nếu không khớp sẽ báo lỗi dừng ứng dụng chứ không tự ý chỉnh sửa.

## 5. Annotation @Column và kiểu dữ liệu tiền tệ

### Ý nghĩa của các thuộc tính trong `@Column`:
* `name`: Chỉ định tên của cột trong bảng CSDL.
* `nullable`: Xác định cột có được phép chứa giá trị `NULL` hay không (`false` là bắt buộc phải có dữ liệu).
* `unique`: Thiết lập giá trị trong cột này phải là duy nhất, không được trùng lặp (ví dụ: email, mã sản phẩm).
* `length`: Định nghĩa độ dài tối đa của chuỗi ký tự (chỉ áp dụng cho kiểu `VARCHAR`).
* `precision`: Tổng số chữ số tối đa của một số thập phân (bao gồm cả phần nguyên và phần thập phân).
* `scale`: Số chữ số tối đa thuộc phần thập phân (nằm sau dấu phẩy).

### Tại sao nên sử dụng BigDecimal thay vì double cho kiểu tiền tệ?
* Kiểu `double` biểu diễn số thập phân bằng hệ nhị phân (theo chuẩn số chấm động IEEE 754) nên **không thể biểu diễn chính xác tuyệt đối** một số giá trị decimal (ví dụ: `0.1 + 0.2` bằng `double` sẽ ra `0.30000000000000004`).
* Trong ngành tài chính, sự sai số dù nhỏ nhất tích lũy qua hàng triệu giao dịch sẽ gây ra thất thoát tiền tệ rất lớn.
* Kiểu `BigDecimal` lưu trữ số dưới dạng số nguyên lớn và hệ số tỷ lệ đi kèm, giúp **đảm bảo độ chính xác tuyệt đối 100%** cho mọi phép toán số học.

## 6. Thực hành bổ sung: Thêm trường description vào Entity Product

### Cập nhật thuộc tính trong Entity:
```java
@Column(name = "description", length = 500, nullable = true)
private String description;
```

### Câu lệnh SQL ALTER TABLE sinh ra trên Console:
Khi chạy ứng dụng ở chế độ `spring.jpa.hibernate.ddl-auto=update`, Hibernate đã tự động quét thay đổi và sinh ra câu lệnh SQL cập nhật cấu trúc bảng như sau:

```sql
ALTER TABLE product ADD COLUMN description VARCHAR(500);
```
