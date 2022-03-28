package hello.core.beanfind;

import hello.core.AppConfig;
import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationContextSameBeanFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SameBeanConfig.class);

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 중복 오류가 발생한다")
    public void findBeanByTypeDuplicate() throws Exception {
        //given
        assertThrows(NoUniqueBeanDefinitionException.class, () -> ac.getBean(MemberRepository.class));
        //when
        //then
    }

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면 빈 이름을 지정하면 됩니다.")
    public void findBeanByName() throws Exception {
        //given
        MemberRepository memberRepository1 = ac.getBean("memberRepository1", MemberRepository.class);
        //when
        assertThat(memberRepository1).isInstanceOf(MemberRepository.class);
        //then
    }

    @Test
    @DisplayName("틀정 타입을 모두 조회하기")
    public void findAllBeanByType() throws Exception {
        Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);

        for (String s : beansOfType.keySet()) {
            System.out.println("key = " + s + " value = " + beansOfType.get(s));
        }

        assertThat(beansOfType.size()).isEqualTo(2);

    }

    @Configuration
    static class SameBeanConfig {
        @Bean
        public MemberRepository memberRepository1() {
            return new MemoryMemberRepository();
        }

        @Bean
        public MemberRepository memberRepository2() {
            return new MemoryMemberRepository();
        }

    }

}
