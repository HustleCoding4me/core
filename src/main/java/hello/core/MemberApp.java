package hello.core;

import hello.core.member.Grade;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;

import hello.core.member.Member;

public class MemberApp {
    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();

        Member memberA = new Member(1L, "memberA", Grade.VIP);
        memberService.join(memberA);//회원가입

        Member member = memberService.findMember(1L);
        System.out.println("findMember : "  + member.getGrade());
        System.out.println("new member : " + memberA.getName());

    }
}
