package com.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.entities.User;
import com.entities.Wishlist;
import com.repositories.ProductJPA;
import com.repositories.WishlistJPA;

@Service
public class WishlistService {
	@Autowired
	WishlistJPA wishlistJPA;

	@Autowired
	ProductJPA productJPA;

	public List<Wishlist> getAllWisListByUser(User user) {
		List<Wishlist> wishlists = user.getWishlists();
		return wishlists;
	}
	
	public Wishlist createWishlist(Wishlist wishlist) {	
		return wishlistJPA.save(wishlist);
	}
	
	public boolean isFavorited(User user, int productId) {
		List<Wishlist> wishlists = user.getWishlists();

		for (Wishlist wl : wishlists) {
			if (wl.getProduct().getProductId() == productId) {
					return true;
			}
		}
		
		return false;
	}

	public boolean validWishlist(User user, int wishlistId) {
		Wishlist wishlist = wishlistJPA.findById(wishlistId).orElse(null);

		if (wishlist == null) {
			return false;
		}

		if (wishlist.getUser().getUserId() != user.getUserId()) {
			return false;
		}

		return true;
	}

	public boolean removeWishlist(int id) {
		try {
			wishlistJPA.deleteById(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
