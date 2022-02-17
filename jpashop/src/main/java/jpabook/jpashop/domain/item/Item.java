package jpabook.jpashop.domain.item;

import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
// strategy : join(가장 정교), single_table(한테이블에 다 통합),
//table_per_class(상속받은 각 객체가 각각의 테이블로 나옴)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// 카테고리화 함
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // 비즈니스 로직 (엔티티 안에서 비즈니스 로직 생성이 가능하면 해도 좋다 (이미 데이터를 가지고 있기 때문에 옮길 필요 없으므로 => 객체지향)
    /*
    **** @Setter 대신 값을 변경하고 싶으면 아래 같은 메서드로 비즈니스 로직을 구현하여 값을 바꿔줘야 한다
    * */
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if (restStock<0){
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }

}
