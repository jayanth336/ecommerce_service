package com.org.ecommerce.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDto {
    @NotNull
    private Long productId;

    @Min(1)
    private int quantity;
}
