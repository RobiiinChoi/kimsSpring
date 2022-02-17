package jpabook.jpashop.domain.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    // 연관관계 거울(mapping을 하는게 아니고 mapping된 거울이다. 해당 항목에 값을 넣는다고 해도 FK 값 변경되지 않음)
    // 이 컬렉션을 가급적 꺼내지 않고, 수정하거나 변경하면 안됌. 있는걸 걍 쓰는게 안전
    // (하이버네이트가 제공하는 내장 컬렉션으로 바뀌기 때문에 임의로 변경 시 내부 메커니즘에 문제 발생할 수 있음)
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
