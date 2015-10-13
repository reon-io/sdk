package io.reon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reon.http.Method;
import io.reon.http.Request;

public class Service extends LocalService<WebAppContext> implements WebAppContext {
	static final String ASSETS_CLASS_NAME = "io.reon.MyAssetInfo";
	static final String SERVICES_CLASS_NAME = "io.reon.MyServices";
	static final String SOCKET_ADDR = "/tmp/io.reon.server";

	public static final String TAG = Service.class.getName();

	private static List<Endpoint.Info> endpointList = new ArrayList<Endpoint.Info>();
	private static List<Filter> filterList = new ArrayList<Filter>();

	static {
		addEndpointInfo(new Endpoint.Info(Method.GET, AppServicesEndpoint.SERVICES_JSON, AppServicesEndpoint.class));
		addEndpointInfo(new Endpoint.Info(Method.GET, "/", AssetEndpoint.class));
		try {
			// run static initializers for generated classes
			Class.forName(SERVICES_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		filterList = Collections.unmodifiableList(filterList);
		endpointList = Collections.unmodifiableList(endpointList);
	}

	// package access
	static void addEndpointInfo(Endpoint.Info info) {
		endpointList.add(info);
	}

	static void addFilter(Filter filter) {
		filterList.add(filter);
	}

	private AssetLengthInfo assetLengthInfo;

	private ExecutorService executorService;

	private RequestProcessor requestProcessor;

	private LocalServer localServer;

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Received intent from server");
		if (executorService == null) {
			executorService = new ThreadPoolExecutor(2, 16, 60, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), ThreadFactories.newWorkerThreadFactory());
		}
//		if(intent.getAction().equals(Intent.ACTION_CALL)) {
//			// We have to connect to a socket to pull request from
//			long requestId = intent.getLongExtra(Intent.EXTRA_UID, -1);
//			requestLocal(requestId);
//		}
		if (localServer == null || localServer.isClosed()) {
			localServer = new LocalServer(this, executorService);
			executorService.submit(localServer);
		}
		return android.app.Service.START_NOT_STICKY;
	}

	protected void requestLocal(long id) {
		try {
			LocalSocket ls = new LocalSocket();
			ls.connect(new LocalSocketAddress(SOCKET_ADDR));
			LocalSocketConnection lsc = new LocalSocketConnection(ls);
			OutputStream os = lsc.getOutputStream();
			os.write(new Request(Method.TRACE, "#"+Long.toHexString(id)).toString().getBytes());
			os.flush();
			RequestTask worker = new RequestTask(lsc, getRequestProcessor());
			executorService.execute(worker);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating REON service");
		if (requestProcessor == null) {
			requestProcessor = new RequestProcessor(createEndpoints(), getFilters());
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying REON service");
		TempServiceConnection.terminateAll();
		if (localServer != null) {
			localServer.shutdown();
			localServer = null;
		}
		if (executorService != null) {
			executorService.shutdown();
			executorService = null;
		}
	}

	public Object bindService(ComponentName name) {
		TempServiceConnection connection = TempServiceConnection.get(name.getClassName());
		if (connection == null) connection = new TempServiceConnection(this, name, executorService);
		return connection.getServiceObject(); // awaits connection
	}

	public List<Endpoint.Info> getEndpointInfos() {
		return endpointList;
	}

	public List<Filter> getFilters() {
		return filterList;
	}

	public AssetLengthInfo getAssetInfo() {
		if (assetLengthInfo == null) {
			try {
				assetLengthInfo = (AssetLengthInfo) Class.forName(ASSETS_CLASS_NAME).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return assetLengthInfo;
	}

	private List<? extends Endpoint> createEndpoints() {
		List<Endpoint> list = new LinkedList<Endpoint>();
		for (Endpoint.Info info: endpointList) {
			try {
				Endpoint ep = (Endpoint) info.getImplementingClass().getConstructor(WebAppContext.class).newInstance(this);
				System.out.println(ep.httpMethod().toString()+" "+ep.originalPath()+" instantiated!");
				list.add(ep);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public RequestProcessor getRequestProcessor() {
		return requestProcessor;
	}
}

