package hello.core.singleton;

import hello.core.AppConfig;
import hello.core.member.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.*;

public class SingletonTest {

    @Test
    @DisplayName("스프링 없는 순수한 DI 컨테이너")
    public void testSingleton () throws Exception {
        //given
        AppConfig ac = new AppConfig();
        //1. 조회 : 호출할 때 마다 객체를 생성하는가

        MemberService memberService = ac.memberService();
        MemberService memberService1 = ac.memberService();

        //두 개가 다른 객체로 생성된다.
        System.out.println("memberService1 = " + memberService1);
        System.out.println("memberService = " + memberService);

        //memberService1 != memberService2
        assertThat(memberService).isNotSameAs(memberService1);

    }

    @Test
    @DisplayName("싱글톤 패턴을 적용한 객체 사용")
    public void singletonServiceTest() throws Exception {
        SingletonService instance = SingletonService.getInstace();
        SingletonService instance2 = SingletonService.getInstace();

        assertThat(instance).isSameAs(instance2);

    }


    @Test
    @DisplayName("스프링 컨테이너 + 싱글톤")
    public void testSingletonSpring() throws Exception {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService1 = ac.getBean("memberService", MemberService.class);
        MemberService memberService = ac.getBean("memberService", MemberService.class);

        assertThat(memberService).isSameAs(memberService1);
    }
}
