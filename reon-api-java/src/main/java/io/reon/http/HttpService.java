package io.reon.http;

public interface HttpService {
	Response service(Request request) throws HttpException;
}
