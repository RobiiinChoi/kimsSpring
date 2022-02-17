package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.Order;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // protected 생성자 생성
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // 주문가
    private int orderPrice;

    // 주문수량
    private int count;

    // 다른(new) 생성자를 허용해 주지 않는 것
//    protected OrderItem() {
//    }

    // == 생성 메서드 == //
    // item에도 가격이 있지만 할인이나 기존 가격과 다를 수 있기 때문에 따로 처리하는게 맞다
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    // == 비즈니스 로직 == //
    public void cancel() {
        getItem().addStock(count);
    }

    // == 조회 로직 (주문상품 전체 확인) == //
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
