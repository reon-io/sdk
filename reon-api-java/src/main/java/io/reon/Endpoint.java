package io.reon;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reon.http.Cookie;
import io.reon.http.Cookies;
import io.reon.http.Headers;
import io.reon.http.HttpException;
import io.reon.http.HttpServiceUnavailableException;
import io.reon.http.Method;
import io.reon.http.Request;
import io.reon.http.Response;

public abstract class Endpoint {

	public static class Info {
		private final Method method;
		private final String uri;
		private final Class implementingClass;

		public Info(Method method, String uri, Class implementingClass) {
			this.method = method;
			this.uri = uri;
			this.implementingClass = implementingClass;
		}

		public Method getMethod() {
			return method;
		}

		public String getUri() {
			return uri;
		}

		public Class getImplementingClass() {
			return implementingClass;
		}
	}

	protected class InvocationContext {
		public final String uri;
		public final Request request;
		public final FormalParam[] formalParams;
		public final Map<String, String> defaultQueryParams;
		public final Map<String, Integer> groupMap;
		public final Matcher matcher;

		public InvocationContext(String uri, Request request, FormalParam[] formalParams, Map<String, String> defaultQueryParams, Map<String, Integer> groupMap) {
			this.uri = uri;
			this.request = request;
			this.formalParams = formalParams;
			this.defaultQueryParams = defaultQueryParams;
			this.groupMap = groupMap;
			matcher = getMatcher(urlNoQueryParams(uri));
		}
	}

	private WebContext webContext;

	public Endpoint(WebContext context) {
		webContext = context;
	}

	public WebContext getWebContext() {
		return webContext;
	}

	public String produces() {
		return null;
	}

	public abstract Response invoke(String uri, Request request) throws HttpException, IOException;

	protected String getServiceName() { // overriden in subclasses
		return null;
	}

	protected Object getServiceObject() { // overriden in subclasses
		return null;
	}

	protected abstract Pattern getPattern();

	protected Matcher getMatcher(String uri) {
		return getPattern().matcher(uri);
	}

	public boolean match(Method method, String uri) {
		String urlNoQueryParams = urlNoQueryParams(uri);
		Matcher m = getMatcher(urlNoQueryParams);
		boolean matched = httpMethod().equals(method) && m.matches();
//		if (matched) {
//			Log.d("Endpoint", "matched path " + httpMethod() + " " + originalPath() + " (pattern: " + getPattern() + ") request: " + method + " " + uri);
//		} else {
//			Log.d("Endpoint", "not matched path " + httpMethod() + " " + originalPath() + " (pattern: " + getPattern() + ") request: " + method + " " + uri);
//		}
		return matched;
	}

	protected abstract String originalPath();

	protected abstract Method httpMethod();

	protected ActualParam matchActualParam(FormalParam fp, InvocationContext ic) {
		Class<?> fpClazz;
		try {
			fpClazz = classForName(fp.getTypeName());
		} catch (ClassNotFoundException e) {
			throw new HttpServiceUnavailableException(e.getMessage(), e);
		}
		Map<String, String> paramsMap = ic.request.getParameterMap();
		String fpName = fp.getName();
		if (fpName.equals(getServiceName())) {
			return new ActualParam(fpClazz, getServiceObject());
		} else if (ic.groupMap.containsKey(fpName)) {
			int urlGroupIdx = ic.groupMap.get(fpName);
			String val = ic.matcher.group(urlGroupIdx);
			Object convertedVal = convert(val, fp.getTypeName());
			return new ActualParam(fpClazz, convertedVal);
		} else if (Cookies.class.equals(fpClazz)){
			return new ActualParam(Cookies.class, ic.request.getCookies());
		} else if (Cookie.class.equals(fpClazz)) {
			Cookie cookie = ic.request.getCookies().getCookie(fpName);
			return new ActualParam(Cookie.class, cookie);
		} else if (Headers.class.equals(fpClazz)) {
			return new ActualParam(Headers.class, ic.request.getHeaders());
		} else if (Request.class.equals(fpClazz)) {
			return new ActualParam(Request.class, ic.request);
		} else if (paramsMap.containsKey(fpName)) {
			String val = paramsMap.get(fpName);
			Object convertedVal = convert(val, fp.getTypeName());
			return new ActualParam(fpClazz, convertedVal);
		} else if (ic.defaultQueryParams.containsKey(fpName)) {
			String val = ic.defaultQueryParams.get(fpName);
			Object convertedVal = convert(val, fp.getTypeName());
			return new ActualParam(fpClazz, convertedVal);
		}
		return null;
	}

	protected ActualParam[] actualParams(InvocationContext ic) throws HttpException {
		ActualParam[] actualParams = new ActualParam[ic.formalParams.length];
		if (ic.matcher.matches()) {
			for (FormalParam fp : ic.formalParams) {
				actualParams[fp.getId()] = matchActualParam(fp, ic);
			}
		} else {
			throw new HttpServiceUnavailableException("couldn't match URI: '" + ic.uri + "' with pattern '" + getPattern() + "' (BTW, this shouldn't happen...)");
		}
		return actualParams;
	}

	protected String urlNoQueryParams(String url) {
		int i = url.indexOf("?");
		String result;
		if (i == -1) {
			result = url;
		} else {
			result = url.substring(0, i);
		}
		return result;
	}

	public static Object convert(String val, String typeName) {
		if(!String.class.getName().equals(typeName)) {
			try {
				Object obj = new JSONTokener(val).nextValue();
				if(obj.equals(JSONObject.NULL)) return null;
				if(classForName(typeName).isAssignableFrom(obj.getClass())) return obj;
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return val;
	}

	public static Class<?> classForName(String typeName) throws ClassNotFoundException {
		if ("int".equals(typeName)) return Integer.class;
		if ("long".equals(typeName)) return Long.class;
		if ("float".equals(typeName)) return Float.class;
		if ("double".equals(typeName)) return Double.class;
		if ("boolean".equals(typeName)) return Boolean.class;
		return Class.forName(typeName);
	}
}
