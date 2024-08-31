package com.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.configs.ConfigVNPay;
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
import com.models.CartOrderDetailModel;
import com.models.CartOrderModel;
import com.repositories.OrderJPA;
import com.responsedto.CartOrderResponse;
import com.responsedto.VnpayDTO;
import com.services.AuthService;
import com.services.CartProductService;
import com.services.CartService;
import com.services.CouponService;
import com.services.JWTService;
import com.services.OrderDetailService;
import com.services.OrderService;
import com.services.PaymentMethodService;
import com.services.PaymentService;
import com.services.ProductService;
import com.services.ProductVersionService;
import com.services.UserCouponService;
import com.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/vnp/")
public class VnPayController {

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

	@PostMapping("/create-payment")
	public ResponseEntity<ResponseAPI<String>> createPayment(
			@RequestHeader("Authorization") Optional<String> authHeader, @RequestBody CartOrderModel orderModel,
			HttpServletRequest req) throws UnsupportedEncodingException {
		ResponseAPI<String> response = new ResponseAPI<>();
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

		if (!user.isStatus()) {
			response.setCode(403);
			response.setMessage("Account locked");

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		ResponseAPI<CartOrderResponse> order = createOder(orderModel, user);

		if (order.getData() == null) {
			response.setCode(order.getCode());
			response.setMessage(order.getMessage());

			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
		}

		String vnp_Version = "2.1.0";
		String vnp_Command = "pay";
		String orderType = "other";
		long amount = getAmountByOrderDetail(orderModel);
		String bankCode = orderModel.getVnpay().getBankCode();

		// Mã hóa đơn
		String vnp_TxnRef = String.valueOf(order.getData().getOrderId());
		String vnp_IpAddr = ConfigVNPay.getIpAddress(req);

		String vnp_TmnCode = ConfigVNPay.vnp_TmnCode;

		Map<String, String> vnp_Params = new HashMap<>();
		vnp_Params.put("vnp_Version", vnp_Version);
		vnp_Params.put("vnp_Command", vnp_Command);
		vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
		vnp_Params.put("vnp_Amount", String.valueOf(amount));
		vnp_Params.put("vnp_CurrCode", "VND");

		if (bankCode != null && !bankCode.isEmpty()) {
			vnp_Params.put("vnp_BankCode", bankCode);
		}
		vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
		vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
		vnp_Params.put("vnp_OrderType", orderType);

		vnp_Params.put("vnp_Locale", "vn");
		vnp_Params.put("vnp_ReturnUrl", ConfigVNPay.vnp_ReturnUrl);
		vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

		Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String vnp_CreateDate = formatter.format(cld.getTime());
		vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

		cld.add(Calendar.MINUTE, 15);
		String vnp_ExpireDate = formatter.format(cld.getTime());
		vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

		List fieldNames = new ArrayList(vnp_Params.keySet());
		Collections.sort(fieldNames);
		StringBuilder hashData = new StringBuilder();
		StringBuilder query = new StringBuilder();
		Iterator itr = fieldNames.iterator();
		while (itr.hasNext()) {
			String fieldName = (String) itr.next();
			String fieldValue = (String) vnp_Params.get(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				// Build hash data
				hashData.append(fieldName);
				hashData.append('=');
				hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

				// Build query
				query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
				query.append('=');
				query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

				if (itr.hasNext()) {
					query.append('&');
					hashData.append('&');
				}
			}
		}

		String queryUrl = query.toString();
		String vnp_SecureHash = ConfigVNPay.hmacSHA512(ConfigVNPay.secretKey, hashData.toString());
		queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
		String paymentUrl = ConfigVNPay.vnp_PayUrl + "?" + queryUrl;

		response.setCode(200);
		response.setMessage("Success");
		response.setData(paymentUrl);

		return ResponseEntity.ok(response);
	}


	@GetMapping("/result-vnpay")
	public void paymentResult(HttpServletRequest req, HttpServletResponse res,
			@RequestParam Map<String, String> queryParams) throws IOException {
		String vpnResponseCode = queryParams.get("vnp_ResponseCode");
		int orderId = Integer.parseInt(queryParams.get("vnp_TxnRef"));

		Order order = orderService.getOrderById(orderId);

		if (vpnResponseCode.equals("24")) {
			removeOrder(order);		
			System.out.println("Hủy giao dịch");
		} else if (vpnResponseCode.equals("00")) {
			OrderStatus status = new OrderStatus();
			status.setStatusId(1);
			order.setOrderStatus(status);

			orderService.createOrderCart(order);
			System.out.println("Giao dịch thành công!");
		} else {
			removeOrder(order);	
			System.out.println("Lỗi máy chủ");
		}
	}
	
	private void removeOrder(Order order) {
		if(order == null ) return;
		
		Coupon coupon = order.getCoupon();
		Payment payment = order.getPayments().get(0);

		if (coupon != null) {
			userCouponService.deleteUserCoupon(coupon.getUserCoupons().get(0));
		}
		
		paymentService.deletePayment(payment);
		orderDetailService.deleteAllOrderDetail(order.getOrderDetails());
		orderService.deleteOrderById(order.getOrderId());
	}

	private long getAmountByOrderDetail(CartOrderModel orderModel) {
		BigDecimal amount = BigDecimal.ZERO;

		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			ProductVersion product = versionService.getProductVersionById(detail.getIdVersion());

			amount = amount.add(product.getRetailPrice().multiply(new BigDecimal(detail.getQuantity())));
		}

		return amount.longValue();
	}

