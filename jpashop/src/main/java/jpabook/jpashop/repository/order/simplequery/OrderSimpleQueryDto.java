package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.domain.item.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate; //주문시간 private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime
            orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
    }
}
