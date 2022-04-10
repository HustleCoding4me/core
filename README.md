# MVC[기초]
4.10 일요일 commit! 내일은 출근
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







?



























# 빈 라이프 사이클 이용해서 의존관계 (외부 라이브러리의 빈 생성,파괴시에 동작과 구별해서)

* 특정 빈을 Spring에 등록하고, 그 빈의 의존관계가 주입된 후(완전히 생성된 후) 무언가 작업을 하고,<br>
삭제 전에 어떤 작업을 하고 싶을 때가 있다. 기본적으로 `@PostConstruct`, `@PreDestroy` 사용을 하면 된다.


* 그러나 외부 라이브러리를 사용할 때도 있는데, 이럴 때는 내부 코드를 수정할 수 없으므로<br> 수동으로 @Bean 등록시에 특정 메서드의 이름을 생성후, 소멸전 메서드로 등록을 해줘야 한다.


> 스프링 빈의 라이프 사이클

기본적인 `@PostConstruct`, `@PreDestroy`의 동작 시점은 아래와 같다.

>> 스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> `@PostConstruct` -> 사용로직 -> `@PreDestroy` -> 스프링 종료

// 기본 어노테이션 사용법

```java

    @PostConstruct
    public void init() throws Exception {
        //의존 관계 주입이 끝나면 호출
        System.out.println("NetworkClient.afterPropertiesSet");
        connect();
        call("초기화 연결 메세지");
    }

    @PreDestroy
    public void close() throws Exception {
        System.out.println("NetworkClient.destroy");
        disconnect();
    }

```


### 특정 라이브러리에서 생명주기에 맞춰 사용법(`@Bean(initMethod = "init",destroyMethod = "beforeDestroy")`)

* 보통 외부 라이브러리를 사용하면, `@PostConstruct`, `PreDestroy` 시점에 호출해줘야 할 경우가 생긴다.
* 그럴 때 외부 라이브러리를 @Configuration 파일의 @Bean으로 등록하면서, 특정 메서드를 호출해달라고 이름으로 설정해줄 수 있다.

> 사용법

```java
@Configuration
    static class LifeCycleConfig {
       ************************************************************
        @Bean(initMethod = "init",destroyMethod = "beforeDestroy")
        ************************************************************
        public NetworkClient networkClient() {
            NetworkClient networkClient = new NetworkClient();
            networkClient.setUrl("http://hello-spring.dev");
            return networkClient;
        }
    }
```

* 외부 라이브러리의 사용법에 맞춰 메서드명만 맞춰주면 된다.

> 💥 Bean의 `destroyMethod`는 추론이라는 기능이 내장되어 있는데, `close`,`shutdown` 이름의 메서드를 자동으로 추적하여 종료 직전에 호출해준다.
> 설정하지 않아도 작동된다고 놀라지 말자!














# 빈 스코프 (빈 생존 범위)

싱글톤은 너무나 알기 쉽기 때문에 생략하고, 특정 경우에 사용할 다른 종류를 알아보자.


> 프로토타입
>> 빈 생성 요청마다 `DI컨테이너`가 계속해서 새로운 인스턴스를 생성해주는 것. ( = 클라이언트가 요청마다 새로운 인스턴스 생성)

* 💥`DI 컨테이너`는 빈 인스턴스 생성, 의존관계 주입과 초기화 단계까지만 처리한다. 이후 스프링은 어떻게 처리하던 클라이언트에게 위임.(관리 안한다는 뜻)
* -> 따라서 `@PreDestroy` 호출 안됨. 클라이언트가 수동으로 종료해줘야 한다.

```java
@Test
    void prototypeBeanFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);
        System.out.println("find prototypeBean1");
        PrototypeBean bean = ac.getBean(PrototypeBean.class);
        System.out.println("find prototypeBean2");
        PrototypeBean bean2 = ac.getBean(PrototypeBean.class);


        Assertions.assertThat(bean).isNotSameAs(bean2);
        //같지 않다.
        
        //사용자가 수동으로 파괴해줘야 한다.
        bean.destroy();
        bean2.destroy();
      
        
    }

    @Scope("prototype")
    static class PrototypeBean {
        @PostConstruct
        public void init() {
            System.out.println("PrototypeBean.init");
        }
        @PreDestroy
        public void destroy() {
            System.out.println("PrototypeBean.destroy");
        }
    }

```


### 💥 주의사항
* 만약 Singleton 내부에 의존관계 주입된 Prototype빈이 있을 경우, Client가 Singleton 내부 변수인 Prototype 변수를 호출하여 무언가 작업 할 때,
* 내부변수 Prototype이 매번 요청마다 새로운 인스턴스가 생성되는게 아니다. Singleton 클래스의 Prototype 내부 변수에 에 참조 주소값이 이미 생성되어 저장되어있기 때문에,
* 계속해서 같은 Prototype 인스턴스가 불려와 작동하기 때문에, Prototype 의미가 없어진다. (매번 새로운 인스턴스가 생성되어 작동하는게 아니므로)

