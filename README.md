# MVC[기초]

## 싱글톤 패턴

웹 App은 보통 여러 Client가 동시에 요청을 한다.
-> 매번 요청마다 인스턴스new를 해서 객체를 생성하면, 메모리 낭비가 심하다.

따라서, 싱글톤 패턴 (한 개의 인스턴스만 생성하여 사용)을 사용해야한다.

> 클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는 디자인 패턴

>> 기본 Singleton 코드
```java
public class SingletonService {

    private static final SingletonService instance = new SingletonService();

    public static SingletonService getInstace() {
        return instance;
    }

    // 외부에서 SingletonService new 를 막기 위해 private 생성자를 선언했다.
    private SingletonService() {
    }
    
    
    
}


```

* static 영역에 최초 1회에 생성하여 instance에 참조를 넣어놓는다.
* 그래서 SingletonService를 사용할 때, getInstance()를 사용하여 불러서 사용하면 된다.
* private 생성자를 사용해서 외부에서 new로 생성자를 사용하는 것을 막아야 한다.

> 사용 예제

```java

    @Test
    @DisplayName("싱글톤 패턴을 적용한 객체 사용")
    public void singletonServiceTest() throws Exception {
        SingletonService instance = SingletonService.getInstace();
        SingletonService instance2 = SingletonService.getInstace();

        assertThat(instance).isSameAs(instance2);
        
    }

```


### 기존 Singleton 구현 문제점 
위의 방법은 여러 문제가 있다. (Spring 사용 X시)
* 공수가 많이 들어간다.
* 클라이언트가 구현체에서 getInstance()를 하기 때문에, DIP 원칙 어긴다.
* 구체 클래스를 의존하기 때문에 OCP 원칙 또한 어긴다.
* 


> Spring 컨테이너는 이 Singleton 사용을 도와준다. (Bean으로)

```java
    @Test
    @DisplayName("스프링 컨테이너 + 싱글톤")
    public void testSingletonSpring() throws Exception {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService1 = ac.getBean("memberService", MemberService.class);
        MemberService memberService = ac.getBean("memberService", MemberService.class);

        assertThat(memberService).isSameAs(memberService1);
    }

```
* 그냥 스프링 DI 컨테이너에서 꺼내서 사용하면 된다.


### 주의점

Singleton패턴의 특성상, 모든 유저가 한개의 인스턴스를 공유하기 때문에,
최대한 무상태(stateless)로 설계해야 한다.

> 무상태
> > 특정 클라이언트에 의존적인 필드가 있으면 안된다.
> 
> > 특정 클라이언트가 값을 변경하는 필드가 있으면 안된다.
> 
> > 가급적 Read만 가능해야 한다.
> 
> > 필드 대신에 자바에서 공유되지 않는 지역번수, 파라미터, ThreadLocal 등을 사용해야 한다. ( 공유값이 있으면 장애가 난다.)


### 무상태로 짜는 방법

공유되는 가변 변수 없이, 직접 받으면 된다.
ex) 사용자의 주문 금액을 받을 때

> 유상태

```java

public class StatefulService {

    private int price; //상태를 유지하는 필드

    public void order(String name, int price) {
        this.price = price;//여기가 문제
    }

    public int getPrice() {
        return price;
    }
}

```

>> 문제점
* 지역 변수 price에 order 메서드로 모든 접근 쓰레드가 값을 담는다.
* 동시에 쓰레드가 접근해 값을 변경하면, 마지막 변경된 숫자가 반환된다. (stateful하기 때문)

```java
@Test
    public void statefulServiceSingleton() throws Exception {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

        StatefulService statefulService1 = ac.getBean(StatefulService.class);
        StatefulService statefulService2 = ac.getBean(StatefulService.class);

        //ThreadA : A 사용자 10000원 주문
        statefulService1.order("userA", 10000);

        //ThreadB : B 사용자 20000원 주문
        statefulService2.order("userB", 20000);

        //ThreadA: 사용자A 주문 금액 조회
        //사용자 A가 조회요청을 했기 때문에 예상액 10000, 그러나 20000나온다.
        //Singleton이기 때문에 마지막 요청이 들어온 금액이 들어간다.
        int price = statefulService1.getPrice();
        System.out.println("price = " + price); //price = 20000
    }
```



### 해결 방법 (무상태로 설계시)

```java
public class StatefulService {

    //private int price; //상태를 유지하는 필드를 제거

    /*public void order(String name, int price) {
        this.price = price;//
    }*/

    public int order(String name, int price) {
        return price;//그냥 주어진 price를 return한다.
    }
    
    /*public int getPrice() {
        return price;
    }*/
}

```

* 공유되는 지역변수를 제거하고, 그냥 price를 바로 return하여 무상태 완성.

### + @Configuration의 역할
 
