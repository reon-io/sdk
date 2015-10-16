package io.reon.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import io.reon.api.After;
import io.reon.api.Before;
import io.reon.api.BindService;
import io.reon.api.ContentProvider;
import io.reon.api.DELETE;
import io.reon.api.Export;
import io.reon.api.GET;
import io.reon.api.POST;
import io.reon.api.PUT;
import io.reon.api.Produces;
import io.reon.http.Method;
import io.reon.processor.model.Exported;
import io.reon.processor.model.ParsedFilter;
import io.reon.processor.model.ParsedMethod;
import io.reon.processor.model.Provider;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MyAnnotationProcessor extends AbstractProcessor {
	private final List<InternalProcessor> processors;
	private List<ParsedMethod> parsedMethods = new ArrayList<>();
	private List<ParsedFilter> parsedFilters = new ArrayList<>();
	private Set<ExecutableElement> processed = new HashSet<>();
	private List<Provider> providers;
	private List<Exported> exports;

	public MyAnnotationProcessor() {
		ArrayList<InternalProcessor> p = new ArrayList<InternalProcessor>();
		p.add(new HttpMethodProcessor());
		p.add(new ContentProviderProcessor());
		p.add(new FilterProcessor());
		p.add(new ExportProcessor());
		processors = Collections.unmodifiableList(p);
	}

	private void error(String msg) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
	}

	private void warning(String msg) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotationTypes = new HashSet<String>();
		for (InternalProcessor ip : processors) {
			annotationTypes.addAll(ip.supportedTypes());
		}
		return annotationTypes;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.isEmpty()) return false;
		MyCodeGenerator mCodeGenerator = new MyCodeGenerator(processingEnv);
		try {
			providers = new ArrayList<>();
			exports = new ArrayList<>();
			for (TypeElement annotation : annotations) {
//				System.out.println("Processing: " + annotation.getQualifiedName().toString());
				for(InternalProcessor ip: processors) {
					if (ip.consume(annotation, roundEnv)) break;
				}
			}
			mCodeGenerator.generateCode(parsedMethods, providers, parsedFilters, exports);
			return true;
		} catch (Exception e) {
//			e.printStackTrace();
			error("error(s) found - details above: " + e.getMessage());
		}
		return false;
	}

	private static List<Method> convertMethods(List<Object> attrs) {
		LinkedList<Method> lm = new LinkedList<Method>();
		if (attrs == null) {
			return Arrays.asList(Method.GET, Method.PUT, Method.POST, Method.DELETE);
		}
		for (Object attr : attrs) {
			String name = attr.toString();
			lm.add(Method.findByName(name.substring(name.lastIndexOf(".") + 1)));
		}
		return lm;
	}

	private class InternalProcessor {
		private final List<String> types;

		protected InternalProcessor(List<String> types) {
			this.types = types;
		}

		public List<String> supportedTypes() {
			return types;
		}

		public boolean consume(TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
			for(String name: types) {
				if (name.equals(annotation.getQualifiedName().toString())) {
					return process(annotation, roundEnv);
				}
			}
			return false;
		}

		protected boolean process(TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
			return false;
		}
	}

	private class HttpMethodProcessor extends InternalProcessor {
		MyValidator mValidator = null;
		MyParser mParser = null;

		public HttpMethodProcessor() {
			super(Arrays.asList(GET.class.getName(), PUT.class.getName(), DELETE.class.getName(),
					POST.class.getName(), Produces.class.getName(), BindService.class.getName()));
		}

		@Override
		protected boolean process(TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
			if (mParser == null) {
				mValidator = new MyValidator(processingEnv.getMessager());
				mParser = new MyParser(processingEnv.getMessager(), mValidator);
			}
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				ExecutableElement ee = (ExecutableElement) element;
				if (!processed.contains(ee)) {
					processed.add(ee);
					ParsedMethod parsedMethod = mParser.parse(ee);
					parsedMethods.add(parsedMethod);
				}
			}
			return true;
		}
	}

	private class ContentProviderProcessor extends InternalProcessor {
		public ContentProviderProcessor() {
			super(Arrays.asList(ContentProvider.class.getName()));
		}

		@Override
		@SuppressWarnings("unchecked")
		protected boolean process(TypeElement annotation, RoundEnvironment roundEnv) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				TypeElement ce = (TypeElement) element;
				if (ce.getQualifiedName().toString().equals(""))
					error("Annotation @" + annotation.getSimpleName() + " is not allowed for local or anonymous classes!");
				Object v = getAnnotationValue(annotation, ce, "value");
				String value = v != null ? v.toString() : null;
				if (value != null && value.length() > 0) {
					List<Method> methods = convertMethods((List<Object>) getAnnotationValue(annotation, ce, "methods"));
					Provider p = new Provider(value, ce.getSimpleName().toString(), methods);
					providers.add(p);
				}
			}
			return true;
		}
	}

	private Object getAnnotationValue(TypeElement annotation, Element element, String name) {
		for (AnnotationMirror am: element.getAnnotationMirrors()) {
			if (am.getAnnotationType().toString().equals(annotation.getQualifiedName().toString())) {
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
					if (entry.getKey().getSimpleName().toString().equals(name)) {
						return entry.getValue().getValue();
					}
				}
			}
		}
		return null;
	}

	private class FilterProcessor extends InternalProcessor {
		MyValidator mValidator = null;

		public FilterProcessor() {
			super(Arrays.asList(Before.class.getName(), After.class.getName()));
		}

		@Override
		protected boolean process(TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
			if (mValidator == null) {
				mValidator = new MyValidator(processingEnv.getMessager());
			}
			boolean processed = false;
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				ExecutableElement ee = (ExecutableElement) element;
				Object v = getAnnotationValue(annotation, element, "value");
				String value = v != null ? v.toString() : null;
				boolean isBefore = annotation.getQualifiedName().toString().equals(Before.class.getName());
				// validate parameters and return type
				ParsedFilter filter = mValidator.validateFilterAnnotation(value, isBefore, ee);
				parsedFilters.add(filter);
				processed = true;
			}
			return processed;
		}
	}

	private class ExportProcessor extends InternalProcessor {
		private static final String EXPORTED_CLASS = "io.reon.WebService";
		protected ExportProcessor() {
			super(Arrays.asList(Export.class.getName()));
		}

		@Override
		protected boolean process(TypeElement annotation, RoundEnvironment roundEnv) {
			for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
				TypeElement ce = (TypeElement) element;
				if (ce.getQualifiedName().toString().equals(""))
					error("Annotation @" + annotation.getSimpleName() + " is not allowed for local or anonymous classes!");
				if (!ce.getSuperclass().toString().equals(EXPORTED_CLASS)) {
					error("Only classes extending " +EXPORTED_CLASS+ " can be annotated wit @" + annotation.getSimpleName() + "!");
				}
				Exported exp = new Exported(ce.getQualifiedName().toString());
				exports.add(exp);
			}
			return true;
		}
	}

}
