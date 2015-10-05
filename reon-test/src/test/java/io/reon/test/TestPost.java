package io.reon.test;

import io.reon.api.POST;

public class TestPost {

	@POST("/testpost?:id=0&:name=noname")
	public String post(int id, String name) {
		return "post test id=" + id + " name=" + name;
	}
}
