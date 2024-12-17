package com.services;

import java.util.ArrayList;
import java.util.List;

import com.responsedto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.AttributeOptionsVersion;
import com.entities.Cart;
import com.entities.CartProduct;
import com.entities.Image;
import com.entities.ProductVersion;
import com.repositories.CartJPA;
import com.repositories.UserJPA;
import com.utils.UploadService;

@Service
public class CartService {
	@Autowired
	CartJPA cartJPA;

	@Autowired
	UserJPA userJPA;

	@Autowired
	UploadService uploadService;

	@Autowired
	ProductService productService;
	
	@Autowired
	VersionService vsService;

	@Autowired
	SaleService saleService;

	public Cart addCart(Cart cart) {
		Cart cartTemp = cartJPA.getCartByUser(cart.getUser().getUserId());

		if (cartTemp != null) {
			return cartTemp;
		}

		return cartJPA.save(cart);
	}

	public List<CartItemResponse> getAllCartItemByUser(int userId) {

		List<CartItemResponse> items = new ArrayList<>();
		List<CartProduct> cartProducts = cartJPA.getAllCartItemByUser(userId);

		for (CartProduct cart : cartProducts) {
			List<Attribute> attributes = new ArrayList<>();
			ProductVersion pdvsion = cart.getProductVersionBean();
			boolean activeVersion = cart.getProductVersionBean().getProduct().isStatus() && cart.getProductVersionBean().isStatus();
			int stockQuantityVersion = vsService.getTotalStockQuantityVersion(cart.getProductVersionBean().getId());
			boolean active = pdvsion.isStatus() && pdvsion.getProduct().isStatus();
			SaleProductDTO saleProductDTO = saleService.getVersionSaleDTO(pdvsion.getId());
			
			CartItemResponse item = new CartItemResponse(cart.getCartPrdId(), cart.getProductVersionBean().getId(),
					activeVersion, cart.getProductVersionBean().getQuantity(),
					cart.getProductVersionBean().getProduct().getProductName(),
					cart.getProductVersionBean().getRetailPrice(), cart.getQuantity(), stockQuantityVersion, active);

			if(saleProductDTO != null) {
				item.setSale(saleProductDTO.getSale());
				item.setSalePrice(saleProductDTO.getPrice());
			}

			ProductDetailResponse productDetail = productService
					.getProductDetail(cart.getProductVersionBean().getProduct().getProductId());
			productDetail.setProduct(null);
			item.setProductDetail(productDetail);

			List<AttributeOptionsVersion> optionVersions = cart.getProductVersionBean().getAttributeOptionsVersions();
			optionVersions.stream().forEach(optionVersion -> {
				Attribute attribute = new Attribute(0,
						optionVersion.getAttributeOption() != null
								? optionVersion.getAttributeOption().getAttribute().getAttributeName()
								: null,
						optionVersion.getAttributeOption() != null
								? optionVersion.getAttributeOption().getAttributeValue()
								: null);
				attributes.add(attribute);
			});

			item.setAttributes(attributes);

			String image = cart.getProductVersionBean().getProduct().getProductImg();
			item.setImage(uploadService.getUrlImage(image));

			items.add(item);
		}

		return items;
	}

	private String getFirstImageInList(List<Image> images) {
		if (images.size() > 0) {
			return uploadService.getUrlImage(images.get(0).getImageUrl());
		}

		return null;
	}
}
