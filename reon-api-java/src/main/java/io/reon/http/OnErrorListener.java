package io.reon.http;

public interface OnErrorListener {
	void onError(Response response, Throwable cause);
}
