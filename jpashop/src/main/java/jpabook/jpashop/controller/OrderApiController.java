package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Address;
import jpabook.jpashop.domain.item.Order;
import jpabook.jpashop.domain.item.OrderItem;
import jpabook.jpashop.domain.item.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경
 * 가능) *
 * V4.JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1+NQuery)
 * - 페이징 가능
 * V5.JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1+1Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능... *
 */

/*
    * 엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다.
    * 둘중 상황에 따라서 더 나은 방법을 선택하면 된다.
    * 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다.
    * 따라서 권장하는 방법은 다음과 같다.
    *
    *   1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
        2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다. (페치조인으로 쿼리 한번으로 가져옴)
        3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
        4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접
        사용한다. > 네이티브 SQL (JPQL의 경우 특정 DB에 종속적인 기능은 지원하지 않기 떄문에, 여러 이유로 사용 불가능할 때 SQL 직접 사용
        * > 영속성 컨텍스트 기능 그대로 사용 가능 )
    * */

// API 개발 고급 - 컬렉션 조회 최적화
/*
 *   주문내역 > 추가 주문 상품 정보 조회
 *   Order > OrderItem, Item 필요
 *   @OneToMany (컬렉션 조회)
 *   컬렉션 조회는 더 쿼리가 많이 나가기 때문에 최적화에 대한 고민이 더 필요하다
 *
 */

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리 * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
//        List<Order> all = orderRepository.findAll();
//        for (Order order : all){
//            order.getMember().getName(); // Lazy 강제 초기화
//            order.getDelivery().getAddress(); // Lazy 강제 초기화
//            List<OrderItem> orderItems = order.getOrderItems();
//            orderItems.stream().forEach(o -> o.getItem().getName()); // Lazy 강제 초기화
//        }
//        return all;
        // Hibernate modulㄷ 자체에서 프록시를 강제초기화를 하게되면 데이터가 있음을 감지 > 뿌림 (그래서 강제초기화 해줌)
        // 양방향의 경우 무한루프 때문에 반드시 JsonIgnore를 해줘야함
        // 엔티티를 직접 노출하므로 좋은 방법이 아님
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // orderItem Lazy 강제 초기화
        }
        return all;
    }

    // orderDto라는 객체 자체를 통으로 넘기되, 그안에 있는 값들만 넘기는거임
    // orderDto : ~ 가 아니고 그 안의
    // orderId : "1",
    // name : "JPA2" 이런식으로

    // 지연 로딩으로 너무 많은 SQL 실행 SQL 실행 수
    // order 1번
    // member , address N번(order 조회 수 만큼) orderItem N번(order 조회 수 만큼)
    // item N번(orderItem 조회 수 만큼)
    // 참고: 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL을 실행한다.
    // 따라서 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회하면 SQL을 실행하지 않는다.
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    // 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화
    // DB입장에서 1:N인 경우 조인하면 N으로 뻥튀기 됨(왜냐면 N개만큼의 데이터가 연결되어 있기 때문)
    // DB는 이걸 어떤식으로 다룰지 모른다 > 우리는 오더 자체에 대해서는 뻥튀기 하고싶지 않음
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream().map(o -> new OrderDto(o))
                .collect(toList());

        for (Order order : orders) {
            System.out.println("order ref= " + order);
            System.out.println("order id= " + order.getId());
        }

        return result;
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /*
        * 주문 조회 V3.1: 엔티티를 DTO로 변환 - 페이징과 한계 돌파
            컬렉션을 페치 조인하면 페이징이 불가능하다.
            컬렉션을 페치 조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가한다.
            일다대에서 일(1)을 기준으로 페이징을 하는 것이 목적이다. 그런데 데이터는 다(N)를 기준으로 row 가 생성된다.
            Order를 기준으로 페이징 하고 싶은데, 다(N)인 OrderItem을 조인하면 OrderItem이 기준이 되어버린다.
            (더 자세한 내용은 자바 ORM 표준 JPA 프로그래밍 - 페치 조인 한계 참조)
            이 경우 하이버네이트는 경고 로그를 남기고 모든 DB 데이터를 읽어서 메모리에서 페이징을 시도한다. 최악의 경우 장애로 이어질 수 있다.
            *
            * 그러면 페이징 + 컬렉션 엔티티를 함께 조회하려면 어떻게 해야할까?
              지금부터 코드도 단순하고, 성능 최적화도 보장하는 매우 강력한 방법을 소개하겠다.
              대부분의 페이징 + 컬렉션 엔티티 조회 문제는 이 방법으로 해결할 수 있다.
              먼저 ToOne(OneToOne, ManyToOne) 관계를 모두 페치조인 한다.
              ToOne 관계는 row수를 증가시키지 않으므로 페이징 쿼리에 영향을 주지 않는다.
            * 컬렉션은 지연 로딩으로 조회한다.
              지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size , @BatchSize 를 적용한다.
              hibernate.default_batch_fetch_size: 글로벌 설정
              @BatchSize: 개별 최적화
              이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.
        * */

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * -ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
     */
        /*
        * 쿼리 호출 수가 1+N > 1+1로 최적화된다.
        * 조인보다 DB 데이터 전송량이 최적화 된다. (Order와 OrderItem을 조인하면 Order가 OrderItem 만큼 중복해서 조회된다.
        * 이 방법은 각각 조회하므로 전송해야할 중복 데이터가 없다.) 페치 조인 방식과 비교해서 쿼리 호출 수가 약간 증가하지만,
        * 매우매우매우매우중요포인트
        * ************* DB 데이터 전송량이 감소한다. 컬렉션 페치 조인은 페이징이 불가능 하지만 이 방법은 페이징이 가능하다.
        *
        * ** 결론 **
        * ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다.
        * 따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄이고 해결하고, 나머지는 hibernate.default_batch_fetch_size 로 최적화 하자.
        *
        *
        > 참고: default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를 선택하는 것을 권장한다.
        * 1000개 넘어가면 에러나는 경우가 있음 **
        *
        * 이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 한다.
        * 1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB 에 순간 부하가 증가할 수 있다.
        * 하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
        * 1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있는지로 결정하면 된다.
        * */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset",
            defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue
                                                = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,
                limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    /*
    * Query: 루트 1번, 컬렉션 N 번 실행
        ToOne(N:1, 1:1) 관계들을 먼저 조회하고, ToMany(1:N) 관계는 각각 별도로 처리한다.
        이런 방식을 선택한 이유는 다음과 같다.
        ToOne 관계는 조인해도 데이터 row 수가 증가하지 않는다. ToMany(1:N) 관계는 조인하면 row 수가 증가한다.
        row 수가 증가하지 않는 ToOne 관계는 조인으로 최적화 하기 쉬우므로 한번에 조회하고,
        * ToMany 관계는 최적화 하기 어려우므로 findOrderItems() 같은 별도의 메서드로 조회한다.
    */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    // 쿼리 1번으로 통으로 다 묶어서 dto에 때려박기
    // 쿼리는 한번이지만 조인으로 인해 DB에서 App에 전달하는 데이터가 중복 데이터가 추가되므로 V5보다 더 느릴 수 있음( 케이스바이케이스)
    // App 추가 작업이 크고 페이징이 안된다
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());

    }

}
