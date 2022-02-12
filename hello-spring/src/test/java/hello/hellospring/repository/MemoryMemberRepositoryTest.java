package hello.hellospring.repository;

import hello.hellospring.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MemoryMemberRepositoryTest {

    MemoryMemberRepository repository = new MemoryMemberRepository();

    // 리포 실행할 때마다 저장내역 클리어
    // *** 테스트는 각 테스트마다 의존관계 없이 설계가 되어야 한다 ***
    // TDD : Test Driven Development  => 개발 기능 구현을 먼저하는것이 아니라 그 기능을 테스트하는 틀을 먼저 만들고
    // 테스트의 이상이 없을 시 기능 구현을 진행하는 방법
    @AfterEach
    public void afterEach(){
        repository.clearStore();
    }
    @Test
    public void save(){
        Member member = new Member();
        member.setName("spring");

        repository.save(member);
        // optional에서 값을 꺼낼 때 get
        Member result = repository.findById(member.getId()).get();
        Assertions.assertEquals(member, result);
        assertThat(member).isEqualTo(result);
    }

    @Test
    public void findByName(){
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);

        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);

        Member result = repository.findByName("spring1").get();

        assertThat(result).isEqualTo(member1);

    }

    @Test
    public void findAll(){
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);


        Member member2 = new Member();
        member2.setName("spring2");
        repository.save(member2);

        List<Member> result = repository.findAll();

        assertThat(result.size()).isEqualTo(3);
    }




}
