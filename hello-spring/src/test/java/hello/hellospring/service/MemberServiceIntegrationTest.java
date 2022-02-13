package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;
import hello.hellospring.repository.MemoryMemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional // test에서 트랜잭션 어노테이션이 붙었을 때만 롤백함. 다른 곳에서는 정상적으로 트랜잭션 진행
public class MemberServiceIntegrationTest {

    @Autowired MemberService service;
    @Autowired
    MemberRepository memberRepository;

    @Test
     void 회원가입() {
        //given
        Member member = new Member();
        member.setName("spring100");
        //when
        Long saveId = service.join(member);

        //then
        Member findMember = service.findOne(saveId).get();
        org.assertj.core.api.Assertions.assertThat(member.getName()).isEqualTo(findMember.getName());
    }

    @Test
    public void 중복_회원예외() {
        //given
        Member member1 = new Member();
        member1.setName("spring");

        Member member2 = new Member();
        member2.setName("spring");
        //when
        service.join(member1);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> service.join(member2));
        org.assertj.core.api.Assertions.assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원입니다.");
    }
}
