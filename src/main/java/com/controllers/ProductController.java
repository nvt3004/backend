package com.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.AttributeOptionsVersion;
import com.entities.Category;
import com.entities.Product;
import com.entities.User;
import com.errors.ResponseAPI;
import com.models.CategoryDTO;
import com.models.OptionDTO;
import com.models.VersionDTO;
import com.responsedto.ProductResponse;
import com.services.AttributesOptionVersionService;
import com.services.AuthService;
import com.services.CategoryService;
import com.services.JWTService;
import com.services.ProductService;
import com.services.ProductVersionService;
import com.services.UserService;
import com.utils.UploadService;

@RestController
@RequestMapping("/api/staff/product")
public class ProductController {

    @Autowired
    AuthService authService;

    @Autowired
    JWTService jwtService;

    @Autowired
    ProductService productService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    UserService userService;

    @Autowired
    ProductVersionService versionService;

    @Autowired
    UploadService uploadService;

    @Autowired
    AttributesOptionVersionService attributeOptionVersionService;

    @PostMapping("/add")
	@PreAuthorize("hasPermission(#userId, 'Add Product')")
    public ResponseEntity<ResponseAPI<Boolean>> addProduct(@RequestHeader("Authorization") Optional<String> authHeader,
            @RequestBody com.models.ProductDTO productModel) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // ResponseAPI<Boolean> responseValid = validProduct(productModel);
        // if (!responseValid.getData()) {
        // response.setCode(responseValid.getCode());
        // response.setMessage(responseValid.getMessage());
        // return ResponseEntity.status(responseValid.getCode()).body(response);
        // }
        Product productSaved = productService.createProduct(productModel);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    @PreAuthorize("hasPermission(#userId, 'Update Product')")
    public ResponseEntity<ResponseAPI<Boolean>> updateProduct(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestBody com.models.ProductDTO productModel) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Product temp = productService.getProductById(productModel.getId());

