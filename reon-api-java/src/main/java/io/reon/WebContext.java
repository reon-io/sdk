package io.reon;

import android.content.ComponentName;

import java.util.List;

public interface WebContext {
	Object bindService(ComponentName name);

	List<Endpoint.Info> getEndpointInfos();

	List<Filter> getFilters();

	RequestProcessor getRequestProcessor();

	AssetLengthInfo getAssetInfo();
}
