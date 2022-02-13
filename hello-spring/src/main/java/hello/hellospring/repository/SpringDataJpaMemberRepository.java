package hello.hellospring.repository;

import hello.hellospring.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {

    // 공통화 할 수 있는 기본 CRUD는 JpaRepository에 묶여있음 (아이디로 찾기, 저장하기 ...)
    // JPQL: select m from Member m where m.name = ?
    // 인터페이스만으로도 쿼리가 짜지는 마법!

    // Spring Data JPA
    // 1) 인터페이스를 통한 기본 CRUD
    // 2) findByName(), findByEmail() 처럼 메서드 이름만으로 조회 가능
    // 3) 페이징 기능 자동 제공
    // 실무에서는 Spring-Data-Jpa(base) + Querydsl(동적)
    // 이걸로 해결이 안됀다 > Jpa native query or JdbcTemplate query
    @Override
    Optional<Member> findByName(String name);
}
