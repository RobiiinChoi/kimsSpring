package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    private final EntityManager em;

    // 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
    // new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
    // select 절에서 원하는 데이터를 직접 선택 DB > app 용량 최적화 (생각보다 미비)
    // 리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
    public List<OrderSimpleQueryDto> findOrderDtos(){
        return em.createQuery("select new jpabook.jpashop.repository.order.simplequery" +
                ".OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.member m" +
                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();

    }
}