* @Bean 들을 스프링 DI 컨테이너에 적재하면서, Singleton 유지에 도움이 된다.
* `바이트코드를 조작하는 CGLIB` 기술을 사용하게 해준다. (기능은 아래)
* CGLIB -> 이미 등록되어있는 인스턴스는 new 하지 않고 컨테이너에서 가져와 다른 Bean들이 의존할 경우 Singleton을 유지해준다.
* Configuration Class를 직접 사용하지 않고 CGLIB으로 감싸서 프록시객체처럼 변환하여 사용한다.
* 설정 class에 @Configuration이 없으면 return new 해주는 자바class 그대로 작동하기 때문에, Singleton이 깨진다.
* 굳이굳이 @Configuration을 안쓴다면, 다른데 참조되는 생성자에는 @Autowired로 꺼내 쓴 객체를 주입해주면 된다.(그치만 굳이?)

> 컴포넌트 스캔?
-> @Bean으로 선언하지 않더라도 DI 컨테이너에 적재하게 스프링에서<br>
> 어노테이션을 보고 자동 적재해준다.
> 대표적으로 @Component(@Repository,@Controller 등은 이미 @Component가 달렸다)
> 이후 @Autowired로 의존관계 주입을 받아 사용하면 된다.
* @ComponentScan의 경우 default로 해당 어노테이션의 package 기준으로 하위것들을 모두 scan하게 되어있다.
* 최상단에 설정파일을 위치해두면 추가적인 설정이 필요하지 않다.

//요즘은 @SpringBootApplication을 기본 최상단 app에 두는게 트랜드다.
//내부에 @ComponentScan이 있다.
```java

@SpringBootApplication
public class CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreApplication.class, args);
	}

}


//@SpringBootApplication

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
**************************
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
**************************
public @interface SpringBootApplication {

```

//최상단에 두지 않는다면? or 따로 범위를 규제하고 싶다면


```java
@ComponentScan(
        basePackages = "hello.core", // hello.core 하위의 모든 패키지들을 scan
        basePackageClasses = MemberService.class,//memberService 클래스를 기준으로 하위 package를 뒤진다.
        excludeFilters = @ComponentScan.Filter
        (type = FilterType.ANNOTATION, classes = Configuration.class)
        //@Configuration 어노테이션이 붙은 것들은 제외한다.

)

```
























## 의존 관계 주입 방법

* 생성자 주입
* 수정자 주입(Setter)
* 필드 주입
* 일반 메서드 주입

> 생성자 주입
* 1번만 호출되기 때문에, final, 불변한 필수 의존관계에 사용된다.
>> final 변수의 경우 class 내부에서 꼭 사용되어야 하기 때문에 final 변수는 setting 해달라고 선언하는 것이다.
* (setter가 없어야 함)

```java

 private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    
```

* 생성자가 1개만 있다면 @Autowired 생략해도 의존관계가 주입된다.

> 수정자 주입

* 선택, 수정이 필요할 경우 사용한다. (final 사용 불가)
* 변수 하나하나 선택적으로 의존관계를 주입하여 사용가능하다는 장점. @Autowired(required = false)로 필수값을 꺼줄 수 있다.

```java

    private MemberRepository memberRepository;
    private DiscountPolicy discountPolicy;

    @Autowired(required = false)
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository
    }

    @Autowired
    public void setDiscountPolicy(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

```

---
### 스프링 빈 등록 과정은 2단계이다.
1. 먼저 생성자에서 쭉 땡겨서 의존관계를 생성하고,
2. 이후 setter 등에서 의존관계를 생성한다.
---


> 필드 주입

* 지금은 권장되지 않는 안티패턴이다.
* 외부에서 변경이 불가능해서 테스트가 불가능하다.(생성자는 가능하다.)
* DI 컨테이너 없이 자바코드 테스트가 불가능하다.
* 따라서, 테스트코드같이 다른 곳에서 참조가 되지 않는 것들에 사용하면 좋다.
* 또는 @Configuration 파일들(스프링에서만 사용할 것이기 때문)


```java
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private DiscountPolicy discountPolicy;

```


> 일반 메서드 주입

* 잘 사용하지는 않는다.
* 그냥 생성자 주입, 수정자 주입으로 다 처리되기 때문에, 그냥 참고용으로 보자.

```java

@Autowired
public void init(MemberRepository memberRepository, DiscountPolicy discountPolicy){
            this.memberRepository = memberRepository;
            this.discountPolicy = discountPolicy;
        }

```


### 주입할 스프링 빈이 없어도 동작시키고 싶은 경우
자동 주입 대상을 옵션으로 처리하는 방법 3가지


> Options들
>> Member는 Spring Bean에 등록되어있지 않다.

#### 0. 기본 @Autowired(required = true)

```java
   @Autowired
   public void setNoBean0(Member noBean1){
   System.out.println("member = " + noBean1);
   }
```
* NoSuchBeanDefinitionException이 터진다. (Member가 빈에 없으므로)   

 
#### 1. @Autowired(required = false)

```java
//호출이 아예 안됨
@Autowired(required = false)
public void setNoBean1(Member noBean1){
System.out.println("member = " + noBean1);
}
```
* 자동 주입할 대상이 없으면 수정자 메서드 자체가 호출이 안된다.

#### 2. `org.springframework.lang.@Nullable`

```java
//member = null 널값으로 들어감
@Autowired
public void setNoBean2(@Nullable Member noBean2) {
System.out.println("member = " + noBean2);
}
```
* 자동 주입할 대상이 없으면 null이 입력된다.

 
#### 3. Optional<>

