package com.org.ecommerce.order.dto;

import com.org.ecommerce.common.enums.OrderStatus;
import com.org.ecommerce.orderItem.dto.OrderItemResponseDto;
import com.org.ecommerce.orderItem.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {
    private Long user_id;
    private Long order_id;
    private BigDecimal amount;
    private OrderStatus status;
    private List<OrderItemResponseDto> orderItems;
}
