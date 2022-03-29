package hello.core.autowired;

import hello.core.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class AutowiredTest {

    @Test
    void AutowiredOption() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(TestBean.class);

    }


    @Configuration
    static class TestBean {
        /*
        //NoSuchBeanDefinitionException
        @Autowired
        public void setNoBean0(Member noBean1){
            System.out.println("member = " + noBean1);
        }

        */

        //호출이 아예 안됨
        @Autowired(required = false)
        public void setNoBean1(Member noBean1){
            System.out.println("member = " + noBean1);
        }

        //member = null 널값으로 들어감
        @Autowired
        public void setNoBean2(@Nullable Member noBean2) {
            System.out.println("member = " + noBean2);
        }

        //member = Optional.empty 로 들어감
        @Autowired
        public void setNoBean3(Optional<Member> noBean3) {
            System.out.println("member = " + noBean3);
        }
    }

}
