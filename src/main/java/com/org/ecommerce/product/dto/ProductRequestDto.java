package com.org.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDto {
    @NotBlank
    private String productName;

    @NotBlank
    private String brand;

    @NotBlank
    private String category;

    @NotBlank
    private String description;

    @NotBlank
    private String imageUrl;

    @Positive
    private BigDecimal price;

    @PositiveOrZero
    private int initialStockQuantity;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private double rating;
}
