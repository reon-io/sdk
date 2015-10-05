package io.reon.test;

import io.reon.api.GET;
import io.reon.http.Cookie;
import io.reon.http.Cookies;
import io.reon.http.Response;

public class TestCookie {

	@GET("/setCookie?:name=name&:value=value")
	public Response setCookie(String name, String value) {
		return Response.ok().withCookie(new Cookie(name, value + value));
	}

	@GET("/acceptCookie?:cookieName=name")
	public String acceptCookie(String cookieName, Cookie COOKIE_NAME, Cookies cookies) {
		return cookies.getCookie(cookieName).getValue() + COOKIE_NAME.getValue();
	}
}
