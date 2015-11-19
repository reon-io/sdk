package io.reon;

import java.util.List;

import io.reon.auth.TokenAuth;
import io.reon.http.HttpService;

public interface WebContext {

	List<Endpoint.Info> getEndpointInfos();

	List<Filter> getFilters();

	HttpService getHttpService();

	AssetLengthInfo getAssetInfo();

	String getPackage();

	TokenAuth getTokenAuth();
}
