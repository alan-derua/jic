public class AnonymousGreeter {

    // Интерфейс для демонстрации
    public interface Greeting {
        void sayHello();
        default void sayGoodbye() {
            System.out.println("Goodbye!");
        }
    }

    public void greet(Greeting greeting) {
        greeting.sayHello();
    }

    public void greet(String msg, String name) {
        greet(msg + " " + name);
    }

    public void greet(String msg) {
        Greeting greeting = new Greeting() {
            @Override
            public void sayHello() {
                System.out.println(msg);
            }
        };

        greet(greeting);
    }
}