```java
 //member = Optional.empty 로 들어감
@Autowired
public void setNoBean3(Optional<Member> noBean3) {
System.out.println("member = " + noBean3);
}
```
*  자동 주입할 대상이 없으면 `Optional.empty`가 입력된다.


### 왜 최근 스프링부트는 생성자 주입을 추천해주는가?

* 처음 조립시에 의존 관계 주입이 되는데, 불변해야 한다. 수정하는 경우는 거의 없음.
* 생성자 주입은 객체 생성시 1번 호출되어 불변하게 설계 가능하다.
* 프레임워크 없이 순수 자바 test시에 `Setter 주입`을 택하면, new로 객체 생성시에(스프링 사용 x) 주입이 안된 상태로 만들어진다.
* 변수에 `final` 값을 넣을 수 있다. (생성자에서만 주입 가능 & 무조건 초기 주입되게 선언) 
-> 생성자에서 값이 설정되지 않는 오류를 컴파일시에 막을 수 있다.

> 프레임 워크에 의존하지 않고 순수한 자바 언어의 특징을 잘 살리는 방법이다.
> 기본적으로 생성자 주입 사용, 선택값은 수정자 주입 방식으로 동시에 사용하기도 한다.

> Setter 주입  순수자바 테스트시

```java
@Test
void createOrder(){
   OrderServiceImpl orderService = new OrderServiceImpl();
   orderService.createOrder(...);
        }
```
* 나머지 의존관계 누락된 채로, java로 객체 생성했다.

> 생성자 주입 순수자바 테스트시

```java
@Test
void createOrder(){
        OrderServiceImpl orderService = new OrderServiceImpl(new MemoryMemberRepossitory(), new FixDiscount());
        orderService.createOrder(...);
        }

```
* 수동으로 원하는 구현체를 주입해줄 수 있다.



































# 다형성을 활용해 동일 타입 구현체 선택하여 사용하기

> 상황 1. 할인 정책이 2가지가 있다. 하나는 무조건 1000원, 하나는 구매 금액 10퍼센트이다.
> 개발자 A씨는 경우에 따라 선택적으로 다른 동작을 취하게 하고 싶다.

1. @Autowired로 의존성 주입받아 할인 interface의 구현체들을 Map or List에 받는다. (Map에 받을것임)
2. 자동으로 주입된 할인정책 Map에서 원하는 구현체들을 꺼내어 원하는 상황마다 바꿔가며 사용할 것이다.
3. 물론 클라이언트는 interface만 의존하고, 어떤 정책을 사용할지는 외부에서 인자로 받는다.


### 구현

Service단에서 로직을 구현했다.

```java

    @Service
     class DiscountService {
        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;
        
        //1. Map과 List로 생성자에게 의존관계주입을 요청한 모습이다.
        @Autowired
        public DiscountService(Map<String, DiscountPolicy> policyMap, List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;
            System.out.println("policyMap = " + policyMap + ", policies = " + policies);
        }

        //2. 외부에서 Bean 네임을 주입받아 경우에 따라서 다른 할인 전략을 사용할 수 있게 했다.
        public int discount(Member userA, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);

            return discountPolicy.discount(userA, price);
        }
    }

```

>> 생성자에 @Autowired로 의존관계를 주입 받는데, 위에서 같이 Map, List에 담으면 그대로 객체가 담긴다.

>> 1. Map과 List에 담겨있는 모습 
```cmd
policyMap = {fixDiscountPolicy=hello.core.discount.FixDiscountPolicy@25243bc1, rateDiscountPolicy=hello.core.discount.RateDiscountPolicy@1e287667},
        
policies = [hello.core.discount.FixDiscountPolicy@25243bc1, hello.core.discount.RateDiscountPolicy@1e287667]
```

>> 2. 주입받은 Map으로 `discount` 메서드를 제작한다.

```java
       public int discount(Member userA, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);

            return discountPolicy.discount(userA, price);
        }
```

### 테스트코드

```java

 @Test
    public void findAllBean() throws Exception {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(DiscountService.class, AutoAppConfig.class);

        DiscountService discountService = ac.getBean(DiscountService.class);

        Member userA = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(userA, 10000, "fixDiscountPolicy"); //DiscountPolicy의 구현체 bean Name 1

        assertThat(discountService).isInstanceOf(DiscountService.class);
        assertThat(discountPrice).isEqualTo(1000);

        int discountPrice2 = discountService.discount(userA, 20000, "rateDiscountPolicy"); //DiscountPolicy의 구현체 bean Name 2

        assertThat(discountPrice2).isEqualTo(2000);
    }

```


### 개선점

DiscountService는 오로지 `DiscountPolicy` 라는 interface에만 의존하여 `DIP` 원칙도 지켰고,
자바의 다형성을 잘 사용하였다.

Code 받는 부분을 Enum으로 객체화하여 사용하면 좋을 것 같고, 
@Qualifier나 @Primary 등을 사용해 주입받는 것이 더 좋을지 어쩔지는 팀원들과 상의해봐야 할 문제인 것 같다.





















