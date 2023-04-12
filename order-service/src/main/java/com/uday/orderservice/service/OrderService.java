package com.uday.orderservice.service;

import com.uday.orderservice.dto.InventoryResponse;
import com.uday.orderservice.dto.OrderLineItemsDto;
import com.uday.orderservice.dto.OrderRequest;
import com.uday.orderservice.model.Order;
import com.uday.orderservice.model.OrderLineItems;
import com.uday.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

  @Autowired private final OrderRepository orderRepository;
  @Autowired private final WebClient.Builder webClientBuilder;
  @Autowired private final Tracer tracer;

  public String placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
    Order order = new Order();
    order.setOrderNumber(UUID.randomUUID().toString());
    List<OrderLineItems> orderLineItemsList =
        orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).toList();
    order.setOrderLineItemsList(orderLineItemsList);

    List<String> skuCodes =
        order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

    Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");

    try(Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start()) ) {
      // Call Inventory Service, and place order if product is in stock
      InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
              .uri("http://INVENTORY-SERVICE/api/v1/inventory",
                      uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
              .retrieve()
              .bodyToMono(InventoryResponse[].class)
              .block();
      boolean allProductsInStock =
              Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
      if (allProductsInStock) {
        orderRepository.save(order);
        return "Order places Successfully";
      } else {
        throw new IllegalAccessException("Product is not in stock, please try again later.");
      }
    } finally{
      inventoryServiceLookup.end();
    }
  }

  private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
    OrderLineItems orderLineItems = new OrderLineItems();
    orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
    orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
    orderLineItems.setPrice(orderLineItemsDto.getPrice());
    return orderLineItems;
  }
}
