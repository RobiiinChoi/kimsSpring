package hello.hellospring.controller;

import hello.hellospring.domain.Member;
import hello.hellospring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MemberController {

    // 하나만 생성해놓고 그냥 공유해도 됨 (매번 호출할 때마다 생성할 필요가 없다
  private final MemberService memberService;

@Autowired // memberController가 생성될 때 memberService가 주입된 (Dependency Injection). 생성자 주입
public MemberController(MemberService memberService) {
    this.memberService = memberService;
    // memberService.setMemberRepository() : Setter로 DI 주입 시 해당 객체를 아무 개발자를 호출할 수 있음
}

    @GetMapping("/members/new")
    public String createForm(){
    return "members/createMemberForm";
}
    @PostMapping("/members/new")
    public String create(MemberForm form){
    Member member = new Member();
    member.setName(form.getName());
        System.out.println("member = " + member.getName());
    memberService.join(member);
    return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";

    }
}
