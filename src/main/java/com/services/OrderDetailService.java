package com.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.entities.Coupon;
import com.entities.Image;
import com.entities.Order;
import com.entities.OrderDetail;
import com.entities.ProductVersion;
import com.errors.ApiResponse;
import com.models.AttributeDTO;
import com.models.AttributeProductVersionDTO;
import com.models.ColorDTO;
import com.models.OrderDetailDTO;
import com.models.OrderDetailProductDetailsDTO;
import com.models.OrderQRCodeDTO;
import com.models.SizeDTO;
import com.repositories.AttributeOptionJPA;
import com.repositories.OrderDetailJPA;
import com.repositories.ProductVersionJPA;
import com.utils.FormarCurrencyUtil;
import com.utils.NumberToWordsConverterUtil;
import com.utils.UploadService;

@Service
public class OrderDetailService {

	@Autowired
	private OrderDetailJPA orderDetailJpa;

	@Autowired
	ProductVersionJPA productVersionJpa;

	@Autowired
	private ProductVersionService productVersionService;

	@Autowired
	private OrderUtilsService orderUtilsService;

	@Autowired
	private UploadService uploadService;

	@Autowired
	private MailService mailService;

	public OrderDetailDTO convertToOrderDetailDTO(List<OrderDetail> orderDetailList) {
		List<OrderDetailProductDetailsDTO> productDetails = createProductDetailsList(orderDetailList);

		OrderDetail orderDetail = orderDetailList.get(0);

		Integer couponId = (orderDetail.getOrder().getCoupon() != null)
				? orderDetail.getOrder().getCoupon().getCouponId()
				: null;

		String paymentMethod = (orderDetail.getOrder().getPayments() != null
				&& orderDetail.getOrder().getPayments().getPaymentMethod() != null)
						? orderDetail.getOrder().getPayments().getPaymentMethod().getMethodName()
						: null;

		String email = (orderDetail.getOrder().getUser() != null && orderDetail.getOrder().getUser().getEmail() != null)
				? orderDetail.getOrder().getUser().getEmail()
				: "N/A";

		return new OrderDetailDTO(orderDetail.getOrder().getOrderId(), orderDetail.getOrder().getAddress(), couponId,
				orderDetail.getOrder().getDeliveryDate(),
				orderUtilsService.calculateDiscountedPrice(orderDetailList.get(0).getOrder()),
				orderDetail.getOrder().getFullname(), orderDetail.getOrder().getOrderDate(),
				orderDetail.getOrder().getPhone(), orderDetail.getOrder().getOrderStatus().getStatusName(),
				orderUtilsService.calculateOrderTotal(orderDetail.getOrder()), paymentMethod,
				orderDetail.getOrder().getPhone(), email, productDetails);
	}

	private List<OrderDetailProductDetailsDTO> createProductDetailsList(List<OrderDetail> orderDetails) {
		List<OrderDetailProductDetailsDTO> productDetails = new ArrayList<>();

		for (OrderDetail item : orderDetails) {
			ColorDTO color = new ColorDTO();
			SizeDTO size = new SizeDTO();

			for (AttributeOptionsVersion aov : item.getProductVersionBean().getAttributeOptionsVersions()) {
				String attributeName = aov.getAttributeOption().getAttribute().getAttributeName();
				if ("Color".equalsIgnoreCase(attributeName)) {
					color.setColor(aov.getAttributeOption().getAttributeValue());
					color.setColorId(aov.getAttributeOption().getId());
				} else if ("Size".equalsIgnoreCase(attributeName)) {
					size.setSizeId(aov.getAttributeOption().getId());
					size.setSize(aov.getAttributeOption().getAttributeValue());
				}
			}

			AttributeProductVersionDTO attributeProductVersion = new AttributeProductVersionDTO(color, size);

			List<AttributeDTO> attributesProducts = createAttributeListByProductId(
					item.getProductVersionBean().getProduct().getProductId());

			BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
			BigDecimal price = item.getPrice().setScale(0, RoundingMode.DOWN);
			BigDecimal total = price.multiply(quantity).setScale(0, RoundingMode.DOWN);

			Image images = item.getProductVersionBean().getImage();
			String imageUrl = null;
			if (images != null) {
				imageUrl = images.getImageUrl();
			}

			productDetails.add(new OrderDetailProductDetailsDTO(
					item.getProductVersionBean().getProduct().getProductId(), item.getProductVersionBean().getId(),
					item.getProductVersionBean().getVersionName(), price, item.getQuantity(),
					uploadService.getUrlImage(imageUrl), item.getProductVersionBean().getProduct().getDescription(),
					total, item.getOrderDetailId(), attributeProductVersion, attributesProducts,
					item.getProductVersionBean().getProduct().getProductName()));
		}
		return productDetails;
	}