        if (temp == null) {
            response.setCode(404);
            response.setMessage("Sản phẩm không tồn tại");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        productService.updateProduct(productModel, temp);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    @GetMapping
	@PreAuthorize("hasPermission(#userId, 'View Product')")
    public ResponseEntity<ResponseAPI<PageImpl<ProductResponse>>> getAllProduct(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam(value = "keyword", defaultValue = "") String keywword,
            @RequestParam(value = "idCat", defaultValue = "-1") int idCat,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        boolean statusProduct = true;
        ResponseAPI<PageImpl<ProductResponse>> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        PageImpl<ProductResponse> data;

        if (idCat == -1) {
            data = productService.getProductsByKeyword(page - 1, size, statusProduct, "%" + keywword + "%");
        } else {
            data = productService.getProductsByKeywordAndCategory(page - 1, size, idCat, statusProduct,
                    "%" + keywword + "%");
        }

        response.setCode(200);
        response.setMessage("Success");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasPermission(#userId, 'View Product')")
    public ResponseEntity<ResponseAPI<List<ProductResponse>>> getAllProductNotPage(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam(value = "keyword", defaultValue = "") String keywword) {
        boolean statusProduct = true;
        ResponseAPI<List<ProductResponse>> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        keywword = "%"+keywword+"%";
        List<ProductResponse>  data = productService.getAllProductsByKeyword(2,3,true,keywword);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-sale-update")
    @PreAuthorize("hasPermission(#userId, 'View Product')")
    public ResponseEntity<ResponseAPI<List<ProductResponse>>> getAllProductNotPageForSaleUpdate(
            @RequestHeader("Authorization") Optional<String> authHeader,
            @RequestParam(value = "keyword", defaultValue = "") String keywword,
            @RequestParam(value = "saleId", defaultValue = "-1") Integer saleId) {
        boolean statusProduct = true;
        ResponseAPI<List<ProductResponse>> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        keywword = "%"+keywword+"%";
        List<ProductResponse>  data = productService.getAllProductsByKeywordSaleUpdate(2,3,true,keywword, saleId);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(data);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{id}")
    @PreAuthorize("hasPermission(#userId, 'Delete Product')")
    public ResponseEntity<ResponseAPI<Boolean>> removeProduct(
            @RequestHeader("Authorization") Optional<String> authHeader, @PathVariable("id") Integer idProduct) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        response.setData(false);
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (productService.getProductById(idProduct) == null) {
            response.setCode(404);
            response.setMessage("Không tìm thấy sản phẩm");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        productService.removeProduct(idProduct);

        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return ResponseEntity.ok(response);
    }

    public ResponseAPI<Boolean> validProduct(com.models.ProductDTO product) {
        ResponseAPI<Boolean> response = new ResponseAPI<>();
        BigDecimal oneThousand = new BigDecimal(1000);
        BigDecimal fiveHundred = new BigDecimal(500);
        response.setData(false);

        if (product.getName() == null || product.getName().isBlank() || product.getName().isEmpty()) {
            response.setCode(422);
            response.setMessage("Tên sản phẩm không được để trống");

            return response;
        }

        if (product.getPrice().compareTo(oneThousand) < 0) {
            response.setCode(422);
            response.setMessage("Giá sản phẩm phải từ 1000 trở lên");

            return response;
        }

        if (product.getImage() == null || product.getImage().isBlank() || product.getImage().isEmpty()) {
            response.setCode(422);
            response.setMessage("Vui lòng tải lên ảnh");

            return response;
        }

        if (product.getCategories() == null) {
            response.setCode(400);
            response.setMessage("Vui lòng chọn loại sản phẩm");

            return response;
        }

        if (product.getCategories().isEmpty()) {
            response.setCode(400);
            response.setMessage("Vui lòng chọn loại sản phẩm");

            return response;
        }

        for (CategoryDTO cat : product.getCategories()) {
            Category category = categoryService.getCategoryById(cat.getId());

            if (category == null) {
                response.setCode(404);
                response.setMessage(String.format("Không tìm thấy loại sản phẩm có id là %d", cat.getId()));

                return response;
            }
        }

        for (VersionDTO vs : product.getVersions()) {

            if (vs.getVersionName() == null || vs.getVersionName().isBlank() || vs.getVersionName().isEmpty()) {
                response.setCode(422);
                response.setMessage("Không được để trống tên phiên bản");

                return response;
            }

            if (vs.getImportPrice().compareTo(fiveHundred) < 0) {
                response.setCode(422);
                response.setMessage("Giá nhập phiên bản phải từ 500 trở lên");

                return response;
            }


            if (vs.getAttributes() == null) {
                response.setCode(400);
                response.setMessage("Vui lòng chọn thuộc tính sản phẩm");

                return response;
            }

            if (vs.getAttributes().isEmpty()) {
                response.setCode(400);
                response.setMessage("Vui lòng chọn thuộc tính sản phẩm");

                return response;
            }

            int idAttributeError = validAttribute(vs.getAttributes());
            if (idAttributeError != -1) {
                response.setCode(404);
                response.setMessage(String.format("Không tìm thấy thuộc tính có id là %d", idAttributeError));

                return response;
            }

        }

        response.setCode(200);
        response.setMessage("Success");
        response.setData(true);

        return response;
    }

    private int validAttribute(List<OptionDTO> attributes) {
        for (OptionDTO op : attributes) {
            AttributeOptionsVersion attributeOptionVersion = attributeOptionVersionService.getById(op.getId());

            if (attributeOptionVersion == null) {
                return op.getId();
            }
        }

        return -1;
    }

    @GetMapping("/refresh/{id}")
	@PreAuthorize("hasPermission(#userId, 'View Product')")
    public ResponseEntity<ResponseAPI<ProductResponse>> refreshProduct(
            @RequestHeader("Authorization") Optional<String> authHeader, @PathVariable("id") Integer idProduct) {
        ResponseAPI<ProductResponse> response = new ResponseAPI<>();
        String token = authService.readTokenFromHeader(authHeader);

        try {
            jwtService.extractUsername(token);
        } catch (Exception e) {
            response.setCode(400);
            response.setMessage("Định dạng mã token không hợp lệ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (jwtService.isTokenExpired(token)) {
            response.setCode(401);
            response.setMessage("Phiên đăng nhập đã hết hạn");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            response.setCode(403);
            response.setMessage("Account not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (user.getStatus() == 0) {
            response.setCode(403);
            response.setMessage("Account locked");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        ProductResponse data = productService.refreshSelectedProduct(idProduct);
        if (data == null) {
            response.setCode(404);
            response.setMessage("Không tìm thấy sản phẩm");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.setCode(200);
        response.setMessage("Success");
        response.setData(data);

        return ResponseEntity.ok(response);

    }
}
