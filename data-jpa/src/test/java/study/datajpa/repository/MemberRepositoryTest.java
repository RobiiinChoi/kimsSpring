package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Rollback(false)
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    // 영속성 컨텍스트
    @PersistenceContext
    EntityManager entityManager;

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);
        Member findMember = memberRepository.findById(savedMember.getId()).get(); // null -> NoSuchElementException
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCrud(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        findMember1.setUsername("member!!!!!");

        // 리스트 조회
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy(){
        List<Member> helloBy = memberRepository.findTop3HelloBy();
    }

    @Test
    public void namedQuery(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void testQuery(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(member1);
    }

    @Test
    public void findUsernameList(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> result = memberRepository.findUsernameList();
        for(String s : result){
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);
        Member member1 = new Member("AAA", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<MemberDto> result = memberRepository.findMemberDto();
        for(MemberDto s : result){
            System.out.println("dto = " + s);
        }
    }

    @Test
    public void returnType(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);

        // JPA는 빈 컬렉션을 리턴해줌 (에러가 아니고~)
        // Spring Data JPA -> exception을 안터지게 하려고 대신 try-catch로 리턴값을 감싸줌 (NoResultException)
        // 1.8이후는 Optional이 생겼기 때문에 > 처리에 대한게 클라이언트로 넘어감. (예외가 터짐)
        // 원래 터지는 예외에서 Spring Data Jpa가 SpringFramework Exception으로 반환해서 넘김
        Member aaa = memberRepository.findMemberByUsername("ㅁㄴㄷㄹㅈㄷㄹ");
        System.out.println(aaa);
    }

    // pageable > 내부에 sort 포함
    // page > 추가 count 쿼리 결과를 포함하는 페이징
    // slice > 추가 count 쿼리 없이 다음 페이지만 확인 가능 (내부적으로 limit+1 조회) 더보기

    @Test
    public void paging() {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));

        int age = 10;

        // page 1 offset = 0, limit = 10, page2 -> offset = 11, limit = 10
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
//        Slice<Member> page = memberRepository.findByAge(age, pageRequest);

        // 엔티티로 보내는 것 보다 DTO로 보내야 API 스펙이 변하지 않기 때문에 아래 방법을 추천. 페이지를 유지하면서 dto처리 가능
        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        // then
        List<Member> content = page.getContent();
        // long totalElements = page.getTotalElements();
//        for (Member member : content) {
//            System.out.println("member = " + member);
//        }
        // System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        // assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(page.getNumber()).isEqualTo(0);
        // assertThat(page.getTotalPages()).isEqualTo(2);
        // assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdate(){

        // **** 매우 중요
        // 벌크 연산 후 db에는 member6의 46이 반영이 되어있지만 영속성 컨텍스트에는 해당 사항이
        // 업데이트 되지 않기 때문에 45로 그대로 남아있음
        // 벌크연산 이후에는 영속성 컨텍스트를 "날려"버려야 한다 > db와의 difference를 아예 배제하기 위해
        // API가 벌크연산 후 끝나면 상관없지만 추가 로직이 있을경우 문제 발생 소지가 다분하다.

        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));
        memberRepository.save(new Member("member6", 45));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);
        entityManager.flush(); // 남아있는&변경되지 않은 내용이 DB에 반영
        // entityManager.clear(); // 영속성 컨텍스트 날려버리기

        List<Member> result = memberRepository.findByUsername("member6");
        Member member6 = result.get(0);
        System.out.println("member6 = " + member6);

        // then
        assertThat(resultCount).isEqualTo(4);
    }

}