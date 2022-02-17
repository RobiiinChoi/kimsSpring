package jpabook.jpashop;

import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Rollback(value = false)
class MemberRepositoryTest {

//    @Autowired
//    MemberRepository memberRepository;
//
//    @Test
//    @Transactional
//    public void testMember() throws Exception{
//        // given
//        Member member = new Member();
//        member.setUsername("memberA");
//
//        // when
//        Long savedId = memberRepository.save(member);
//        Member findMember = memberRepository.find(savedId);
//
//        // then
//        org.assertj.core.api.Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
//        org.assertj.core.api.Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
//        org.assertj.core.api.Assertions.assertThat(findMember).isEqualTo(member);
//    }
}