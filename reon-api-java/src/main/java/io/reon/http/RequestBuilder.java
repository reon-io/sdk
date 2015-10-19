package io.reon.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class RequestBuilder extends MessageBuilder<Request> {

	private RequestBuilder(Request request) {
		that = request;
	}

	public static RequestBuilder with(Request request) {
		return new RequestBuilder(request);
	}

	public static RequestBuilder startWith(Method method, String uri) {
		return new RequestBuilder(new Request(method, uri)).withHost("localhost");
	}

	public static RequestBuilder get(String uri) {
		return startWith(Method.GET, uri);
	}

	public static RequestBuilder put(String uri) {
		return startWith(Method.PUT, uri);
	}

	public static RequestBuilder post(String uri) {
		return startWith(Method.POST, uri);
	}

	public static RequestBuilder delete(String uri) {
		return startWith(Method.DELETE, uri);
	}

	public static RequestBuilder head(String uri) {
		return startWith(Method.HEAD, uri);
	}

	public static RequestBuilder trace(String uri) {
		return startWith(Method.TRACE, uri);
	}

	public static RequestBuilder connect(String uri) {
		return startWith(Method.CONNECT, uri);
	}

	public static RequestBuilder options(String uri) {
		return startWith(Method.OPTIONS, uri);
	}


	public RequestBuilder withId(String id) {
		return (RequestBuilder) super.withId(id);
	}

	public RequestBuilder withKeepAlive() {
		return (RequestBuilder) super.withKeepAlive();
	}

	public RequestBuilder withUserAgent(String agent) {
		return (RequestBuilder) withUpdatedHeader(Headers.REQUEST.USER_AGENT, agent);
	}

	public RequestBuilder withHost(String host) {
		return (RequestBuilder) withUpdatedHeader(Headers.REQUEST.HOST, host);
	}

	public RequestBuilder withFrom(String from) {
		return (RequestBuilder) withUpdatedHeader(Headers.REQUEST.FROM, from);
	}

	public RequestBuilder withAuth(String auth) {
		return (RequestBuilder) withUpdatedHeader(Headers.REQUEST.AUTH, auth);
	}

	public RequestBuilder withChunks() {
		return (RequestBuilder) super.withChunks();
	}

	public RequestBuilder withReferer(String referer) {
		return (RequestBuilder) withUpdatedHeader(Headers.REQUEST.REFERER, referer);
	}

	public RequestBuilder withContentType(String contentType) {
		return (RequestBuilder) super.withContentType(contentType);
	}

	public RequestBuilder withLength(long length) {
		return (RequestBuilder) withLength(length);
	}

	public RequestBuilder withCookie(Cookie cookie) {
		return (RequestBuilder) withHeader(Headers.REQUEST.COOKIE, cookie.toString());
	}

	public RequestBuilder withCookies(Cookies cookies) {
		for (Cookie cookie : cookies.all()) withCookie(cookie);
		return this;
	}

	public RequestBuilder withBody(String s) {
		return (RequestBuilder) super.withBody(s);
	}

	public RequestBuilder withBody(byte[] b) {
		return (RequestBuilder) super.withBody(b);
	}

	public RequestBuilder withBody(JSONObject json) {
		return (RequestBuilder) super.withBody(json);
	}

	public RequestBuilder withBody(JSONArray json) {
		return (RequestBuilder) super.withBody(json);
	}

	public RequestBuilder withBody(File f) throws FileNotFoundException {
		return (RequestBuilder) super.withBody(f);
	}

	public RequestBuilder withRedirection(URI u) {
		if (u != null) {
			that.getHeaders().update(Headers.REQUEST.ORIGIN, that.getURI().toString());
			that.uri = u;
		}
		return this;
	}

	@Override
	public RequestBuilder withBody(InputStream is) {
		// always read body
		if (that.isChunked()) is = new ChunkedInputStream(is);
		that.body = is;
		long length = that.getContentLenght();
		try {
			if (length > 0 && length <= that.IN_MEMORY_LIMIT) {
				byte[] cache = new byte[(int) that.getContentLenght()];
				that.readBody(cache);
				that.body = cache;
			}
		} catch (IOException ex) {
			throw new HttpBadRequestException(ex.getMessage());
		}
		return this;
	}

}
