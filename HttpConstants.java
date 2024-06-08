package dc.squareup;

public class HttpConstants {
    private static String defaultUAConstants = "";

    public static String getDefaultUA() {
        return defaultUAConstants;
    }

    public static void setUA(String str) {
        defaultUAConstants = str;
    }
}
