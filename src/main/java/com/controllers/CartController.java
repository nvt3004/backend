package com.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.repositories.CouponJPA;
import com.responsedto.SaleProductDTO;
import com.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.entities.Cart;
import com.entities.CartProduct;
import com.entities.Coupon;
import com.entities.Order;
import com.entities.OrderDetail;
import com.entities.OrderStatus;
import com.entities.Payment;
import com.entities.PaymentMethod;
import com.entities.ProductVersion;
import com.entities.User;
import com.entities.UserCoupon;
import com.errors.ResponseAPI;
import com.models.CartItemModel;
import com.models.CartOrderDetailModel;
import com.models.CartOrderModel;
import com.models.ProductCartModel;
import com.repositories.CartProductJPA;
import com.repositories.UserCouponJPA;
import com.responsedto.CartItemResponse;
import com.responsedto.CartOrderResponse;

@RestController
@RequestMapping("api/user/cart")
public class CartController {
	@Autowired
	AuthService authService;

	@Autowired
	JWTService jwtService;

	@Autowired
	ProductService productService;

	@Autowired
	UserService userService;

	@Autowired
	ProductVersionService versionService;

	@Autowired
	CartProductService cartProductService;

	@Autowired
	CartService cartService;

	@Autowired
	OrderService orderService;

	@Autowired
	OrderDetailService orderDetailService;

	@Autowired
	PaymentService paymentService;

	@Autowired
	PaymentMethodService paymentMethodService;

	@Autowired
	CouponService couponService;

	@Autowired
	UserCouponService userCouponService;

	@Autowired
	CartProductJPA cartProductJPA;

	@Autowired
	UserCouponJPA userCouponJPA;

	@Autowired
	VersionService vsService;

	@Autowired
	SaleService saleService;
    @Autowired
    private CouponJPA couponJPA;

	// @RequestHeader("Authorization") Optional<String> authHeader
	@PostMapping("/add")
	public ResponseEntity<ResponseAPI<Boolean>> addCart(@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestBody ProductCartModel productCartModel) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		if (productCartModel.getQuantity() <= 0) {
			response.setCode(999);
			response.setMessage("Số lượng không hợp lệ");

			return ResponseEntity.status(999).body(response);
		}

