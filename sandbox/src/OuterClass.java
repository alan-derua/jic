import utils.Utils;

public class OuterClass {

    private String message = "Hello from OuterClass!";

    public class InnerClass {
        public void displayMessage() {
            System.out.println("InnerClass says: " + message);
            String res = Utils.add(1, 2);
            System.out.println("1 + 2 = " + res);
        }
    }
}