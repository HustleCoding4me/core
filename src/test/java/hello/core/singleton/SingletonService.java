package hello.core.singleton;

public class SingletonService {

    private static final SingletonService instance = new SingletonService();

    public static SingletonService getInstace() {
        return instance;
    }

    // 외부에서 SingletonService new 를 막기 위해 private 생성자를 선언했다.
    private SingletonService() {
    }

    //static 영역에 최초 1회에 생성하여 instance에 참조를 넣어놓는다.
    //그래서 SingletonService를 사용할 때, getInstance()를 사용하여 불러서 사용하면 된다.
}
