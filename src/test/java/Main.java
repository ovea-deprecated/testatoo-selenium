import org.testatoo.selenium.server.SeleniumServerFactory;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class Main {
    public static void main(String... args) throws Exception {
        SeleniumServerFactory.configure()
            .setDontTouchLogging(true)
            .setSingleWindow(true)
            .setPort(4444)
            .create()
            .start();
    }
}
