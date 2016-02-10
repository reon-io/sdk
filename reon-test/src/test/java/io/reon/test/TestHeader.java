package io.reon.test;

import io.reon.api.GET;
import io.reon.http.Cookie;
import io.reon.http.Cookies;
import io.reon.http.Headers;

public class TestHeader {

	@GET("/headertest")
	public String header(Headers headers, Cookies cookies) {
		String host = headers.get(Headers.REQUEST.HOST);
		StringBuilder sb = new StringBuilder();
		sb.append(host);
		for (Cookie c: cookies.all()) {
			sb.append(' ');
			sb.append(c.toString());
		}
		return sb.toString();
	}

}
