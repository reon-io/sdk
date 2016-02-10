package io.reon.test;

import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import org.junit.Test;

import java.io.IOException;

import io.reon.LocalSocketConnection;
import io.reon.Service;
import io.reon.http.HttpClient;
import io.reon.http.MimeTypes;
import io.reon.http.RequestBuilder;
import io.reon.http.Response;
import io.reon.test.support.ReonTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProducesTest extends ReonTestCase {

	private Service startService() {
		Service service = new Service();
		service.onCreate();
		service.onStartCommand(new Intent(), 0, 0);
		return service;
	}

	private void stopService(Service service) {
		service.onDestroy();
	}

	@Test(timeout = 10000)
	public void shouldProduceProperContentType() throws IOException {
		// given
		Service service = startService();

		// when
		LocalSocket clientSocket = new LocalSocket();
		clientSocket.connect(new LocalSocketAddress("test"));
		HttpClient client = new HttpClient(new LocalSocketConnection(clientSocket));
		Response response = client.send(RequestBuilder.get("/text/test").build());


		// then
		assertTrue(response.isOK());
		assertEquals(MimeTypes.MIME_TEXT_HTML, response.getContentType().substring(0,MimeTypes.MIME_TEXT_HTML.length()));

		// when
		response = client.send(RequestBuilder.get("/pdf/test").build());

		// then
		assertTrue(response.isOK());
		assertEquals(MimeTypes.MIME_APPLICATION_PDF, response.getContentType());

		// when
		response = client.send(RequestBuilder.get("/custom/test").build());

		// then
		assertTrue(response.isOK());
		assertTrue(response.getContentType().contains("unusual"));

		client.close();
		stopService(service);
	}
}
