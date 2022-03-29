# core

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






