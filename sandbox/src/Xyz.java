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
}