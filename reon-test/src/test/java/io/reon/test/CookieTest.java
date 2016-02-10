package io.reon.test;

import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import org.junit.Test;

import java.io.IOException;

import io.reon.LocalSocketConnection;
import io.reon.http.HttpClient;
import io.reon.http.Request;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;
import io.reon.test.support.ReonTestCase;
import io.reon.Service;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class CookieTest extends ReonTestCase {

	public static final Request GET_1 = RequestBuilder
			.get("/setCookie?name=COOKIE_NAME&value=cookieValue")
			.withHost("localhost")
			.build();

	public static final Request GET_2 = RequestBuilder
			.get("/acceptCookie?cookieName=COOKIE_NAME")
			.withHost("localhost")
			.withHeader("Cookie", "COOKIE_NAME=cookieValue")
			.build();

	@Test(timeout = 10000)
	public void cookieShouldBeReturned() throws IOException, InterruptedException {
		// given
		Service service = new Service();
		service.onCreate();
		service.onStartCommand(new Intent(), 0, 0);

		// when
		LocalSocket clientSocket = new LocalSocket();
		clientSocket.connect(new LocalSocketAddress("test"));
		HttpClient client = new HttpClient(new LocalSocketConnection(clientSocket));
		Response response = client.send(GET_1);

		// then
		System.out.println("response: " + response);
		String expectedBody = "Set-Cookie: COOKIE_NAME=cookieValuecookieValue";
		assertThat(response.getHeaders().toString(), containsString(expectedBody));

		client.close();
		service.onDestroy();
	}

	@Test(timeout = 10000)
	public void cookieShouldBeAccepted() throws IOException, InterruptedException {
		// given
		Service service = new Service();
		service.onCreate();
		service.onStartCommand(new Intent(), 0, 0);

		// when
		LocalSocket clientSocket = new LocalSocket();
		clientSocket.connect(new LocalSocketAddress("test"));
		HttpClient client = new HttpClient(new LocalSocketConnection(clientSocket));
		Response response = client.send(GET_2);

		// then
		System.out.println("response: " + response);
		String expectedBody = "cookieValuecookieValue";
		assertThat(response.getBodyAsString(), containsString(expectedBody));

		client.close();
		service.onDestroy();
	}
}
