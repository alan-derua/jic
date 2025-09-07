import logging.MyLoggingEvent;

class Xyz {
    void processEvent(MyLoggingEvent event) {
        bar();
        Foo foo = new Foo();
        foo.foo();
    }

    private void bar() {
        Bar.bar();
    }

    private void greet() {
        new AnonymousGreeter().greet("Hello", "Max");
    }

    private void greet2() {
        new AnonymousGreeter().greet(new AnonymousGreeter.Greeting() {
            @Override
            public void sayHello() {
                System.out.println("Hello from Xyz");
            }
        });
    }

    private void display() {
        OuterClass outer = new OuterClass();
        OuterClass.InnerClass inner = outer.new InnerClass();
        inner.displayMessage();
    }
}