	private List<AttributeDTO> createAttributeListByProductId(Integer productId) {
		List<AttributeDTO> attributeList = new ArrayList<>();
		Set<ColorDTO> colorSet = new HashSet<>();
		Set<SizeDTO> sizeSet = new HashSet<>();
		List<ProductVersion> productVersions = productVersionJpa.findByProductId(productId);

		if (productVersions != null) {
			for (ProductVersion productVersion : productVersions) {
				for (AttributeOptionsVersion aov : productVersion.getAttributeOptionsVersions()) {
					String attributeName = aov.getAttributeOption().getAttribute().getAttributeName();
					String attributeValue = aov.getAttributeOption().getAttributeValue();
					Integer attributeId = aov.getAttributeOption().getId();
					if ("Color".equalsIgnoreCase(attributeName)) {
						ColorDTO colorDTO = new ColorDTO();
						colorDTO.setColorId(attributeId);
						colorDTO.setColor(attributeValue);
						colorSet.add(colorDTO);
					} else if ("Size".equalsIgnoreCase(attributeName)) {
						SizeDTO sizeDTO = new SizeDTO();
						sizeDTO.setSizeId(attributeId);
						sizeDTO.setSize(attributeValue);
						sizeSet.add(sizeDTO);
					}
				}
			}
		}

		List<ColorDTO> colorList = new ArrayList<>(colorSet);
		List<SizeDTO> sizeList = new ArrayList<>(sizeSet);
		AttributeDTO attributeDTO = new AttributeDTO(colorList, sizeList);
		attributeList.add(attributeDTO);

		return attributeList;
	}

	public Optional<OrderDetail> findOrderDetailById(Integer orderDetailId) {
		return orderDetailJpa.findById(orderDetailId);
	}

	public boolean isValidOrderStatus(String status) {
		return "Pending".equalsIgnoreCase(status);
	}

	public Optional<ProductVersion> getProductVersion(Integer productId, Integer colorId, Integer sizeId) {
		return productVersionService.getProductVersionByAttributes(productId, colorId, sizeId);
	}

	public ApiResponse<OrderDetail> updateOrderDetail(Integer orderDetailId, Integer productId, Integer colorId,
			Integer sizeId) {

		Optional<OrderDetail> existingOrderDetail = findOrderDetailById(orderDetailId);
		if (!existingOrderDetail.isPresent()) {
			return new ApiResponse<>(404, "Order detail not found", null);
		}

		OrderDetail orderDetail = existingOrderDetail.get();
		Order order = orderDetail.getOrder();

		if (!isValidOrderStatus(order.getOrderStatus().getStatusName())) {
			return new ApiResponse<>(400, "Order cannot be updated in its current state", null);
		}

		String colorName = null;
		String sizeName = null;
		ProductVersion currentProductVersion = orderDetail.getProductVersionBean();
		for (AttributeOptionsVersion aov : currentProductVersion.getAttributeOptionsVersions()) {
			String attributeName = aov.getAttributeOption().getAttribute().getAttributeName();
			if ("Color".equalsIgnoreCase(attributeName)) {
				colorName = aov.getAttributeOption().getAttributeValue();
			} else if ("Size".equalsIgnoreCase(attributeName)) {
				sizeName = aov.getAttributeOption().getAttributeValue();
			}
		}

		Optional<ProductVersion> newProductVersion = getProductVersion(productId, colorId, sizeId);
		if (!newProductVersion.isPresent()) {
			String productName = currentProductVersion.getProduct().getProductName();
			return new ApiResponse<>(400,
					String.format("The product '%s', color '%s', and size '%s' combination does not exist.",
							productName, colorName, sizeName),
					null);
		}

		orderDetail.setProductVersionBean(newProductVersion.get());
		orderDetail.setQuantity(1); // Set default quantity
		orderDetail.setPrice(newProductVersion.get().getRetailPrice());

		// Save updated order detail
		OrderDetail updatedOrderDetail = orderDetailJpa.save(orderDetail);

		// Send email notification
		sendOrderDetailUpdateEmail(orderDetail, order.getUser().getEmail(), newProductVersion.get());

		return new ApiResponse<>(200, "Order detail updated successfully", updatedOrderDetail);
	}

