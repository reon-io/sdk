package io.reon;

import java.util.List;

public interface WebContext {

	List<Endpoint.Info> getEndpointInfos();

	List<Filter> getFilters();

	RequestProcessor getRequestProcessor();

	AssetLengthInfo getAssetInfo();
}
