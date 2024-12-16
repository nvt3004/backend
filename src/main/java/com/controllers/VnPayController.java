package com.controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

import com.repositories.CouponJPA;
import com.responsedto.SaleProductDTO;
import com.services.*;
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
import com.models.CartOrderDetailModel;
import com.models.CartOrderModel;
import com.repositories.OrderJPA;
import com.repositories.UserCouponJPA;
import com.responsedto.CartOrderResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/vnp")
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
	
	@Autowired
	UserCouponJPA userCouponJPA;
	
	@Autowired
	VersionService vsService;

	@Autowired
	SaleService saleService;
    @Autowired
    private CouponJPA couponJPA;

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

		if (user.getStatus() == 0) {
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
		vnp_Params.put("vnp_Amount", String.valueOf(amount*100));
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
			User user = order.getUser();		
			Coupon coupon = order.getCoupon();
			
			if (coupon != null) {
				UserCoupon temp = userCouponJPA.findUsercouponByCoupon(coupon.getCouponId(),user.getUserId());
				
				if(temp != null) {
					temp.setStatus(true);
					userCouponService.createUserCoupon(temp);
				}
			}
					
			System.out.println("Hủy giao dịch");
			removeOrder(order);	
			res.sendRedirect("http://localhost:3000/pm-cancel");
		} else if (vpnResponseCode.equals("00")) {
			OrderStatus status = new OrderStatus();
			status.setStatusId(1);
			order.setOrderStatus(status);

			orderService.createOrderCart(order);
			
			//Mua xong xóa khỏi giỏ hàng
			User user = userService.getUserByUsername(order.getUser().getUsername());
			for(CartProduct crd : user.getCarts().get(0).getCartProducts()) {
				for(OrderDetail md: order.getOrderDetails()) {
					if(crd.getProductVersionBean().getId()==md.getProductVersionBean().getId()) {
						cartProductService.removeCartItem(crd);
						break;
					}
				}
			}
			
			System.out.println("Giao dịch thành công!");
			res.sendRedirect("http://localhost:3000/pm-success");
		} else {
			removeOrder(order);	
			System.out.println("Lỗi máy chủ");
		}
	}
	
	private void removeOrder(Order order) {
		if(order == null ) return;
		
		Payment payment = order.getPayments();

		
		paymentService.deletePayment(payment);
		orderDetailService.deleteAllOrderDetail(order.getOrderDetails());
		orderService.deleteOrderById(order.getOrderId());
	}

	private long getAmountByOrderDetail(CartOrderModel orderModel) {
	    BigDecimal totalAmount = BigDecimal.ZERO;
	    Coupon coupon = couponService.getCouponByCode(orderModel.getCouponCode());

	    for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
	        ProductVersion product = versionService.getProductVersionById(detail.getIdVersion());
	        BigDecimal productTotal = product.getRetailPrice().multiply(new BigDecimal(detail.getQuantity()));
	        totalAmount = totalAmount.add(productTotal);
	    }


	    if (coupon != null) {
	        if (coupon.getDisPercent() != null) {
	            double discount = coupon.getDisPercent().doubleValue() / 100;
	            totalAmount = totalAmount.multiply(BigDecimal.valueOf(1 - discount));
	        } else {
	            totalAmount = totalAmount.subtract(coupon.getDisPrice());
	        }
	    }

	    return totalAmount.add(orderModel.getFee()).longValue();
	}

	private ResponseAPI<CartOrderResponse> createOder(CartOrderModel orderModel, User user) {
		ResponseAPI<CartOrderResponse> response = new ResponseAPI<>();
		ResponseAPI<Boolean> validOrder = validDataOrder(orderModel);
		
		if(orderModel.getFee().compareTo(BigDecimal.ZERO)<0) {
			response.setCode(422);
			response.setMessage("Phí vận chuyển không hợp lệ");

			return response;
		}

		if (!validOrder.getData()) {
			response.setCode(422);
			response.setMessage(validOrder.getMessage());

			return response;
		}



		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			ProductVersion version = versionService.getProductVersionById(detail.getIdVersion());

			if (detail.getIdVersion() == null) {
				response.setCode(422);
				response.setMessage("Mã sản phẩm không được để trống");

				return response;
			}

			if (detail.getQuantity() <= 0) {
				response.setCode(422);
				response.setMessage("Số lượng phải lớn hơn 0");

				return response;
			}

			if (version == null) {
				response.setCode(404);
				response.setMessage(String.format("Sản phẩm với mã id %s không tồn tại", detail.getIdVersion()));

				return response;
			}

			if (!version.getProduct().isStatus() || !version.isStatus()) {
				response.setCode(404);
				response.setMessage("Sản phẩm không tồn tại");

				return response;
			}

			int stockQuantity = vsService.getTotalStockQuantityVersion(version.getId());

			if (stockQuantity <= 0) {
				response.setCode(422);
				response.setMessage("Sản phẩm này đã hết hàng");

				return response;
			}

			if (detail.getQuantity() > stockQuantity) {
				response.setCode(422);
				response.setMessage("Phiên bản sản phẩm với mã id " + detail.getIdVersion() + " hiện chỉ còn "
						+ stockQuantity + " sản phẩm");

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
			
			if(coupon.getDisPercent() != null) {
				orderEntity.setDisPercent(coupon.getDisPercent());
			}else {
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
		orderEntity.setOrderDate(date);
		orderEntity.setDeliveryDate(orderModel.getLeadTime());
		orderEntity.setUser(user);
		orderEntity.setFullname(user.getFullName());
		orderEntity.setPhone(user.getPhone());
		orderEntity.setOrderStatus(status);
		orderEntity.setShippingFee(orderModel.getFee());
		// Thay quyền lớn nhất của user vào
		orderEntity.setIsCreator(false);

		// Save order
		Order orderSaved = orderService.createOrderCart(orderEntity);
		
		if (coupon != null) {
			UserCoupon temp = userCouponJPA.findUsercouponByCoupon(coupon.getCouponId(),user.getUserId());
			
			if(temp != null) {
				temp.setStatus(false);
				userCouponService.createUserCoupon(temp);
			}else {
				UserCoupon userCoupon = new UserCoupon();
				userCoupon.setUser(user);
				userCoupon.setCoupon(coupon);
				userCoupon.setStatus(false);

				userCouponService.createUserCoupon(userCoupon);
			}
		}
		
		

		// Save order details
		int totalProduct = 0;
		BigDecimal amount = new BigDecimal(0);
		for (CartOrderDetailModel detail : orderModel.getOrderDetails()) {
			OrderDetail orderDetailEntity = new OrderDetail();
			ProductVersion product = versionService.getProductVersionById(detail.getIdVersion());
			SaleProductDTO saleProductDTO = saleService.getVersionSaleDTO(detail.getIdVersion());

			product.setId(detail.getIdVersion());
			orderDetailEntity.setPrice(saleProductDTO==null? product.getRetailPrice():saleProductDTO.getPrice());
			product.setRetailPrice(saleProductDTO==null? product.getRetailPrice():saleProductDTO.getPrice());

			totalProduct += detail.getQuantity();
			amount = amount.add(product.getRetailPrice().multiply(new BigDecimal(detail.getQuantity())));

			orderDetailEntity.setOrder(orderSaved);
			orderDetailEntity.setProductVersionBean(product);
			orderDetailEntity.setQuantity(detail.getQuantity());

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
			response.setMessage("Địa chỉ không được để trống");
			return response;
		}

		if (order.getAddress().trim().length() == 0) {
			response.setMessage("Địa chỉ không được để rỗng");
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
