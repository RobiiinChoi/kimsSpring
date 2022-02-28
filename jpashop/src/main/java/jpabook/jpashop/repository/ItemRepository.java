package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item){
        // id 값이 null => 새로 생성한 객체
        if (item.getId() == null){
            em.persist(item);
            // id 값이 있는 경우 : 업데이트
        } else {
            // 머지 호출 : 준영속상태의 엔티티를 영속상태의 엔티티로 다시 변경시키는거임
            // 변경감지는 원하는 속성만 변경 가능하지만 병합을 사용하면 모든 속성이 변경된다.
            // 병합 시 값이 없으면 null로 업데이트 할 위험도 있다.
            em.merge(item);
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }
}
