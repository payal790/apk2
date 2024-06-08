package dc.squareup.cookie;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class CookieCenter {
    static List<ICookieProvider> providers = new ArrayList();

    public interface ICookieProvider {
        boolean addCookie(String str, String str2);

        String getCookieByUrl(String str);

        boolean removeAllCookie();

        boolean removeSessionCookie();
    }

    public static String getCookies(String str) {
        for (ICookieProvider next : providers) {
            if (next != null) {
                String cookieByUrl = next.getCookieByUrl(str);
                if (!TextUtils.isEmpty(cookieByUrl)) {
                    return cookieByUrl;
                }
            }
        }
        return null;
    }

    public static void putCookies(String str, String str2) {
        for (ICookieProvider next : providers) {
            if (next != null) {
                next.addCookie(str, str2);
            }
        }
    }

    public static synchronized void registerProvider(ICookieProvider iCookieProvider) {
        synchronized (CookieCenter.class) {
            providers.add(iCookieProvider);
        }
    }

    public static String removeAllCookie() {
        for (ICookieProvider next : providers) {
            if (next != null) {
                next.removeAllCookie();
            }
        }
        return null;
    }

    public static String removeSessionCookie() {
        for (ICookieProvider next : providers) {
            if (next != null) {
                next.removeSessionCookie();
            }
        }
        return null;
    }

    public static synchronized void unRegisterProvider(ICookieProvider iCookieProvider) {
        synchronized (CookieCenter.class) {
            providers.remove(iCookieProvider);
        }
    }
}
