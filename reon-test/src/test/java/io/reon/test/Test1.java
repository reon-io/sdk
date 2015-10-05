package io.reon.test;

import io.reon.api.After;
import io.reon.api.Before;
import io.reon.api.GET;
import io.reon.http.Request;
import io.reon.http.Response;

public class Test1 {

	@GET("/test")
	public String test() {
		return "test 1 result";
	}

	@Before("/test")
	public Request precondition(Request r) {
		return r.withId("Aqq");
	}

	@After("/test")
	public Response postcondition(Response r) {
		return r.withKeepAlive();
	}

}
