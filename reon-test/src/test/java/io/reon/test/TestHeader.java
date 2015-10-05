package io.reon.test;

import com.google.common.base.Joiner;

import io.reon.api.GET;
import io.reon.http.Cookies;
import io.reon.http.Headers;

public class TestHeader {

	@GET("/headertest")
	public String header(Headers headers, Cookies cookies) {
		String host = headers.get(Headers.REQUEST.HOST);
		return host+" "+Joiner.on(" ").join(cookies.all());
	}

}
