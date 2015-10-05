package io.reon;

import android.content.ComponentName;
import android.content.Context;

import java.util.List;

public interface WebContext {
	Context getContext();
	Object bindService(ComponentName name);
	List<Endpoint.Info> getEndpointInfos();
	List<Filter> getFilters();
	AssetLengthInfo getAssetInfo();
	RequestProcessor getRequestProcessor();
}
