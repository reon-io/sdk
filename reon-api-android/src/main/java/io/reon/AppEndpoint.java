package io.reon;

import android.content.Context;
import io.reon.http.HttpServiceUnavailableException;

public abstract class AppEndpoint extends Endpoint {
	private final Context ctx;

	public AppEndpoint(WebAppContext context) {
		super(context);
		ctx = context.getContext();
	}

	public WebAppContext getWebAppContext() {
		return (WebAppContext) getWebContext();
	}

	protected Context getContext() {
		return ctx;
	}

	@Override
	protected ActualParam matchActualParam(FormalParam fp, InvocationContext ic) {
		ActualParam ap = super.matchActualParam(fp, ic);
		if (ap == null) {
			Class<?> fpClazz;
			try {
				fpClazz = classForName(fp.getTypeName());
			} catch (ClassNotFoundException e) {
				throw new HttpServiceUnavailableException(e.getMessage(), e);
			}
			if (fp.getName().equals(getServiceName())) {
				ap = new ActualParam(fpClazz, getServiceObject());
			} else if (Context.class.equals(fpClazz))
				ap = new ActualParam(Context.class, getContext());
		}
		return  ap;
	}
}
