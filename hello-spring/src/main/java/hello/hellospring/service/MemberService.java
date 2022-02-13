package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemberRepository;
import hello.hellospring.repository.MemoryMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public class MemberService {

    private MemberRepository memberRepository;

    // Dependency Injection. 생성자 주입. 생성시점만 주입되고 그 다음 부터는 변경할 수 없도록 막을 수 있음
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원가입
    public Long join(Member member){

        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    // ctrl + T > 별도로 뽑아주는 함수 (메소드로 바꿀 수 있음)
    // null일 가능성 때문에 optional로 한번 감싸줌
    // ifPresent : 값이 있으면 아무것도 안하는게 아니라 해당 값으로 액션을 취하라는 메소드
    // 중복회원 방지 메소드
    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        });
    }

    // 전체 회원 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    // 개별 회원 조회
    public Optional<Member> findOne(Long memberId){
        return memberRepository.findById(memberId);
    }

}
