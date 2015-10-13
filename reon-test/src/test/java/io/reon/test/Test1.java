package io.reon.test;

import io.reon.api.After;
import io.reon.api.Before;
import io.reon.api.GET;
import io.reon.http.Request;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;
import io.reon.http.ResponseBuilder;

public class Test1 {

	@GET("/test")
	public String test() {
		return "test 1 result";
	}

	@Before("/test")
	public Request precondition(Request r) {
		return RequestBuilder.req(r).withId("Aqq").build();
	}

	@After("/test")
	public Response postcondition(Response r) {
		return ResponseBuilder.resp(r).withKeepAlive().build();
	}

}
