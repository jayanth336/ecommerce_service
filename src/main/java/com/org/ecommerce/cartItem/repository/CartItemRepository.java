package com.org.ecommerce.cartItem.repository;

import com.org.ecommerce.cart.entity.Cart;
import com.org.ecommerce.cartItem.entity.CartItem;
import com.org.ecommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByIdAndCartUserId(Long cartItemId,  Long userId);
    /**
     * SELECT ci
     * FROM CartItem ci
     * WHERE ci.id = :cartItemId
     * AND ci.cart.user.id = :userId
     */

}
