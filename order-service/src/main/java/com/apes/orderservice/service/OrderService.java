package com.apes.orderservice.service;

import com.apes.orderservice.dto.InventoryResponse;
import com.apes.orderservice.dto.OrderRequest;
import com.apes.orderservice.event.OrderPlacedEvent;
import com.apes.orderservice.model.Order;
import com.apes.orderservice.model.OrderLineItems;
import com.apes.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final WebClient.Builder webClientBuilder;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderService(OrderRepository repository, WebClient.Builder webClientBuilder, KafkaTemplate kafkaTemplate) {
        this.repository = repository;
        this.webClientBuilder = webClientBuilder;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();

        List<OrderLineItems> orderLineItemsList = orderRequest.orderLineItemsDtoList()
                .stream()
                .map(OrderLineItems::convert)
                .toList();

        List<String> skuCodes = orderLineItemsList
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        order.getOrderLineItemsList().addAll(orderLineItemsList);

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build()
                )
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductMatches = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

        if (allProductMatches) {
            Order o = repository.save(order);
            kafkaTemplate.send("notification.topic",
                    o.getId(),
                    new OrderPlacedEvent(order.getOrderNumber()));
            return "order placed successfully";
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }
}
