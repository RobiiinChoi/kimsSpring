package com.study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.study.querydsl.entity.QMember.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void testEntity() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // 초기화
        em.flush();
        em.clear();

        // 확인
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println(member);
            System.out.println(member.getTeam());
        }
    }

    @Test
    public void startJPQL() {
        // member1을 찾기
        Member findByJPQL = em.createQuery(
                        "select m from Member m " +
                                "where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findByJPQL.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        // 1) m이라는 별칭 직접 지정
        // QMember m = new QMember("m");
        // 같은 테이블을 조인해야 할 때, 이름이 겹치지 않도록 별칭 사용 > 안그러면 바로 3번처럼 static import 해서 사용하는게 좋음

        // 2) 기본 인스턴스 사용
        // QMember q = QMember.member;

        // 3) 더 짧게 줄이는법 > QMember.member > static 처리
        // querydsl은 결과적으로 jpql의 빌더 역할***
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .or(member.age.eq(10))) // and : 조건추가
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                ) // and 대신 , 로 분리해서 표현할 수도 있다.
                // 후자의 경우 null값이 중간에 들어가더라도 무시하기 때문에 동적쿼리 생성시 코드가 깔끔해짐
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    public void resultFetch(){

        // List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // count
        long total = fetch.size();

        // single
        Member fetchOne = queryFactory.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        // single first
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst(); // limit(1).fetch() 와 동일
        /*
        * ** 중요
        * fetchResults(), fetchCount() 는 Deprecated됨 => 다중 쿼리 그룹에서 완벽하게 지원되지 않아서.
        * 사용 시
        * 1) fetchCount() > fetch().size() 로 변경
        * 2) fetchResults() > offset() / limit()를 fetch()전 항목을 걸어두고 fetch() 진행
        *
        * ex) List<User> content = queryFactory
			.selectFrom(user)
			.where(user.username.like("user_"))
			.offset(pageable.getOffset()) // offset
			.limit(pageable.getPageSize()) // limit
			.fetch();
        *
        * */

//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults(); fetchResults는 decprecated 되었음 (fetch 대신 사용할 것)
//
//        results.getTotal();
//        List<Member> content = results.getResults();
//        results.getLimit(); 페이징 처리 할 수 있는 메서드를 가져다 줌
//        results.getOffset();



    }
}
