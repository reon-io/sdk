package io.reon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reon.http.Method;

public class Service extends LocalService<WebAppContext> implements WebAppContext {
	static final String ASSETS_CLASS_NAME = "io.reon.MyAssetInfo";
	static final String SERVICES_CLASS_NAME = "io.reon.MyServices";

	public static final String TAG = Service.class.getName();
	public static final String SERVER_APP = "io.reon.server.app";
	public static final String REON_SERVICE = "ReonService";

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

	private volatile String authToken;

	@Override
	public Context getContext() {
		return this;
	}

	private void newAuthToken() {
		SecureRandom sr = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		sb.append(Long.toHexString(sr.nextLong()));
		sb.append(Long.toHexString(sr.nextLong()));
		authToken = sb.toString();
		Intent i = new Intent();
		i.setComponent(new ComponentName(SERVER_APP, REON_SERVICE));
		i.putExtra(WebAppContext.EXTRA_AUTH, authToken);
		i.putExtra(WebAppContext.EXTRA_APP, getPackage());
		startService(i);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Received intent from server");
		if (authToken == null) newAuthToken();
		if (executorService == null) {
			executorService = new ThreadPoolExecutor(1, 8, 30, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), ThreadFactories.newWorkerThreadFactory());
		}
		if (localServer == null || localServer.isClosed()) {
			localServer = new LocalServer(this, executorService);
			executorService.submit(localServer);
		}
		return android.app.Service.START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating APP service");
		if (requestProcessor == null) {
			requestProcessor = new RequestProcessor(createEndpoints(), getFilters());
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Destroying APP service");
		TempServiceConnection.terminateAll();
		if (localServer != null) {
			localServer.shutdown();
			localServer = null;
		}
		if (executorService != null) {
			executorService.shutdown();
			executorService = null;
		}
		authToken = null;
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

	@Override
	public String getPackage() {
		return getContext().getPackageName();
	}

	@Override
	public String getAuthToken() {
		return authToken;
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

	public RequestProcessor getHttpService() {
		return requestProcessor;
	}
}