	private void sendOrderDetailUpdateEmail(OrderDetail orderDetail, String userEmail,
			ProductVersion newProductVersion) {
		// Construct the email content and send the email asynchronously
		CompletableFuture.runAsync(() -> {
			String emailContent = generateOrderDetailUpdateEmailContent(orderDetail, newProductVersion);
			mailService.sendEmail(userEmail, "Your Order Detail Has Been Updated", emailContent);
		});
	}

	private String generateOrderDetailUpdateEmailContent(OrderDetail orderDetail, ProductVersion newProductVersion) {
		// You can modify this method to generate an HTML email content based on the
		// updated details
		return String.format("Your order detail has been updated to product '%s' with price %s.",
				newProductVersion.getProduct().getProductName(),
				FormarCurrencyUtil.formatCurrency(newProductVersion.getRetailPrice()));
	}

	public boolean isValidQuantity(Integer quantity) {
		return quantity != null && quantity > 0;
	}

	public ApiResponse<OrderDetail> validateAndUpdateOrderDetailQuantity(Integer orderDetailId, Integer quantity) {

		Optional<OrderDetail> existingOrderDetail = findOrderDetailById(orderDetailId);
		if (!existingOrderDetail.isPresent()) {
			return new ApiResponse<>(404, "Order detail not found", null);
		}

		OrderDetail orderDetail = existingOrderDetail.get();
		String orderStatusName = orderDetail.getOrder().getOrderStatus().getStatusName();

		if (!isValidOrderStatus(orderStatusName)) {
			return new ApiResponse<>(400, "Order cannot be updated in its current state", null);
		}

		if (!isValidQuantity(quantity)) {
			return new ApiResponse<>(400, "Quantity must be positive.", null);
		}

		ProductVersion productVersion = orderDetail.getProductVersionBean();
		Integer productVersionStock = productVersion.getQuantity();
		productVersionStock = (productVersionStock != null) ? productVersionStock : 0;

		Integer processedOrderQuantity = productVersionJpa
				.getTotalQuantityByProductVersionInProcessedOrders(productVersion.getId());
		Integer cancelledOrderQuantity = productVersionJpa
				.getTotalQuantityByProductVersionInCancelledOrders(productVersion.getId());
		Integer shippedOrderQuantity = productVersionJpa
				.getTotalQuantityByProductVersionInShippedOrders(productVersion.getId());
		Integer deliveredOrderQuantity = productVersionJpa
				.getTotalQuantityByProductVersionInDeliveredOrders(productVersion.getId());

		processedOrderQuantity = (processedOrderQuantity != null) ? processedOrderQuantity : 0;
		cancelledOrderQuantity = (cancelledOrderQuantity != null) ? cancelledOrderQuantity : 0;
		shippedOrderQuantity = (shippedOrderQuantity != null) ? shippedOrderQuantity : 0;
		deliveredOrderQuantity = (deliveredOrderQuantity != null) ? deliveredOrderQuantity : 0;

		Integer totalQuantitySold = processedOrderQuantity + shippedOrderQuantity + deliveredOrderQuantity;

		Integer availableProductVersionStock = productVersionStock - totalQuantitySold;

		if (quantity > availableProductVersionStock) {
			return new ApiResponse<>(400,
					"Requested quantity exceeds available stock. Available stock: " + availableProductVersionStock,
					null);
		}

		orderDetail.setQuantity(quantity);
		orderDetailJpa.save(orderDetail);
		System.out.println(orderDetail.getOrder().getUser().getEmail() + "EmailUser");

		sendQuantityUpdateEmail(orderDetail, orderDetail.getOrder().getUser().getEmail());

		return new ApiResponse<>(200, "Order detail quantity updated successfully", orderDetail);
	}

