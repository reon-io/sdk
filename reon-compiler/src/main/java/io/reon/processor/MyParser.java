package io.reon.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import io.reon.api.BindService;
import io.reon.api.DELETE;
import io.reon.api.GET;
import io.reon.api.POST;
import io.reon.api.PUT;
import io.reon.api.Produces;
import io.reon.http.Method;
import io.reon.http.MimeTypes;
import io.reon.processor.model.ParsedMethod;
import io.reon.processor.model.ParsedParam;
import io.reon.processor.model.ServiceParam;

public class MyParser extends AnnotationMessagerAware {

	private final MyValidator myValidator;

	public MyParser(Messager messager, MyValidator myValidator) {
		super(messager);
		this.myValidator = myValidator;
	}

	public ParsedMethod parse(ExecutableElement ee) throws Exception {
		List<ParsedParam> parasedParams = extractMethodParams(ee);
		return extractParsedMethod(ee, parasedParams);
	}

	private ParsedMethod extractParsedMethod(ExecutableElement ee, List<ParsedParam> params) throws Exception {
		String destClass = ee.getEnclosingElement().toString();
		String destMethod = ee.getSimpleName().toString();
		String destMethodRetType = ee.getReturnType().toString();
		Method httpMethod = null;
		String httpUri = "/";
		String produces = MimeTypes.MIME_TEXT_HTML;
		ServiceParam service = null;
		List<? extends AnnotationMirror> annotationMirrors = ee.getAnnotationMirrors();
		for (AnnotationMirror am : annotationMirrors) {
			Method m = extractHttpMethod(am);
			if (httpMethod == null) {
				httpMethod = m;
				httpUri = validateAndExtractHttpUri(am, httpMethod, destMethodRetType, destMethod, params, ee);
			} else if (m != null) {
				throw new IllegalArgumentException("Duplicate annotations @" + httpMethod.toString()
						+ " and @" + m.toString() + " for method " + destClass + "." + destMethod + "()");
			}
			if (produces.equals(MimeTypes.MIME_TEXT_HTML)) {
				produces = extractProducesAnnotation(am);
			}
			if (service == null) {
				service = validateAndExtractServiceAnnotation(params, am, ee);
			}
		}
		return new ParsedMethod(destClass, destMethod, destMethodRetType, params, httpMethod, httpUri, produces, service);
	}

	private ServiceParam validateAndExtractServiceAnnotation(List<ParsedParam> params, AnnotationMirror am, ExecutableElement ee) {
		ServiceParam service = null;
		if (isBindServiceAnnotation(am)) {
			String value = getAnnotationValue(am, "value");
			service = myValidator.validateBindServiceAnnotation(value, params, ee, am);
		}
		return service;
	}

	private String validateAndExtractHttpUri(AnnotationMirror am, Method httpMethod, String destMethodRetType, String destMethod, List<ParsedParam> params, ExecutableElement ee) {
		String httpUri = "/";
		if (isHttpMethodAnnotation(am)) {
			httpUri = getAnnotationValue(am, "value");
			myValidator.validateAnnotation(httpMethod, destMethodRetType, destMethod, params, httpUri, ee, am);
		}
		return httpUri;
	}

	private static String getAnnotationValue(AnnotationMirror am, String name) {
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
			if (name.equals(entry.getKey().getSimpleName().toString())) {
				return entry.getValue().getValue().toString();
			}
		}
		return null;
	}

	private static String getAnnotationName(AnnotationMirror am) {
		return am.getAnnotationType().toString();
	}

	private static boolean isHttpMethodAnnotation(String annotationName) {
		return GET.class.getName().equals(annotationName) || POST.class.getName().equals(annotationName)
				|| DELETE.class.getName().equals(annotationName) || PUT.class.getName().equals(annotationName);
	}

	private static boolean isHttpMethodAnnotation(AnnotationMirror am) {
		return isHttpMethodAnnotation(getAnnotationName(am));
	}

	private static boolean isProducesAnnotation(AnnotationMirror am) {
		return Produces.class.getName().equals(getAnnotationName(am));
	}

	private static boolean isBindServiceAnnotation(AnnotationMirror am) {
		return BindService.class.getName().equals(getAnnotationName(am));
	}

	private Method extractHttpMethod(AnnotationMirror am) {
		String annotationName = getAnnotationName(am);
		Method httpMethod = null;
		if (isHttpMethodAnnotation(annotationName)) {
			httpMethod = Method.findByName(annotationName.substring(annotationName.lastIndexOf(".") + 1).trim());
		}
		return httpMethod;
	}

	private String extractProducesAnnotation(AnnotationMirror am) {
		String produces = MimeTypes.MIME_TEXT_HTML;
		if (isProducesAnnotation(am)) {
			for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
				if ("value".equals(entry.getKey().getSimpleName().toString())) {
					produces = entry.getValue().getValue().toString();
				}
			}
		}
		return produces;
	}

	private List<ParsedParam> extractMethodParams(ExecutableElement ee) {
		List<? extends VariableElement> parameters = ee.getParameters();
		List<ParsedParam> params = new LinkedList<ParsedParam>();
		int i = 0;
		for (VariableElement p : parameters) {
			ParsedParam pp = extractMethodParam(i++, p);
			params.add(pp);
		}
		return params;
	}

	private ParsedParam extractMethodParam(int paramNumber, VariableElement variableElement) {
		ParsedParam pp = null;
		try {
			String type = variableElement.asType().toString().replaceFirst("class ", "");
			pp = new ParsedParam(paramNumber, type, variableElement.getSimpleName().toString());
		} catch (ClassNotFoundException e) {
			error("cannot load class: " + e.getMessage());
		}
		return pp;
	}
}
