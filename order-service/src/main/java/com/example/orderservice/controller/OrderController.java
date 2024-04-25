package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/order-service")
public class OrderController {

    private final Environment env;
    private final OrderService orderService;

    @GetMapping("/health_check")
    public String healthCheck() {
        return "It's Working in Order Service on PORT : "+ env.getProperty("local.server.port");
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<?> createOrder(@PathVariable("userId") String userId,
                                         @RequestBody RequestOrder order) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(order, OrderDto.class);
        orderDto.setUserId(userId);
        orderService.createOrder(orderDto);

        ResponseOrder result = mapper.map(orderDto, ResponseOrder.class);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<?> getOrdersUserId(@PathVariable("userId") String userId) {
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> result = new ArrayList<>();

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        orders.forEach(o -> {
            result.add(mapper.map(o, ResponseOrder.class));
        });

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) {

        OrderDto findOrder = orderService.getOrderByOrderId(orderId);

        ResponseOrder order = new ModelMapper().map(findOrder, ResponseOrder.class);

        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