		ProductVersion version = versionService.getProductVersionById(productCartModel.getVersionId());
// False: nếu sản phẩm gốc bị xóa hoặc phiên bản sản phẩm này không tồn tại
		if (!versionService.isValidProductVersion(version)) {
			response.setCode(404);
			response.setMessage("Không tìm thấy sản phẩm");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		int stockQuantity = vsService.getTotalStockQuantityVersion(version.getId());

		if (stockQuantity <= 0) {
			response.setCode(999);
			response.setMessage("Sản phẩm đã hết hàng");

			return ResponseEntity.status(999).body(response);
		}

		if (productCartModel.getQuantity() > stockQuantity) {
			response.setCode(999);
			response.setMessage("Sản phẩm hiện chỉ còn " + stockQuantity + " phiên bản trong kho");

			return ResponseEntity.status(999).body(response);
		}


		Cart cartEntity = new Cart();
		cartEntity.setUser(user);

		Cart cart = cartService.addCart(cartEntity);

		CartProduct cartProductEntity = new CartProduct();
		cartProductEntity.setCart(cart);
		cartProductEntity.setProductVersionBean(version);
		cartProductEntity.setQuantity(productCartModel.getQuantity());
		cartProductEntity.setAddedDate(new Date());

		cartProductService.addProductToCart(cartProductEntity);
		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/all")
	public ResponseEntity<ResponseAPI<List<CartItemResponse>>> getAllCart(
			@RequestHeader("Authorization") Optional<String> authHeader) {

		String token = authService.readTokenFromHeader(authHeader);
		ResponseAPI<List<CartItemResponse>> response = new ResponseAPI<>();

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		List<CartItemResponse> items = cartService.getAllCartItemByUser(user.getUserId());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(items);

		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/remove/{cartItemId}")
	public ResponseEntity<ResponseAPI<Boolean>> removeCartItem(
			@RequestHeader("Authorization") Optional<String> authHeader,
			@PathVariable("cartItemId") Optional<Integer> cartItemId) {

		String token = authService.readTokenFromHeader(authHeader);
		ResponseAPI<Boolean> response = new ResponseAPI<>();

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		if (!cartItemId.isPresent()) {
			response.setCode(400);
			response.setMessage("ID không được để trống");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		CartProduct cartItem = cartProductService.getCartItemById(cartItemId.get());
		if (cartItem == null) {
			response.setCode(404);
			response.setMessage("Không tìm thấy sản phẩm trong giỏ hàng");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!cartProductService.isValidItem(user, cartItem.getCartPrdId())) {
			response.setCode(404);
			response.setMessage("Không tìm thấy sản phẩm trong giỏ hàng");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		boolean isRemoveSuccess = cartProductService.removeCartItem(cartItem);

		if (!isRemoveSuccess) {
			response.setCode(500);
			response.setMessage("Lỗi hệ thống, xóa sản phẩm không thành công");
			response.setData(isRemoveSuccess);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}


		response.setCode(200);
		response.setMessage("Success");
		response.setData(isRemoveSuccess);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/checkout")
	public ResponseEntity<ResponseAPI<CartOrderResponse>> checkout(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody CartOrderModel orderModel) {
		ResponseAPI<CartOrderResponse> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(403);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(403).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(999);
			response.setMessage("Phiên đăng nhập đã hết hạn");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(403);
			response.setMessage("Account not found");

			return ResponseEntity.status(403).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(403).body(response);
		}

		if(orderModel.getFee().compareTo(BigDecimal.ZERO) < 0) {
			response.setCode(999);
			response.setMessage("Phí vận chuyển không hợp lệ");

			return ResponseEntity.status(999).body(response);
		}

		ResponseAPI<Boolean> validOrder = validDataOrder(orderModel);
		if (!validOrder.getData()) {
			response.setCode(999);
			response.setMessage(validOrder.getMessage());

			return ResponseEntity.status(999).body(response);
		}

		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			ProductVersion version = versionService.getProductVersionById(detail.getIdVersion());

			if (detail.getIdVersion() == null) {
				response.setCode(999);
				response.setMessage("ID sản phẩm không được để trống");

				return ResponseEntity.status(999).body(response);
			}

			if (detail.getQuantity() <= 0) {
				response.setCode(999);
				response.setMessage("Số lượng phải lớn hơn 0");

				return ResponseEntity.status(999).body(response);
			}

			if (version == null) {
				response.setCode(999);
				response.setMessage(String.format("Sản phẩm với ID %s không tồn tại", detail.getIdVersion()));

				return ResponseEntity.status(999).body(response);
			}

			if (!version.getProduct().isStatus() || !version.isStatus()) {
				response.setCode(999);
				response.setMessage(String.format("Sản phẩm với ID %s không tồn tại", detail.getIdVersion()));

				return ResponseEntity.status(999).body(response);
			}

			int stockQuantity = vsService.getTotalStockQuantityVersion(version.getId());

			if (stockQuantity <= 0) {
				response.setCode(999);
				response.setMessage("Sản phẩm đã hết hàng trong kho");

				return ResponseEntity.status(999).body(response);
			}

			if (detail.getQuantity() > stockQuantity) {
				response.setCode(999);
				response.setMessage("Sản phẩm với ID " + detail.getIdVersion() + " hiện chỉ còn "
						+ stockQuantity + " phiên bản trong kho");

				return ResponseEntity.status(999).body(response);
			}
		}

		Coupon coupon = couponJPA.getCouponByCode(orderModel.getCouponCode());
		if (coupon == null && orderModel.getCouponCode() != null && !orderModel.getCouponCode().isBlank()) {
			response.setCode(999);
			response.setMessage("Mã giảm giá không tồn tại");

			return ResponseEntity.status(999).body(response);
		}

		Order orderEntity = new Order();
		OrderStatus status = new OrderStatus();
		status.setStatusId(1);

		orderEntity.setAddress(orderModel.getAddress());
		if (coupon != null) {
			orderEntity.setCoupon(coupon);
			if (coupon.getDisPercent() != null) {
				orderEntity.setDisPercent(coupon.getDisPercent());
			} else {
				orderEntity.setDisPrice(coupon.getDisPrice());
			}
		}

		LocalDateTime localDateTime = LocalDateTime.now();

		// Chuyển LocalDateTime sang múi giờ UTC+7 (Việt Nam)
		ZonedDateTime vietnamTime = localDateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh"));

		// Trừ đi 8 giờ
		ZonedDateTime adjustedTime = vietnamTime.minusHours(8);

		// Chuyển về java.util.Date
		Date date = Date.from(adjustedTime.toInstant());

		// Gán vào orderEntity
		orderEntity.setOrderDate(new Date());
		orderEntity.setDeliveryDate(orderModel.getLeadTime());
		orderEntity.setUser(user);
		orderEntity.setFullname(user.getFullName());
		orderEntity.setPhone(user.getPhone());
		orderEntity.setOrderStatus(status);
		orderEntity.setCoupon(coupon);
		orderEntity.setShippingFee(orderModel.getFee());

		// Thay quyền lớn nhất của user vào
		orderEntity.setIsCreator(false);

		// Save order
		Order orderSaved = orderService.createOrderCart(orderEntity);

		if (coupon != null) {
			UserCoupon temp = userCouponJPA.findUsercouponByCoupon(coupon.getCouponId(), user.getUserId());

			if (temp != null) {
				temp.setStatus(false);
				userCouponService.createUserCoupon(temp);
			} else {
				UserCoupon userCoupon = new UserCoupon();
				userCoupon.setUser(user);
				userCoupon.setCoupon(coupon);
				userCoupon.setStatus(false);

				userCouponService.createUserCoupon(userCoupon);
			}
		}

		// Save order details
		int totalProduct = 0;
		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			OrderDetail orderDetailEntity = new OrderDetail();
			ProductVersion product = versionService.getProductVersionById(detail.getIdVersion());
			SaleProductDTO productDTO = saleService.getVersionSaleDTO(detail.getIdVersion());
			product.setId(detail.getIdVersion());

			totalProduct += detail.getQuantity();

			orderDetailEntity.setOrder(orderSaved);
			orderDetailEntity.setProductVersionBean(product);
			orderDetailEntity.setQuantity(detail.getQuantity());
			orderDetailEntity.setPrice(productDTO == null?product.getRetailPrice(): productDTO.getPrice());

			orderDetailService.createOrderDetail(orderDetailEntity);
		}

		// save payment
		Payment paymentEntity = new Payment();
		PaymentMethod paymentMethod = new PaymentMethod();
		// Than toán khi nhận hàng
		paymentMethod.setPaymentMethodId(2);

		paymentEntity.setOrder(orderSaved);
		paymentEntity.setPaymentDate(new Date());
		paymentEntity.setPaymentMethod(paymentMethod);
		paymentEntity.setAmount(BigDecimal.ZERO);

		Payment paymentSaved = paymentService.createPayment(paymentEntity);

		// Mua xong xóa khỏi giỏ hàng
		for (CartProduct crd : user.getCarts().get(0).getCartProducts()) {
			for (CartOrderDetailModel md : orderModel.getOrderDetails()) {
				if (crd.getProductVersionBean().getId() == md.getIdVersion()) {
					cartProductService.removeCartItem(crd);
					break;
				}
			}
		}

		// Respone result
		CartOrderResponse orderResponse = new CartOrderResponse();

		orderResponse.setAddress(orderSaved.getAddress());
		orderResponse.setCouponCode(orderSaved.getCoupon() != null ? orderSaved.getCoupon().getCouponCode() : null);
		orderResponse.setDeliveryDate(orderSaved.getDeliveryDate());
		orderResponse.setDisPercent(orderSaved.getDisPercent());
		orderResponse.setDisPrice(orderSaved.getDisPrice());
		orderResponse.setFullname(user.getFullName());
		orderResponse.setOrderDate(orderSaved.getOrderDate());
		orderResponse.setOrderId(orderSaved.getOrderId());
		orderResponse.setPaymentName(paymentSaved.getPaymentMethod().getMethodName());
		orderResponse.setPhone(user.getPhone());
		orderResponse.setStatusOrderName(orderSaved.getOrderStatus().getStatusName());
		orderResponse.setTotalProduct(totalProduct);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(orderResponse);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/update")
	public ResponseEntity<ResponseAPI<CartItemModel>> updateCatrt(@RequestBody CartItemModel cartItemModel,
			@RequestHeader("Authorization") Optional<String> authHeader) {
		ResponseAPI<CartItemModel> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		if (cartItemModel.getQuantity() <= 0) {
			response.setCode(422);
			response.setMessage("Quantity cannot be negative or zero");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		CartProduct cartItem = cartProductService.getCartItemById(cartItemModel.getCartItemId());
		if (cartItem == null) {
			response.setCode(404);
			response.setMessage("Cart item not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!cartProductService.isValidItem(user, cartItem.getCartPrdId())) {
			response.setCode(404);
			response.setMessage("Cart item not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		ProductVersion version = versionService.getProductVersionById(cartItem.getProductVersionBean().getId());
		// False: nếu sản phẩm gốc bị xóa hoặc phiên bản sản phẩm này không tồn tại
		if (version == null) {
			response.setCode(404);
			response.setMessage("Products version not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!version.isStatus() && !version.getProduct().isStatus()) {
			response.setCode(404);
			response.setMessage("Products version not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		int stockQuantity = vsService.getTotalStockQuantityVersion(cartItem.getProductVersionBean().getId());

		if (stockQuantity <= 0) {
			response.setCode(999);
			response.setMessage("Products that exceed the quantity in stock");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (cartItemModel.getQuantity() > stockQuantity) {
			response.setCode(999);
			response.setMessage("The product currently has only " + stockQuantity + " versions left");
			cartItem.setQuantity(stockQuantity);
			cartProductService.updateCartItem(cartItem);

			return ResponseEntity.status(999).body(response);
		}

		cartItem.setQuantity(cartItemModel.getQuantity());
		cartProductService.updateCartItem(cartItem);

		response.setCode(200);
		response.setMessage("Success");
		response.setData(cartItemModel);

		return ResponseEntity.ok(response);
	}

	@PutMapping("/update-item")
	public ResponseEntity<ResponseAPI<CartItemModel>> updateCatrtItem(@RequestBody CartItemModel cartItemModel,
			@RequestHeader("Authorization") Optional<String> authHeader) {
		ResponseAPI<CartItemModel> response = new ResponseAPI<>();
		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			response.setCode(400);
			response.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (jwtService.isTokenExpired(token)) {
			response.setCode(401);
			response.setMessage("Token expired");

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		String username = jwtService.extractUsername(token);
		User user = userService.getUserByUsername(username);
		if (user == null) {
			response.setCode(404);
			response.setMessage("Account not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (user.getStatus() == 0) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		CartProduct cartItem = cartProductService.getCartItemById(cartItemModel.getCartItemId());
		if (cartItem == null) {
			response.setCode(404);
			response.setMessage("Cart item not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		if (!cartProductService.isValidItem(user, cartItem.getCartPrdId())) {
			response.setCode(404);
			response.setMessage("Cart item not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		ProductVersion versionCheck = versionService.getProductVersionById(cartItemModel.getVersionId());
		// False: nếu sản phẩm gốc bị xóa hoặc phiên bản sản phẩm này không tồn tại
		if (!versionService.isValidProductVersion(versionCheck)) {
			response.setCode(404);
			response.setMessage("Products version not found");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		int stockQuantity = vsService.getTotalStockQuantityVersion(versionCheck.getId());

		if (stockQuantity <= 0) {
			response.setCode(422);
			response.setMessage("Products that exceed the quantity in stock");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		if (cartItem.getQuantity() > stockQuantity) {
			response.setCode(422);
			response.setMessage("The product currently has only " + stockQuantity + " versions left");

			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
		}

		ProductVersion version = new ProductVersion();
		version.setId(cartItemModel.getVersionId());

		CartProduct cartProductTemp = cartProductJPA.getVersionInCartByUserUp(cartItemModel.getVersionId(),
				user.getUserId(), cartItemModel.getCartItemId());

		if (cartProductTemp != null) {
			cartProductTemp.setQuantity(cartProductTemp.getQuantity() + cartItem.getQuantity());
			CartProduct saved = cartProductService.updateCartItem(cartProductTemp);

			cartProductService.removeCartItem(cartItem);

			cartItemModel.setQuantity(saved.getQuantity());

			response.setCode(200);
			response.setMessage("Success");
			response.setData(cartItemModel);

			return ResponseEntity.ok(response);
		}

		cartItem.setProductVersionBean(version);
		cartProductService.updateCartItem(cartItem);

		cartItemModel.setQuantity(cartItem.getQuantity());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(cartItemModel);

		return ResponseEntity.ok(response);
	}

	private ResponseAPI<Boolean> validDataOrder(CartOrderModel order) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setCode(422);
		response.setData(false);

		final BigDecimal zero = new BigDecimal(0.0);
		final BigDecimal limitDispercent = new BigDecimal(0.7);

		if (order.getAddress() == null) {
			response.setMessage("Địa chỉ không được để trống");
			return response;
		}

		if (order.getAddress().trim().length() == 0) {
			response.setMessage("Địa chỉ không được để trống");
			return response;
		}

		if (order.getPaymentMethodId() == null) {
			response.setMessage("Phương thức thanh toán không được để trống");
			return response;
		}

		if (paymentMethodService.getPaymentMethodById(order.getPaymentMethodId()) == null) {
			response.setCode(404);
			response.setMessage("Phương thức thanh toán không tồn tại");
			return response;
		}

		if (order.getOrderDetails() == null) {
			response.setMessage("Chi tiết đơn hàng không được để trống");
			return response;
		}

		if (order.getOrderDetails().size() <= 0) {
			response.setMessage("Chi tiết đơn hàng không được để trống");
			return response;
		}

		if (order.getCouponCode() != null) {
			Coupon coupon = couponJPA.getCouponByCode(order.getCouponCode());

			if (coupon == null) {
				response.setCode(404);
				response.setMessage("Mã giảm giá không tồn tại");
				return response;
			}
		}

		response.setCode(200);
		response.setMessage("Thành công");
		response.setData(true);

		return response;
	}


}
