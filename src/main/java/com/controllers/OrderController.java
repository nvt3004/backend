package com.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.entities.OrderDetail;
import com.entities.User;
import com.errors.ApiResponse;
import com.errors.InvalidException;
import com.errors.UserServiceException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.models.OrderByUserDTO;
import com.models.OrderDTO;
import com.models.OrderDetailProductDetailsDTO;
import com.models.OrderQRCodeDTO;
import com.models.OrderStatusDTO;
import com.services.AuthService;
import com.services.JWTService;
import com.services.OrderDetailService;
import com.services.OrderService;
import com.services.OrderStatusService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class OrderController<UsbPrinter> {

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderDetailService orderDetailService;

	@Autowired
	private OrderStatusService orderStatusService;

	@Autowired
	private AuthService authService;

	@Autowired
	private JWTService jwtService;

	@GetMapping("/staff/orders")
	@PreAuthorize("hasPermission(#userId, 'View Order')")
	public ResponseEntity<ApiResponse<?>> getAllOrders(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (page < 0 || size < 1) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(400,
					"Invalid page number or size. Page must be greater than or equal to 0 and size must be greater than or equal to 1.",
					null));
		}

		try {
			ApiResponse<PageImpl<OrderDTO>> successResponse = orderService.getAllOrders(keyword, statusId, page, size);
			return ResponseEntity.ok(successResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An error occurred while retrieving orders " + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/user/orders/username")
	public ResponseEntity<ApiResponse<?>> getOrdersByUsername(
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestHeader("Authorization") Optional<String> authHeader,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size) {

		ApiResponse<String> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (page < 0) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid page number. It must be greater than or equal to 0.", null));
		}

		if (size < 1) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Invalid size. It must be greater than or equal to 1.", null));
		}

		try {
			ApiResponse<PageImpl<OrderByUserDTO>> successResponse = orderService.getOrdersByUsername(user.getUsername(),
					keyword, statusId, page, size);
			return ResponseEntity.ok(successResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An error occurred while retrieving orders " + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	@GetMapping("/staff/orders/statuses")
	@PreAuthorize("hasPermission(#userId, 'View Order')")
	public ResponseEntity<ApiResponse<?>> getAllOrderStatus(
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		List<OrderStatusDTO> orderStatusDTOList = orderStatusService.getAllOrderStatusDTOs();
		ApiResponse<List<OrderStatusDTO>> response = new ApiResponse<>(200, "Order statuses fetched successfully",
				orderStatusDTOList);

		return ResponseEntity.ok(response);

	}

	@PutMapping("/staff/orders/update-order-detail")
	@PreAuthorize("hasPermission(#userid, 'Update Order')")
	public ResponseEntity<ApiResponse<?>> updateOrderDetail(@RequestParam("orderDetailId") Integer orderDetailId,
			@RequestParam("productId") Integer productId, @RequestParam("colorId") Integer colorId,
			@RequestParam("sizeId") Integer sizeId, @RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<String> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (orderDetailId == null || productId == null || colorId == null || sizeId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		ApiResponse<OrderDetail> response = orderDetailService.updateOrderDetail(orderDetailId, productId, colorId,
				sizeId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@PutMapping("/staff/orders/update-order-detail-quantity")
	@PreAuthorize("hasPermission(#userid, 'Update Order')")
	public ResponseEntity<ApiResponse<?>> updateOrderDetailQuantity(
			@RequestParam("orderDetailId") Integer orderDetailId, @RequestParam("quantity") Integer quantity,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (orderDetailId == null || quantity == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		ApiResponse<OrderDetail> validationResponse = orderDetailService
				.validateAndUpdateOrderDetailQuantity(orderDetailId, quantity);

		if (validationResponse.getErrorCode() != 200) {
			return ResponseEntity.status(HttpStatus.valueOf(validationResponse.getErrorCode()))
					.body(validationResponse);
		}

		return ResponseEntity.ok(new ApiResponse<>(200, "Order detail quantity updated successfully", null));
	}

	@PutMapping("/staff/orders/update-status")
	@PreAuthorize("hasPermission(#userid, 'Update Order')")
	public ResponseEntity<ApiResponse<?>> updateOrderStatus(@RequestParam("orderId") Integer orderId,
			@RequestParam("statusId") Integer statusId, @RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (orderId == null || statusId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		errorResponse = orderService.updateOrderStatus(orderId, statusId, user);

		if (errorResponse.getErrorCode() == 200) {
			return ResponseEntity.ok(errorResponse);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getErrorCode())).body(errorResponse);
		}
	}

	@GetMapping("/staff/orders/{orderId}")
	@PreAuthorize("hasPermission(#userid, 'View Order')")
	public ResponseEntity<ApiResponse<?>> getOrderDetail(@PathVariable Integer orderId,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (orderId == null) {
			ApiResponse<String> response = new ApiResponse<>(400, "Order ID is required", null);
			return ResponseEntity.badRequest().body(response);
		}

		ApiResponse<Map<String, Object>> response = orderService.getOrderDetails(orderId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@DeleteMapping("/staff/orders/remove-orderdetail")
	@PreAuthorize("hasPermission(#userid, 'Delete Order')")
	public ResponseEntity<ApiResponse<?>> deleteOrderDetailsByOrderDetailId(@RequestParam Integer orderId,
			@RequestParam Integer orderDetailId, @RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<String> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (orderId == null || orderDetailId == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse<>(400, "Some required parameters are missing.", null));
		}

		ApiResponse<?> response = orderService.deleteOrderDetail(orderId, orderDetailId);

		return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
	}

	@PutMapping("/user/orders/cancel-order")
	@PreAuthorize("hasPermission(#userid, 'Delete Order')")
	public ResponseEntity<ApiResponse<?>> cancelOrder(@RequestParam("orderId") Integer orderId,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}

		if (orderId == null) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Order ID is required.", null));
		}

		errorResponse = orderService.cancelOrder(orderId, user);

		if (errorResponse.getErrorCode() == 200) {
			return ResponseEntity.ok(errorResponse);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(errorResponse.getErrorCode())).body(errorResponse);
		}
	}

	@GetMapping("/staff/orders/export")
//	@PreAuthorize("hasPermission(#userId, 'STAFF_ORDER_VIEW_ALL')")
	public ResponseEntity<?> exportOrdersToExcel(
			@RequestParam(value = "isAdminOrder", required = false) Boolean isAdminOrder,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestHeader("Authorization") Optional<String> authHeader) {

		ApiResponse<?> errorResponse = new ApiResponse<>();

		if (!authHeader.isPresent()) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Authorization header is missing");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		String token = authService.readTokenFromHeader(authHeader);

		try {
			jwtService.extractUsername(token);
		} catch (Exception e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage("Invalid token format");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		}

		User user;
		try {
			user = authService.validateTokenAndGetUsername(token);
		} catch (InvalidException e) {
			errorResponse.setErrorCode(401);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
		} catch (UserServiceException e) {
			errorResponse.setErrorCode(400);
			errorResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
		} catch (Exception e) {
			errorResponse.setErrorCode(500);
			errorResponse.setMessage("An unexpected error occurred: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
		ApiResponse<ByteArrayResource> apiResponse = new ApiResponse<>();
		try {

			ByteArrayResource file = orderService.exportOrdersToExcel(isAdminOrder, keyword, statusId, page, size);

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.xlsx")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
		} catch (Exception e) {
			apiResponse.setErrorCode(500);
			apiResponse.setMessage(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
		}
	}

	@GetMapping("/orders/{orderId}")
	public ResponseEntity<ApiResponse<?>> getOrderDetail(@PathVariable Integer orderId) {

		if (orderId == null) {
			ApiResponse<String> response = new ApiResponse<>(400, "Order ID is required", null);
			return ResponseEntity.badRequest().body(response);
		}

		ApiResponse<Map<String, Object>> response = orderService.getOrder(orderId);

		if (response.getErrorCode() == 200) {
			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.status(HttpStatus.valueOf(response.getErrorCode())).body(response);
		}
	}

	@PostMapping("/staff/orders/export")
	public void exportInvoiceAsPdf(@RequestParam("orderId") Integer orderId, HttpServletResponse response) {
		try {
			ByteArrayOutputStream pdfStream = orderService.generateInvoicePdf(orderId);

			BufferedImage image = orderService.convertPdfToImage(pdfStream);

			// Trả ảnh về client
			response.setContentType("image/png");
			ImageIO.write(image, "png", response.getOutputStream());
		} catch (IllegalArgumentException e) {
			response.setStatus(400);
			try {
				response.getWriter().write(e.getMessage());
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
			try {
				response.getWriter().write("Error generating PDF: " + e.getMessage());
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	// Helper functions for cleaner code
	private float addParagraphToDocument(Document document, String text, Font font, int alignment)
			throws DocumentException {
		Paragraph paragraph = new Paragraph(text, font);
		paragraph.setAlignment(alignment);
		document.add(paragraph);
		return paragraph.getTotalLeading(); // Return the height of the paragraph
	}

	private float addTableToDocument(Document document, List<OrderDetailProductDetailsDTO> products, Font font)
			throws DocumentException {
		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(100);
		table.setWidths(new float[] { 4f, 2f, 3f });

		table.addCell(new PdfPCell(new Phrase("Tên SP & SL", font)));
		table.addCell(new PdfPCell(new Phrase("Đơn Giá", font)));
		table.addCell(new PdfPCell(new Phrase("Thành Tiền", font)));

		for (OrderDetailProductDetailsDTO product : products) {
			table.addCell(new PdfPCell(new Phrase(product.getProductName() + " x" + product.getQuantity(), font)));
			table.addCell(new PdfPCell(new Phrase(String.valueOf(product.getPrice()), font)));
			table.addCell(new PdfPCell(new Phrase(String.valueOf(product.getTotal()), font)));
		}
		document.add(table);
		return table.getTotalHeight(); // Return the height of the table
	}

//	private Rectangle createPageSize(float height) {
//	    // Convert width from mm to points (1 mm = 2.83465 points)
//	    float widthInPoints = 58 * 2.83465f;
//	    float heightInPoints = height * 2.83465f;
//	    System.out.println(widthInPoints + " widthInPoints");
//	    return new Rectangle(widthInPoints, heightInPoints);
//	}

	// Trả PDF về client
//    response.setContentType("application/pdf");
//    response.setHeader("Content-Disposition", "attachment; filename=\"hoa_don.pdf\""); // Set filename
//    response.setContentLength(baos.size());
//    response.getOutputStream().write(baos.toByteArray());

}