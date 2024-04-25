package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    @Override
    public OrderDto createOrder(OrderDto orderDto) {

        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderEntity order = mapper.map(orderDto, OrderEntity.class);

        orderRepository.save(order);

        return orderDto;

    }

    @Override
    public OrderDto getOrderByOrderId(String orderId) {

        Optional<OrderEntity> findOrder = orderRepository.findByOrderId(orderId);

        if(findOrder.isEmpty()) {
            throw new IllegalStateException("no search orderId");
        }

        return new ModelMapper().map(findOrder.get(), OrderDto.class);
    }

    @Override
    public List<OrderDto> getOrdersByUserId(String userId) {

        List<OrderEntity> orders = orderRepository.findByUserId(userId);
        List<OrderDto> result = new ArrayList<>();

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        orders.forEach(o-> {
            result.add(mapper.map(o, OrderDto.class));
        });

        return result;
    }
}
