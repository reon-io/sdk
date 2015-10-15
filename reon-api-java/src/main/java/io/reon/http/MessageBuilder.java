package io.reon.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MessageBuilder<T extends Message> {
	protected T that;

	public T build() {
		return that;
	}

	public MessageBuilder<T> withHeader(String name, String value) {
		that.getHeaders().add(name, value);
		return this;
	}

	public MessageBuilder<T> withHeaders(String headers) {
		that.getHeaders().add(Headers.parse(headers));
		return this;
	}

	public MessageBuilder<T> withUpdatedHeader(String name, String value) {
		that.getHeaders().update(name, value);
		return this;
	}

	public MessageBuilder<T> withId(String id) {
		if (id != null) withUpdatedHeader(Headers.X.REON_ID, id);
		return this;
	}

	public MessageBuilder<T> withKeepAlive() {
		return withUpdatedHeader(Headers.RESPONSE.CONNECTION, "keep-alive");
	}

	public MessageBuilder<T> withContentType(String contentType) {
		return withUpdatedHeader(Headers.RESPONSE.CONTENT_TYPE, contentType);
	}

	public MessageBuilder<T> withContentTypeFrom(String filename) {
		return withContentType(MimeTypes.getMimeType(filename));
	}

	public MessageBuilder<T> withLength(long length) {
		that.setContentLength(length);
		return this;
	}

	public MessageBuilder<T> withBody(File file) throws FileNotFoundException {
		return withBody(new FileInputStream(file)).withLength(file.length()).withContentTypeFrom(file.getName());
	}

	public MessageBuilder<T> withBody(String s) {
		that.setBody(s);
		return this;
	}

	public MessageBuilder<T> withBody(InputStream is) {
		that.setBody(is);
		return this;
	}

	public MessageBuilder<T> withBody(byte[] data) {
		that.setBody(data);
		return this;
	}

	public MessageBuilder<T> withBody(JSONObject jsonObject) {
		that.setBody(jsonObject);
		return this;
	}

	public MessageBuilder<T> withBody(JSONArray jsonObject) {
		that.setBody(jsonObject);
		return this;
	}

	public MessageBuilder<T> withChunks() {
		return withTransferEncoding(Message.CHUNKED);
	}

	public MessageBuilder<T> withIdentity() {
		return withTransferEncoding(Message.IDENTITY);
	}

	public MessageBuilder<T> withTransferEncoding(String value) {
		return withUpdatedHeader(Headers.RESPONSE.TRANSFER_ENC, value);
	}

}