	private void sendQuantityUpdateEmail(OrderDetail orderDetail, String userEmail) {
		CompletableFuture.runAsync(() -> {
			String emailContent = generateQuantityUpdateEmailContent(orderDetail);
			mailService.sendEmail(userEmail, "Your Order Quantity Has Been Updated", emailContent);
		});
	}

	private String generateQuantityUpdateEmailContent(OrderDetail orderDetail) {
		return String.format("Your order quantity has been updated to %d for the product '%s'.",
				orderDetail.getQuantity(), orderDetail.getProductVersionBean().getProduct().getProductName());
	}

	public OrderQRCodeDTO convertToOrderQRCode(List<OrderDetail> orderDetailList) {
		if (orderDetailList == null || orderDetailList.isEmpty()) {
			throw new IllegalArgumentException("orderDetailList cannot be null or empty");
		}

		OrderDetail orderDetail = orderDetailList.get(0);
		BigDecimal subTotal = orderUtilsService.calculateOrderTotal(orderDetail.getOrder());
		BigDecimal discountValue = orderUtilsService.calculateDiscountedPrice(orderDetail.getOrder());
		BigDecimal finalTotal = subTotal.add(orderDetail.getOrder().getShippingFee()).subtract(discountValue);
		finalTotal = finalTotal.max(BigDecimal.ZERO);

		String finalTotalInWords = NumberToWordsConverterUtil.convert(finalTotal);
		String statusName = orderDetail.getOrder().getOrderStatus().getStatusName();
		Integer couponId = Optional.ofNullable(orderDetail.getOrder().getCoupon()).map(Coupon::getCouponId)
				.orElse(null);
		String disCount = orderUtilsService.getDiscountDescription(orderDetail.getOrder());

		String paymentMethod = Optional.ofNullable(orderDetail.getOrder().getPayments())
				.map(payments -> payments.getPaymentMethod()).map(paymentMethodObj -> paymentMethodObj.getMethodName())
				.orElse(null);

		String email = Optional.ofNullable(orderDetail.getOrder().getUser()).map(user -> user.getEmail()).orElse("N/A");

		List<OrderDetailProductDetailsDTO> productDetails = createProductDetailsList(orderDetailList);
		return new OrderQRCodeDTO(orderDetail.getOrder().getOrderId(), orderDetail.getOrder().getUser().getGender(),
				orderDetail.getOrder().getAddress(), couponId, disCount, discountValue, subTotal,
				orderDetail.getOrder().getShippingFee(), finalTotal, finalTotalInWords,
				orderDetail.getOrder().getDeliveryDate(), orderDetail.getOrder().getFullname(),
				orderDetail.getOrder().getOrderDate(), orderDetail.getOrder().getPhone(), statusName, paymentMethod,
				orderDetail.getOrder().getPhone(), email, productDetails);
	}

	// ty
	public OrderDetail createOrderDetail(OrderDetail orderDetail) {
		return orderDetailJpa.save(orderDetail);
	}

	public boolean deleteAllOrderDetail(List<OrderDetail> orderDetails) {
		try {
			orderDetailJpa.deleteAll(orderDetails);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