-> 결론, 싱글톤 빈 내부에 주입된 Prototype 빈은 매번 새로 생성되는 것이 아니라 유지된다. 우리가 Prototype을 사용한 이유는 매번 새로운 인스턴스를 가지고 싶어서일 것이다.
But, 다른 싱글톤 클래스를 호출할 때, 내부에 같은 Prototype이 있어도 이때는 두 Prototype이 새로 생성되긴 한다.(우리 의도는 아닐듯, Prototype은 주로 무조건 쌔거만 쓰고싶어서 사용한다.)


### 그렇다면, Singleton 빈 내부에 PrototypeBean을 사용하는 방법은?
-> `Provider`를 사용하면 된다.

Dipendency Injection(DI) <-> Dipendency Lookup(DL) 이다. 우리는 Singleton 내부의 Prototype을 DL하여 생성하면 된다.


```java

@Scope("singleton")
static class ClientBean{//Singleton 하위에 Prototype 빈
******************************************
    @Autowired
private ObjectProvider<PrototypeBean> prototypeBeanProvider;
******************************************
    
    public int logic() {
    ******************************************
        PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
    ******************************************
    }
}
```

* `private ObjectProvider<PrototypeBean> prototypeBeanProvider` 으로 속성 맞춰 Prototype으로 Provider 생성한다.
* 그것으로 새로 Prototype을 필요할 때마다 생성할 위치에 getObject() 사용한다.
* Provider는 getObject() 사용할 때, 그 때 마다 PrototypeBean을 찾아 제공해준다.

-> 결론, ObjectProvider는 매번 DI 컨테이너에게 새로 요청해주는 기능을 사용하므로, Prototype이 매번 새로 생성된다.
기존 새로 Singleton에서 가져다 쓰면 생성을 하지 않는 문제를 해결할 수 있다.(최초 1회 생성하여 멤버변수에 저장해서 Prototype이 재생성되지 않는 문제 해결)




# 웹 스코프

Prototype과 다르게 종료시점까지 관리해서 종료 메서드가 사용가능하다.

### 종류
* request : HTTP Request 하나마다 별도의 인스턴스가 생성, 관리 (유저 A, 유저 B가 각각 다른 Bean 생성, 사용)
* session : HTTP Session과 동일한 생명주기를 가진 스코프
* application : 서블릿 컨텍스트와 동일한 생명주기
* websocket : 웹 소켓과 동일한 생명주기를 가지는 스코프

request를 대표로 설명
-> User A, User B로 나눴을 때, 하위 모든 bean들도 각각 제작되어 사용된다. 
-> 요청 끝나면 함께 파기

---
### 활용 

> 동시에 여러 HTTP 요청이 올 때, 어떤 요청이 남긴 로근지 확인하는데 사용

로그 형식 : [UUID][requestURL]{message}

로그 객체를 @PostConstruct에서 UUID를 세팅해주고 requestURL을 Set 해준다.
이 객체는 모든 Client가 독립적으로 인스턴스를 가지고 사용, 소멸해야 하기 때문에 싱글톤이 아니다.

Http 요청의 생성과 연결이 끝나면 종료하기 때문에 @Scope(value="request")로 사용한다.
한번 요청이 들어와서 한 Contoller -> Service 동작에서 한번 생성된 애들은 동일한 빈이 유지가 된다.
-> (한 요청에 무슨 동작 했는지 구분이 다 된다는 뜻, prototype과 별개인 기능이다.)



> 로그 찍어주는 객체를 `@Scope("request") 범위로 선언한 모습`
> MyLogger 생성시 init으로 UUID 자동 할당, 이후 URL은 수동 할당, 파괴될 때도 죽는다고 로그 찍고 죽는다. (빈 라이프사이클에 의해)

```java

@Component
@Scope(value="request")
public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "]" + message);
    }

    @PostConstruct
    public void init() {
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create:" +  this);
    }

    @PreDestroy
    public void close() {
        System.out.println("[" + uuid + "] request scope bean close:" +  this);
    }
    
}
```

> Controller에 사용자 request가 들어오면 해당 URL찍고, 동일 UUID로 request가 소멸될 때 까지 같은 MyLogger 객체가 유지되어 맘껏 사용하다가 소멸한다. 

* ObjectProvider는 말그대로 MyLogger 빈 컨테이너에 접근해 달라는 요청인데 MyLogger 스코프가 request기 때문에
* 매번 request스코프의 MyLogger를 생성해 준다. (Singleton이면 동일한놈 계속 줌)

