package io.reon.auth;

public interface HttpAuth {
	String getAuthName();
	String challenge();
	String response(String auth);
	boolean verify(String auth);
}
