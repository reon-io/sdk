package io.reon.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResponseBuilder extends MessageBuilder<Response> {

	private ResponseBuilder(Response response) {
		that = response;
	}

	public static ResponseBuilder startWith(StatusCode statusCode) {
		return new ResponseBuilder(new Response(statusCode));
	}

	public static ResponseBuilder with(Response response) {
		return new ResponseBuilder(response);
	}

	public static ResponseBuilder ok() {
		return startWith(StatusCode.OK);
	}

	public static ResponseBuilder notFound() {
		return startWith(StatusCode.NOT_FOUND);
	}

	public static ResponseBuilder error(HttpException ex) {
		return startWith(ex.getStatusCode()).withClose();
	}

	public static ResponseBuilder methodNotAllowed() {
		return startWith(StatusCode.METHOD_NOT_ALLOWED);
	}

	public static ResponseBuilder unauthorized() {
		return startWith(StatusCode.UNAUTHORIZED);
	}

	public static ResponseBuilder serviceUnavailable() {
		return startWith(StatusCode.SERVICE_UNAVAILABLE).withClose();
	}

	public static ResponseBuilder internalError(Exception e) {
		return startWith(StatusCode.INTERNAL_ERROR).withClose();
	}

	public static ResponseBuilder forbidden() {
		return startWith(StatusCode.FORBIDDEN);
	}

	public static ResponseBuilder found(String location) {
		return startWith(StatusCode.FOUND).withLocation(location);
	}

	private ResponseBuilder withLocation(String location) {
		return (ResponseBuilder) withUpdatedHeader(Headers.RESPONSE.LOCATION, location);
	}

	@Override
	public ResponseBuilder withId(String id) {
		return (ResponseBuilder) super.withId(id);
	}

	public ResponseBuilder withKeepAlive() {
		return (ResponseBuilder) super.withKeepAlive();
	}

	public ResponseBuilder withClose() {
		return (ResponseBuilder) withUpdatedHeader(Headers.RESPONSE.CONNECTION, "close");
	}

	public ResponseBuilder withCookie(Cookie cookie) {
		return (ResponseBuilder) withHeader(Headers.RESPONSE.SET_COOKIE, cookie.toString());
	}

	public ResponseBuilder withCookies(Cookies cookies) {
		for (Cookie cookie : cookies.all()) withCookie(cookie);
		return this;
	}

	public ResponseBuilder withReason(String reason) {
		that.setReason(reason);
		return this;
	}

	public ResponseBuilder withContentType(String contentType) {
		return (ResponseBuilder) super.withContentType(contentType);
	}

	public ResponseBuilder withContentTypeFrom(String filename) {
		return (ResponseBuilder) super.withContentTypeFrom(filename);
	}

	public ResponseBuilder withBody(File file) throws FileNotFoundException {
		return (ResponseBuilder) super.withBody(file);
	}

	public ResponseBuilder withBody(String s) {
		return (ResponseBuilder) super.withBody(s);
	}

	public ResponseBuilder withBody(InputStream is) {
		return (ResponseBuilder) super.withBody(is);
	}

	public ResponseBuilder withBody(byte[] data) {
		return (ResponseBuilder) super.withBody(data);
	}

	@Override
	public ResponseBuilder withBody(JSONObject jsonObject) {
		return (ResponseBuilder) super.withBody(jsonObject);
	}

	@Override
	public ResponseBuilder withBody(JSONArray jsonObject) {
		return (ResponseBuilder) super.withBody(jsonObject);
	}

	public ResponseBuilder withBody(Response response) {
		return with(response);
	}

	public ResponseBuilder withLength(long length) {
		return (ResponseBuilder) withUpdatedHeader(Headers.RESPONSE.CONTENT_LEN, Long.toString(length));
	}

	public ResponseBuilder withChunks() {
		return (ResponseBuilder) super.withChunks();
	}

	public ResponseBuilder withIdentity() {
		return (ResponseBuilder) super.withIdentity();
	}

	public ResponseBuilder withTransferEncoding(String value) {
		return (ResponseBuilder) super.withTransferEncoding(value);
	}

	public ResponseBuilder withCharset(String charset) {
		that.charset = charset;
		return this;
	}
}
