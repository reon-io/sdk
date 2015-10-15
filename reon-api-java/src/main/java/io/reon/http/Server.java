package io.reon.http;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Server {
	private final ExecutorService exec;
	private ConcurrentHashMap<String, HttpService> services = new ConcurrentHashMap<String, HttpService>();

	public Server(ExecutorService exec) {
		this.exec = exec;
	}

	public void accept(Connection conn) throws IOException {
		exec.submit(new ServerTask(conn));
	}

	public void addHttpService(String context, final HttpService service) {
		if(context.startsWith("/")) {
			context = URI.create(context).normalize().getPath();
			services.put(context, service);
		}
	}

	public void removeHttpService(String context) {
		services.remove(context);
	}

	private class ServerTask extends AbstractServerTask {

		public ServerTask(Connection conn) {
			super(conn);
		}

		@Override
		protected HttpService matchServicePath(String path) {
			HttpService service = services.get(path);
			while(!path.isEmpty() && service == null) {
				path = path.substring(0,path.lastIndexOf('/'));
				service = services.get(path);
			}
			if(service==null) service = services.get("/");
			return service;
		}
	}

}