	private ResponseAPI<CartOrderResponse> createOder(CartOrderModel orderModel, User user) {
		ResponseAPI<CartOrderResponse> response = new ResponseAPI<>();
		ResponseAPI<Boolean> validOrder = validDataOrder(orderModel);

		if (!validOrder.getData()) {
			response.setCode(422);
			response.setMessage(validOrder.getMessage());

			return response;
		}

		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			ProductVersion version = versionService.getProductVersionById(detail.getIdVersion());

			if (detail.getIdVersion() == null) {
				response.setCode(422);
				response.setMessage("Id product canot be null");

				return response;
			}

			if (detail.getIdVersion() == null) {
				response.setCode(422);
				response.setMessage("Price canot be null");

				return response;
			}

			if (detail.getQuantity() <= 0) {
				response.setCode(422);
				response.setMessage("Quantity must be positive");

				return response;
			}

			if (version == null) {
				response.setCode(404);
				response.setMessage(String.format("Product id %s does not exist", detail.getIdVersion()));

				return response;
			}

			if (!version.getProduct().isStatus()) {
				response.setCode(404);
				response.setMessage("Product not found");

				return response;
			}

			if (detail.getQuantity() > version.getQuantity()) {
				response.setCode(422);
				response.setMessage("Exceeded stock quantity");

				return response;
			}

		}

		Order orderEntity = new Order();
		Coupon coupon = couponService.getCouponByCode(orderModel.getCouponCode());
		OrderStatus status = new OrderStatus();
		status.setStatusId(3);

		orderEntity.setAddress(orderModel.getAddress());
		if (coupon != null) {
			orderEntity.setCoupon(coupon);
			orderEntity.setDisPrice(coupon.getDisPrice());
		}

		orderEntity.setOrderDate(new Date());
		orderEntity.setDeliveryDate(new Date());
		orderEntity.setUser(user);
		orderEntity.setFullname(user.getFullName());
		orderEntity.setPhone(user.getPhone());
		orderEntity.setOrderStatus(status);
		// Thay quyền lớn nhất của user vào
		orderEntity.setIsCreator(false);

		// Save order
		Order orderSaved = orderService.createOrderCart(orderEntity);

		if (coupon != null) {
			UserCoupon userCoupon = new UserCoupon();
			userCoupon.setUser(user);
			userCoupon.setCoupon(coupon);

			userCouponService.createUserCoupon(userCoupon);
		}

		// Save order details
		int totalProduct = 0;
		BigDecimal amount = new BigDecimal(0);
		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			OrderDetail orderDetailEntity = new OrderDetail();
			ProductVersion product = versionService.getProductVersionById(detail.getIdVersion());
			product.setId(detail.getIdVersion());

			totalProduct += detail.getQuantity();
			amount = amount.add(product.getRetailPrice().multiply(new BigDecimal(detail.getQuantity())));

			orderDetailEntity.setOrder(orderSaved);
			orderDetailEntity.setProductVersionBean(product);
			orderDetailEntity.setQuantity(detail.getQuantity());
			orderDetailEntity.setPrice(product.getRetailPrice());

			orderDetailService.createOrderDetail(orderDetailEntity);
		}

		// save payment
		Payment paymentEntity = new Payment();
		PaymentMethod paymentMethod = new PaymentMethod();
		paymentMethod.setPaymentMethodId(1);

		paymentEntity.setOrder(orderSaved);
		paymentEntity.setPaymentDate(new Date());
		paymentEntity.setPaymentMethod(paymentMethod);
		paymentEntity.setAmount(amount);

		paymentService.createPayment(paymentEntity);

		CartOrderResponse orderResponse = new CartOrderResponse();
		orderResponse.setOrderId(orderSaved.getOrderId());

		response.setCode(200);
		response.setMessage("Success");
		response.setData(orderResponse);

		return response;
	}

	private ResponseAPI<Boolean> validDataOrder(CartOrderModel order) {
		ResponseAPI<Boolean> response = new ResponseAPI<>();
		response.setCode(422);
		response.setData(false);

		final BigDecimal zero = new BigDecimal(0.0);
		final BigDecimal limitDispercent = new BigDecimal(0.7);

		if (order.getAddress() == null) {
			response.setMessage("Address cannot be null");
			return response;
		}

		if (order.getAddress().trim().length() == 0) {
			response.setMessage("Address cannot be blank");
			return response;
		}

		if (order.getOrderDetails() == null) {
			response.setMessage("Order details cannot be null");
			return response;
		}

		if (order.getOrderDetails().size() <= 0) {
			response.setMessage("Order details cannot empty");
			return response;
		}

		if (order.getCouponCode() != null) {
			Coupon coupon = couponService.getCouponByCode(order.getCouponCode());

			if (coupon == null) {
				response.setCode(404);
				response.setMessage("Coupon code not found");
				return response;
			}

			long now = new Date().getTime();
			if (now < coupon.getStartDate().getTime() || now > coupon.getEndDate().getTime()) {
				response.setCode(402);
				response.setMessage("Coupon code expired");
				return response;
			}
		}

		response.setCode(200);
		response.setMessage("Success");
		response.setData(true);

		return response;
	}
}
