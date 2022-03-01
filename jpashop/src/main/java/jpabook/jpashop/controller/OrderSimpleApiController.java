package jpabook.jpashop.controller;


import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.domain.item.Order;
import jpabook.jpashop.domain.item.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * xToOne(ManyToOne, OneToOne) 관계 최적화 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /*
     *   V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리 * - 양방향 관계 문제 발생 -> @JsonIgnore
     * */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }
    /*
        엔티티를 직접 노출하는 것은 좋지 않다. (앞장에서 이미 설명)
        order > member 와 order > address 는 지연 로딩이다. 따라서 실제 엔티티 대신에 프록시 존재
        jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름 Hibernate5Module 을 스프링 빈으로 등록하면 해결(스프링 부트 사용중)
    * */
    /*
    *   즉시로딩은 JPQL에서 N+1문제를 일으킨다.
    *   em.find()하면 JPA가 조인으로 쿼리 최적화 해서 가져오지만 jpql은 sql로 번역되어서 나가기 때문에 join되서 가져오지 않는다.
    *   EAGER 로딩으로 설정되어 있으면 일단 member 엔티티 조회 후 연관된 엔티티 다시 조회 후 채워줌.
    *   추가 쿼리가 발생한다 (추가로 채워줘야 할 데이터 개수 N개만큼 쿼리 발생 > 처음 날린 쿼리 1 + 결과값들 쿼리 N : N + 1 문제
    *
    *   *** @ManyToOne, @OneToOne은 기본이 즉시로딩 > LAZY로 설정할 것
    *   *** @OneToMany, @ManyToMany는 기본이 지연로딩
    *
    *   모든 연관관계에 지연 로딩 사용해라!!
        실무에서 즉시 로딩 사용하지 마라!!
        JPQL fetch 조인이나, 엔티티 그래프 기능을 사용해라!
        즉시 로딩은 상상하지 못한 쿼리가 나간다!!

    * * * */

    // V2. 엔티티 조회 후 DTO (FetchJoin X)
    // 단점 : 지연 로딩으로 쿼리 N번 호출


    // 쿼리가 1 + N + N 실행 (v1과 쿼리 수 동일)
    // order 조회 1번 (order 조회 결과 수가 N)
    // order > member 지연로딩 N번
    // order > delivery 지연로딩 N번
    // ex ) order 결과가 4개면 최악의 경우 1 + 4 + 4번 실행(최악)
    // 지연로딩은 영속성 컨텍스트에서 조회, 이미 조회된 경우 쿼리 생략
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAll();
        List<SimpleOrderDto> result = orders.stream().map(o-> new SimpleOrderDto(o))
                .collect(toList());

        return result;
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; // 주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함) */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream().map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    // V4 : JPA에서 DTO로 바로 조회
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

}