```java

@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    //ObjectProvider는 말그대로 MyLogger 빈 컨테이너에 접근해 달라는 요청인데 MyLogger 스코프가 request기 때문에
    //매번 request스코프의 MyLogger를 생성해 준다. (Singleton이면 동일한놈 계속 줌)
    ******************************************************************
    //1.ObjectProvider 선언 (MyLogger를 컨테이너에 요청해주는 제공자)
    private final ObjectProvider<MyLogger> myLoggerProvider;
    ******************************************************************
    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        ******************************************************************
        //2. 컨테이너에 요청한 모습. 스코프에 맞춰서 규칙에 맞게 반환해준다.(해당 Mylogger는 `@Scope("request")이므로, 매번 새 인스턴스 생성, 한 request단위까지는 동일하게 유지해줌
        MyLogger mylogger = myLoggerProvider.getObject();
        ******************************************************************
        myLogger.setRequestURL(requestURL);

        //이후 Service, Controller, Repository 아무대서나 휘뚜루 마뚜루 맘대로 로그 찍기
        mylogger.log("controller이다~");
        logDemoService.log("서비스 로그~");
        //한 request에서는 동일한 mylogger 빈이 유지가 된다는 사실!
        //따라서 클라이언트별로 UUID로 구분이 가능하다.
    }
}


@Service
@RequiredArgsConstructor
public class LogDemoService{
    
    private final ObjectProvider<MyLogger> myLoggerProvider;
    
    
    public void logic(String id){
        ******************************************************************
        //3. 컨트롤러에서 서비스 로직을 호출하고, 서비스에서 Provider로 또 MyLogger 호출.
        //But, 같은 request이므로 같은 MyLogger 객체가 반환된다.(UUID가 동일하게 유지된다는 뜻, 사용자 구별 가능하다는 뜻)
        MyLogger mylogger = myLoggerProvider.getObject();
        ******************************************************************
        mylogger.log("service 블라블라~~");
    }
    
}

```


#### Provider에 의존하지 말고 프록시 선언하여 다형성까지 잡아보자

ObjectProvider에 의존하지 않고, 가상의 Proxy객체를 넣어 클라이언트 코드에서 그냥 MyLogger만 호출하여 사용해보자.(모든 AOP의 근간이 되는 기술)

* 기존 `requestScope`를 달아놓은 객체에 추가로 `proxyMode`를 단다.

```java

```java

@Component

****************************************************************
//Scope에 프록시Mode 선언
@Scope(value="request", proxyMode = ScopedProxyMode.TARGET_CLASS)//Interface면 TARGET_INTERFACE 선택
****************************************************************

public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "]" + message);
    }

    @PostConstruct
    public void init() {
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create:" +  this);
    }

    @PreDestroy
    public void close() {
        System.out.println("[" + uuid + "] request scope bean close:" +  this);
    }
    
}
```


```java

@Controller
@RequiredArgsConstructor
public class LogDemoController {
    
    ******************************************************************
    //private final ObjectProvider<MyLogger> myLoggerProvider;
    변경
    private final MyLogger mylogger;
    ******************************************************************
    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        ******************************************************************
        //2. 컨테이너에 요청한 모습. 스코프에 맞춰서 규칙에 맞게 반환해준다.(해당 Mylogger는 `@Scope("request")이므로, 매번 새 인스턴스 생성, 한 request단위까지는 동일하게 유지해줌
        MyLogger mylogger = myLoggerProvider.getObject();
        ******************************************************************

    }
}


@Service
@RequiredArgsConstructor
public class LogDemoService{
    
    private final ObjectProvider<MyLogger> myLoggerProvider;
    
    
    public void logic(String id){
        ******************************************************************
        //MyLogger mylogger = myLoggerProvider.getObject();
        변경
        private final MyLogger mylogger;
        ******************************************************************
        mylogger.log("service 블라블라~~");
    }
    
}

```

### Proxy 객체 레이지로딩의 장점

* ProxyMode를 추가하여 Provider에게 주입받지 않고,
* Spring에서 `CGLIB` 바이트코드 조작으로 만든 가짜 MyLogger 프록시 객체를 Provider처럼 등록하게 스프링 컨테이너 빈에 적재한다.
* 이 프록시객체는 싱글톤이고, 레이지 로딩으로 실 동작을 수행할 때, Proxy객체가 해당 객체를 찾아 기능을 작동시켜준다. (스코프에 맞춰서)

myLogger.logic() 호출 -> Spring CGLIB Proxy객체 MyLogger 호출 -> Proxy객체가 원본 클래스의 logic() 호출해준다.

-> 💥 진짜 객체 조회를 기다렸다가 실 동작시에 가져와 작동하는 LAZYLOADING하여 처리하기 때문에, 가능한 일이다.
어차피 가짜 객체를 등록하고 request단위로 요청시에 호출해주기 때문에, 모든 코드에 그냥 선언하여 주입하듯이 요청하면 된다.  (다형성 사용)
이 프록시 객체는 원본 객체를 상속하고 있기 때문. (Interface, 역할 기능을 해준다고 생각하면 됨)
결론적으로 클라이언트 코드를 고치지 않아도 된다.
