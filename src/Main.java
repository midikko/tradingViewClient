import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        new Client().start();
    }
}
