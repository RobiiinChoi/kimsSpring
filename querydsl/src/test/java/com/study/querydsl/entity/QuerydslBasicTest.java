package com.study.querydsl.entity;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.study.querydsl.entity.QMember.*;
import static com.study.querydsl.entity.QTeam.*;
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
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .or(member.age.eq(10))) // and : 조건추가
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    public void searchAndParam() {
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
    public void resultFetch() {

        // List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

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
        * 1) fetchCount() > fetch().size() 로 변경. 하지만 단순히 리스트의 사이즈만 가져오기 때문에
        * totalcount는 따로 count query를 날려서 가지고 오는게 나을듯..
        *
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

    /*
     * 회원 정렬 순서
     * 1) 회원 나이 내림차순 desc
     * 2) 회원 이름 올림차순 asc
     * 단, 2에서 회원 이름이 없으면 마지막에 출력 nulls last
     * */
    @Test
    public void sort() {

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast()) // nullsFirst()도 있음
                .fetch();

        Member member5 = fetch.get(0);
        Member member6 = fetch.get(1);
        Member memberNull = fetch.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isEqualTo(null); // isNull()이랑 같음
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        List<Member> queryResult = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)
                .limit(4)
                .fetch();

        // long result = (long) queryResult.size();

        // 별도로 count query를 날려준다
        long totalCount = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        // 하지만 size()랑 count랑 다른 경우 있으니 주의!!!!!!

        assertThat(queryResult.size()).isEqualTo(totalCount);
    }

    @Test
    public void aggregation(){

        // data type이 여러개가 들어오기 때문에 (단일타입 X) > 튜플 사용
        // 실무에서는 dto로 직접 뽑아내는 방법을 많이 사용
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }
    /*
    * 팀 이름과 각 팀의 평균 연령을 구해라
    * */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // ( 10 + 20 ) / 2
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // ( 10 + 20 ) / 2
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) // left, right도 가능
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /*
    * 세타 조인 (연관관계가 없는 필드로 조인)
    * 회원의 이름이 팀 이름과 같은 회원 조인
    *
    * 모든 회원 & 모든 팀을 다 조인 시킨 후에 where 실행
    *
    * 외부 조인 불가 > 조인 on 을 사용시 외부 조인 가능능    * */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> fetch = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(fetch)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }
    /*
        회원과 팀을 조인, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
        JPQL : select m, t from Member m left join m.team on t.name = 'teamA'

        ** on절을 활용한 조인 대상을 필터링할 때,
        외부조인이 아니라 ****내부조인(inner join) 사용 시,
        where 절에서 필터링 하는것 과 동일한 기능을 가지고옴.
        따라서 내부조인이면 where, 외부조인이 필요한 경우에만 이기능을 사용하면 좋다.
     */
    @Test
    public void join_on_filtering(){
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // 연관관계 없는 엔티티 외부 조인
    // 회원의 이름이 팀 이름과 같은 대상 외부 조인
    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // .leftJoin(member.team, team) 이라고 하지 않 > on으로만 조인
                .fetch();

        for(Tuple tuple : fetch){
            System.out.println("tuple = " + tuple);
        }
    }


    @PersistenceUnit
    EntityManagerFactory emf;

    // fetch Join은 sql에서 제공하는 기능은 아니고, 연관된 엔티티를 sql 한번에 조회하는 기능 : 성능최적화
    @Test
    public void fitchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")) // fetch lazy이기 때문에 team은 조회 안됨.
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();

    }

    // fetchJoin은 뒤에 fetchJoin()으로 진행
    @Test
    public void fitchJoin(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1")) // fetch lazy이기 때문에 team은 조회 안됨.
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인").isTrue();

    }
}
