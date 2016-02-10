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

public class HeaderTest extends ReonTestCase {

	public static final Request GET_1 = RequestBuilder
			.get("/headertest")
			.withHost("localhost")
			.withHeader("Cookie", "a=1; b=2")
			.build();

	@Test(timeout = 10000)
	public void headersShouldBeDecoded() throws IOException {
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
		String headers = response.getBodyAsString();
		assertThat(headers, containsString("localhost"));
		assertThat(headers, containsString("b=2"));
		assertThat(headers, containsString("a=1"));
	}
}
