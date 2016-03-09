package io.reon.test;


import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reon.LocalSocketConnection;
import io.reon.concurrent.ListenableFuture;
import io.reon.http.AsyncHttpClient;
import io.reon.http.Cookie;
import io.reon.http.HttpClient;
import io.reon.http.Request;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;
import io.reon.test.support.ReonTestCase;
import io.reon.Service;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class WowFirstWorkingTest extends ReonTestCase {

	public static final Request GET_1 = RequestBuilder
			.get("/test")
			.withHost("localhost")
			.withClose()
			.build();

	public static final String POST_BODY = "id=11&name=somename";
	public static final Request POST_1 = RequestBuilder
			.post("/testpost")
			.withHost("localhost")
			.withBody(POST_BODY)
			.withClose()
			.build();

	public static final Request GET_LONG_1 = RequestBuilder
			.get("/test")
			.withHost("localhost")
			.withCookie(new Cookie("cookieName", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
					+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
					+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
					+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
					+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
					+ "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
			))
			.withKeepAlive()
			.build();

	@Test(timeout = 1000)
	public void simplestGet() throws Exception {
		// given
		Service service = startService();

		// when
		LocalSocket clientSocket = new LocalSocket();
		clientSocket.connect(new LocalSocketAddress("test"));
		HttpClient client = new HttpClient(new LocalSocketConnection(clientSocket));
		Response response = client.send(GET_1);

		// then
		System.out.println("response: " + response);
		String expectedBody = new Test1().test();
		assertThat(response.getBodyAsString(), containsString(expectedBody));

		// clean up
		client.close();
		service.onDestroy();
	}

	@Test(timeout = 5000)
	public void simplestPost() throws Exception {
		// given
		Service service = startService();

		// when
		LocalSocket clientSocket = new LocalSocket();
		clientSocket.connect(new LocalSocketAddress("test"));
		HttpClient client = new HttpClient(new LocalSocketConnection(clientSocket));
		Response response = client.send(POST_1);

		// then
		System.out.println("response: " + response);
		String expectedBody = new TestPost().post(11, "somename");
		assertThat(response.getBodyAsString(), containsString(expectedBody));

		// clean up
		client.close();
		service.onDestroy();
	}

	private Service startService() {
		Service service = new Service();
		service.onCreate();
		service.onStartCommand(new Intent(), 0, 0);
		return service;
	}

	@Test(timeout = 5000)
	public void shouldParseLongGetRequests() throws Exception {
		// given
		Service service = startService();
		ExecutorService executor = Executors.newSingleThreadExecutor();

		// when
		LocalSocket clientSocket = new LocalSocket();
		clientSocket.connect(new LocalSocketAddress("test"));
		AsyncHttpClient client = new AsyncHttpClient(new LocalSocketConnection(clientSocket), executor);
		ListenableFuture<Response> responseFuture1 = client.send(GET_LONG_1);
		ListenableFuture<Response> responseFuture2 = client.send(GET_LONG_1);

		// then
		Response response = responseFuture1.get();
		System.out.println("response: " + response);
		assertTrue(response.isOK());
		String expectedBody = new Test1().test();
		assertThat(response.getBodyAsString(), containsString(expectedBody));

		response = responseFuture2.get();
		System.out.println("response: " + response);
		assertTrue(response.isOK());
		assertThat(response.getBodyAsString(), containsString(expectedBody));

		// clean up
		client.close();
		executor.shutdown();
		service.onDestroy();
	}
}
