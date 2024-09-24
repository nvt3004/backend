package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.entities.Wishlist;

public interface WishlistJPA extends JpaRepository<Wishlist, Integer> {

}
