package com.apes.orderservice.dto;

import com.apes.orderservice.model.OrderLineItems;

import java.util.List;

public record OrderRequest(
        List<OrderLineItemsDto> orderLineItemsDtoList
) {
}
