package hello.hellospring.repository;

import hello.hellospring.domain.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

// Jpa로 db접근
public class JpaMemberRepository implements MemberRepository{

    // gradle dependency에 JPA를 추가하면 스프링부트가 자동으로 EntityManager를 생성해준다
    // DataSource 등 내부적으로 필요한 것을 다 가지고 있기 때문에 EntityManager를 주입받아야 JPA를 사용가능
    private final EntityManager em;

    public JpaMemberRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    // pk기반으로 조회하는 것이 아닌 경우, jpql이라는 jpa 쿼리문을 작성해야한다.
    // Spring-data-jpa를 쓸 경우 이마저도 줄일 수 있음
    @Override
    public Optional<Member> findByName(String name) {
        List<Member> result = em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name",name)
                .getResultList();

        return result.stream().findAny();
    }

    // DB가 아니라 객체를 대상으로 쿼리를 날림 (from 뒤가 객체가 되는 것)
    // 쿼리 : select m from Member as m : 멤버를 통으로 조회하는 것
    @Override
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
}
