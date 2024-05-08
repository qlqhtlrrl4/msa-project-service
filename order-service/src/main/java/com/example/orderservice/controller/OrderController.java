package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.messagequeue.OrderProducer;
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
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/order-service")
public class OrderController {

    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String healthCheck() {
        return "It's Working in Order Service on PORT : "+ env.getProperty("local.server.port");
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<?> createOrder(@PathVariable("userId") String userId,
                                         @RequestBody RequestOrder orderDetail) {

        log.info("Before add orders data");
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(orderDetail, OrderDto.class);
        orderDto.setUserId(userId);

        //jpa
        OrderDto createOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createOrder, ResponseOrder.class);

        //kafka
        //orderDto.setOrderId(UUID.randomUUID().toString());
        //orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        //kafka send order to kafka
        //kafkaProducer.send("example-catalog-topic",orderDto);
        //orderProducer.send("orders", orderDto);

        //ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);

        log.info("After add orders data");
        return new ResponseEntity<>(responseOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<?> getOrdersUserId(@PathVariable("userId") String userId) throws Exception {

        log.info("Before retrieve orders data");
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);

        List<ResponseOrder> result = new ArrayList<>();

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        orders.forEach(o -> {
            result.add(mapper.map(o, ResponseOrder.class));
        });

        // 강제 에러
        try {
            Thread.sleep(1000);
            throw new Exception("장애 발생");
        } catch(InterruptedException e) {
            log.warn(e.getMessage());
        }

        log.info("After retrieve orders data");

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId) {

        OrderDto findOrder = orderService.getOrderByOrderId(orderId);

        ResponseOrder order = new ModelMapper().map(findOrder, ResponseOrder.class);

        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
