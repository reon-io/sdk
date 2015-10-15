package io.reon.http;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cookies {

	private Map<String, Cookie> cookies = new HashMap<String, Cookie>();

	public Cookies(Map<String, Cookie> cookies) {
		this.cookies = cookies;
	}

	public Cookie getCookie(String name) {
		return cookies.get(name);
	}

    public Collection<Cookie> all() {
        return cookies.values();
    }

    public void setCookie(Cookie cookie) {
        cookies.put(cookie.getName(), cookie);
    }

    public static Cookies parse(Headers headers) {
        return parse(headers, Headers.REQUEST.COOKIE);
    }

    public static Cookies parseServer(Headers headers) {
        return parse(headers, Headers.RESPONSE.SET_COOKIE);
    }

    private static Cookies parse(Headers headers, String cookieTag) {
        List<Headers.Header> cookieHeaders = headers.findAll(cookieTag);
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        for (Headers.Header cookieHeader : cookieHeaders) {
            for(String cookieStr: cookieHeader.getValue().split(";")) {
                Cookie cookie = Cookie.parse(cookieStr.trim());
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return new Cookies(cookieMap);
    }

}
