package com.org.ecommerce.cart.repository;

import com.org.ecommerce.cart.entity.Cart;
import com.org.ecommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
