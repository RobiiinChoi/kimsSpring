package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    List<Member> findHelloBy();

    List<Member> findTop3HelloBy();

    // @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    // 이름이 없는 NamedQuery임
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username); // collection
    Member findMemberByUsername(String username);  // single

    Optional<Member> findOptionalByUsername(String username); // single Optional

    // 쿼리가 복잡해지면 카운트쿼리 분리가 필요하다
    // sorting이 너무 많아지면 그냥 쿼리로 처리해야한다.
    // 주의할 점 : 페이지의 인덱스는 1이 아닌 0부터 시작이다 :)
    @Query(value = "select m from Member m left join m.team t", countQuery = "select count(m.username) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);
//        Slice<Member> findByAge(int age, Pageable pageable);


    /*
    *   JPA 벌크 연산의 주의할 점 : 원래 jpa 영속성 컨테스트 안에서 관리되야 될 것들이 한번에 쿼리가 실행된다
    *   영속성 관리가 되던 엔티티들이 한번에 벌크로 디비 반영되어 버리므로 문제가 발생할 수 있다.
  . * */

    // update 같은 쿼리가 있을 경우 영속성 컨텍스트 내의 데이터를 플러쉬 처리(디비 반영) 후 내부적으로 해당 쿼리가 실행된다.
    // @Modifying 있어야 executedUpdate 실행. 없으면 getSingleList getResultList 호출. update 쿼리에는 넣어줘야 됨
    // clear옵션을 설정하면 자동으로 영속성 컨텍스트 비워주는 처리를 한다 = em.clear()
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}
