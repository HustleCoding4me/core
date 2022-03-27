package hello.core;

import hello.core.member.Grade;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;

import hello.core.member.Member;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MemberApp {
    public static void main(String[] args) {
/*
Spring 이전 방법
        AppConfig appConfig = new AppConfig();
        MemberService memberService = appConfig.memberService();
*/
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        MemberService memberService = applicationContext.getBean("memberService",MemberService.class);

        Member memberA = new Member(1L, "memberA", Grade.VIP);
        memberService.join(memberA);//회원가입

        Member member = memberService.findMember(1L);
        System.out.println("findMember : "  + member.getGrade());
        System.out.println("new member : " + memberA.getName());

    }
}
