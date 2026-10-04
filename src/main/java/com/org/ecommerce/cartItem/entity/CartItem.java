package com.org.ecommerce.cartItem.entity;

import com.org.ecommerce.cart.entity.Cart;
import com.org.ecommerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) //ONE PRODUCT CAN APPEAR IN MULTIPLE CART_ITEMS
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY) //ONE CART HAS MULTIPLE CART_ITEMS
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    private int quantity;
}
