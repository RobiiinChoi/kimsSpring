package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity @Getter @Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    // READY, COMP
    // EnumType.Ordinal : 1, 2, 3... 숫자대로. 중간에 다른 상태가 끼면 번호 뒤죽박죽
    // EnumType.String : string 타입.
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
}
