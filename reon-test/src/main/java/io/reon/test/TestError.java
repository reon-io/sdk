package io.reon.test;

import io.reon.api.GET;

public class TestError {

	@GET("/path/:id/:name")
	public String error(String id) {
		return "";
	}
}
