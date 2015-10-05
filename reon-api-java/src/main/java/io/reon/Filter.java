package io.reon;

import io.reon.http.Request;
import io.reon.http.Response;

public class Filter {

	public boolean matchBefore(String uri) {
		return false;
	}

	public boolean matchAfter(String uri) {
		return false;
	}

	public Request before(Request request) {
		return request;
	}

	public Response after(Response response) {
		return response;
	}
}
