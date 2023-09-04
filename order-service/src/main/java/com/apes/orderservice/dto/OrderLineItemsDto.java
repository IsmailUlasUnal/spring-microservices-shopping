package com.apes.orderservice.dto;

import com.apes.orderservice.model.OrderLineItems;

import java.math.BigDecimal;

public record OrderLineItemsDto(
        String id,
        String skuCode,
        BigDecimal price,
        Integer quantity
) {
    public static OrderLineItemsDto convert(OrderLineItems from) {
        return new OrderLineItemsDto(
                from.getId(),
                from.getSkuCode(),
                from.getPrice(),
                from.getQuantity());
    }
}
