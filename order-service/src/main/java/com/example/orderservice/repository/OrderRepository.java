package com.example.orderservice.repository;

import com.example.orderservice.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("select o from OrderEntity o where o.orderId = :orderId")
    Optional<OrderEntity> findByOrderId(@Param("orderId") String orderId);

    @Query("select o from OrderEntity o where o.userId = :userId")
    List<OrderEntity> findByUserId(@Param("userId") String userId);
}
