package com.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.Cart;
import com.entities.CartProduct;
import com.entities.User;
import com.repositories.CartProductJPA;
import com.responsedto.CartDetailResponse;
import com.responsedto.ProductCartResponse;
import com.responsedto.VersionCart;

@Service
public class CartProductService {
	@Autowired
	CartProductJPA cartProductJPA;

	public ProductCartResponse addProductToCart(CartProduct cartProduct) {
		int versionId = cartProduct.getProductVersionBean().getId();
		int userId = cartProduct.getCart().getUser().getUserId();
		CartProduct cartSaved;

		CartProduct cartProductTemp = cartProductJPA.getVersionInCartByUser(versionId, userId);

		if (cartProductTemp != null) {
			cartProductTemp.setQuantity(cartProductTemp.getQuantity() + cartProduct.getQuantity());
			cartSaved = cartProductJPA.save(cartProductTemp);
		} else {
			cartSaved = cartProductJPA.save(cartProduct);
		}

		ProductCartResponse cartResponse = new ProductCartResponse();
		VersionCart versionCart = new VersionCart(userId, cartSaved.getProductVersionBean().getVersionName(),
				cartSaved.getProductVersionBean().getRetailPrice());
		CartDetailResponse cartDetail = new CartDetailResponse(versionCart, cartSaved.getQuantity());

		cartResponse.setCartId(cartSaved.getCart().getCartId());
		cartResponse.setUserId(cartSaved.getCart().getUser().getUserId());
		cartResponse.setCartItem(cartDetail);

		return cartResponse;
	}

	public CartProduct getCartItemById(int id) {
		return cartProductJPA.findById(id).orElse(null);
	}

	public boolean removeCartItem(CartProduct cartItem) {

		try {
			cartProductJPA.delete(cartItem);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public CartProduct updateCartItem(CartProduct cartItem) {
		return cartProductJPA.save(cartItem);
	}

	public boolean isValidItem(User user, int cartItemId) {
		List<Cart> carts = user.getCarts();

		if (carts == null) {
			return false;
		}

		if (carts.get(0).getCartProducts() == null) {
			return false;
		}

		for (CartProduct item : carts.get(0).getCartProducts()) {
			if (cartItemId == item.getCartPrdId()) {
				return true;
			}
		}

		return false;
	}
}
