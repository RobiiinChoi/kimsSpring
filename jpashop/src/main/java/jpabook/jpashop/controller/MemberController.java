package jpabook.jpashop.controller;

import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model){
        // 빈 모델이라도 넘. Validation 등을 처리해 주기 때문에 내용이 비었어도 넘겨줌
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }
}
