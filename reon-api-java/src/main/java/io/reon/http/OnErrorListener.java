package io.reon.http;

public interface OnErrorListener {
	void onError(Message response, Throwable cause);
}
