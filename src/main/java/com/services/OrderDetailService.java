package com.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
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
import com.models.SizeDTO;
import com.repositories.OrderDetailJPA;
import com.repositories.ProductVersionJPA;
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

	public OrderDetailDTO convertToOrderDetailDTO(List<OrderDetail> orderDetailList) {
		List<OrderDetailProductDetailsDTO> productDetails = createProductDetailsList(orderDetailList);

		OrderDetail orderDetail = orderDetailList.get(0);
		String disPercent = formatDiscount(orderDetail.getOrder().getDisPercent());
		String disPrice = formatDiscount(orderDetail.getOrder().getDisPrice());

		Integer couponId = (orderDetail.getOrder().getCoupon() != null)
				? orderDetail.getOrder().getCoupon().getCouponId()
				: null;

		String paymentMethod = (orderDetail.getOrder().getPayments() != null
				&& orderDetail.getOrder().getPayments().getPaymentMethod() != null)
						? orderDetail.getOrder().getPayments().getPaymentMethod().getMethodName()
						: null;

		return new OrderDetailDTO(orderDetail.getOrder().getOrderId(), orderDetail.getOrder().getAddress(), couponId,
				orderDetail.getOrder().getDeliveryDate(), disPercent, disPrice, orderDetail.getOrder().getFullname(),
				orderDetail.getOrder().getOrderDate(), orderDetail.getOrder().getPhone(),
				orderDetail.getOrder().getOrderStatus().getStatusName(),
				orderUtilsService.calculateOrderTotal(orderDetail.getOrder()), paymentMethod,
				orderDetail.getOrder().getPhone(), orderDetail.getOrder().getUser().getEmail(), productDetails);
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
			BigDecimal price = item.getPrice();
			BigDecimal total = price.multiply(quantity);

			Image images = item.getProductVersionBean().getImage();
			String imageUrl = null;
			if (images != null) {
				imageUrl = images.getImageUrl();
			}

			productDetails.add(new OrderDetailProductDetailsDTO(
					item.getProductVersionBean().getProduct().getProductId(), item.getProductVersionBean().getId(),
					item.getPrice(), item.getQuantity(), uploadService.getUrlImage(imageUrl),
					item.getProductVersionBean().getProduct().getDescription(), total, item.getOrderDetailId(),
					attributeProductVersion, attributesProducts));
		}
		return productDetails;
	}

	private List<AttributeDTO> createAttributeListByProductId(Integer productId) {
		List<AttributeDTO> attributeList = new ArrayList<>();
		List<ColorDTO> colorList = new ArrayList<>();
		List<SizeDTO> sizeList = new ArrayList<>();
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
						colorList.add(colorDTO);
					} else if ("Size".equalsIgnoreCase(attributeName)) {
						SizeDTO sizeDTO = new SizeDTO();
						sizeDTO.setSizeId(attributeId);
						sizeDTO.setSize(attributeValue);
						sizeList.add(sizeDTO);
					}
				}
			}
		}

		AttributeDTO attributeDTO = new AttributeDTO(colorList, sizeList);
		attributeList.add(attributeDTO);

		return attributeList;
	}

	private String formatDiscount(BigDecimal discount) {
		return discount != null ? discount.stripTrailingZeros().toPlainString() : null;
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

		Optional<ProductVersion> newProductVersion = getProductVersion(productId, colorId, sizeId);
		if (!newProductVersion.isPresent()) {
			return new ApiResponse<>(400, "The provided color and size combination is not valid.", null);
		}

		orderDetail.setProductVersionBean(newProductVersion.get());
		orderDetail.setQuantity(1);
		orderDetail.setPrice(newProductVersion.get().getRetailPrice());

		OrderDetail updatedOrderDetail = orderDetailJpa.save(orderDetail);

		return new ApiResponse<>(200, "Order detail updated successfully", updatedOrderDetail);
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
		Integer totalQuantityReturnedToStock = cancelledOrderQuantity;
		Integer availableProductVersionStock = productVersionStock + totalQuantityReturnedToStock - totalQuantitySold;

		if (quantity > availableProductVersionStock) {
			return new ApiResponse<>(400,
					"Requested quantity exceeds available stock. Available stock: " + availableProductVersionStock,
					null);
		}

		orderDetail.setQuantity(quantity);
		orderDetailJpa.save(orderDetail);

		return new ApiResponse<>(200, "Order detail quantity updated successfully", orderDetail);
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
