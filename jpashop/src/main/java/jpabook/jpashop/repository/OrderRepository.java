package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Member;
import jpabook.jpashop.domain.item.Order;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAll() {
        return em.createQuery("select m from Order m", Order.class)
                .getResultList();
    }

    public List<Order> findAllByString(OrderSearch orderSearch){
        return em.createQuery("select o from Order o join o.member m" +
                        " where  o.status = :status " +
                        " and m.name like :name", Order.class)
                .setParameter("status",orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
//                .setFirstResult() startPosition for paging
                .setMaxResults(1000) // 최대 조회값 1000
                .getResultList();
    }

    // JPA Criteria
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
    //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
    //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();

        }

        // 엔티티를 페치 조인을 사용해서 쿼리 1번에 조회
        // 페치 조인으로 order > member, order > delivery 는 이미 조회 된 상태이므로 지연로딩 X
        public List<Order> findAllWithMemberDelivery(){
        return em.createQuery("select o from Order o" + " join fetch o.member m" +
                " join fetch o.delivery d", Order.class)
                .getResultList();
        }


        /*
        * 페치 조인으로 SQL이 1번만 실행됨.
        * distinct 를 사용한 이유는 1대다 조인이 있으므로 데이터베이스 row가 증가한다.
        * 그 결과 같은 order 엔티티의 조회 수도 증가하게 된다.
        * JPA의 distinct는 SQL에 distinct를 추가하고, 더해서 같은 엔티티가 조회되면, 애플리케이션에서 중복을 걸러준다.
        * 이 예에서 order가 컬렉션 페치 조인 때문에 중복 조회 되는 것을 막아준다.
        * 단점은 페이징이 불가능하다
        *
        * 참고: 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든 데이터를 DB에서 읽어오고,
        * 메모리에서 페이징 해버린다(매우 위험하다). 자세한 내용은 자바 ORM 표준 JPA 프로그래밍의 페치 조인 부분을 참고하자.
        > 참고: 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다.
        * 데이터가 부정합하게 조회될 수 있다. 자세한 내용은 자바 ORM 표준 JPA 프로그래밍을 참고하자.
        * */
        public List<Order> findAllWithItem() {
            return em.createQuery("select distinct o from Order o" +
                            " join fetch o.member m" +
                            " join fetch o.delivery d" +
                            " join fetch o.orderItems oi" +
                            " join fetch oi.item i", Order.class)
                    .getResultList();
        }

        public List<Order> findAllWithMemberDelivery(int offset, int limit){
            return em.createQuery(
                    "select o from Order o" +
                            " join fetch o.member m" +
                            " join fetch o.delivery d", Order.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        }


}
