package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional // 읽기에는 readOnly 옵션이 좀더 빠르고 좋다
@RequiredArgsConstructor // 스프링이 생성자 하나면 자동으로 주입해줌
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입
    public Long join(Member member){
        validateDuplicateMember(member); // 중복회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    // 단건 조회
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    // 회원 수정
    @Transactional
    public void update(Long id, String name){
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}