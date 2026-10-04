package com.org.ecommerce.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponseDto {
    private Long id;

    private String productName;

    private String brand;

    private String category;

    private String description;

    private String imageUrl;

    private BigDecimal price;

    private int stockQuantity;

    private double rating;
}
