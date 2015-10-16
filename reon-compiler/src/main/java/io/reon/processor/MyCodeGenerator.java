package io.reon.processor;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

import io.reon.processor.model.AssetFile;
import io.reon.processor.model.Exported;
import io.reon.processor.model.ParsedFilter;
import io.reon.processor.model.ParsedMethod;
import io.reon.processor.model.Provider;
import io.reon.processor.velocity.VelocityLogger;

import static com.google.common.io.Files.fileTreeTraverser;
import static com.google.common.io.Files.isFile;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class MyCodeGenerator extends ProcessingEnvAware {
	public static final String PKG_PREFIX = "io.reon.";
	private String sourceCodePath = "";

	public static class EscTool {
		public String java(String str) {
			return str.replaceAll("\\\\","\\\\\\\\");
		}
	}

	public MyCodeGenerator(ProcessingEnvironment processingEnvironment) {
		super(processingEnvironment);
	}

	public void generateCode(List<ParsedMethod> parsedMethods, List<Provider> providers, List<ParsedFilter> filters, List<Exported> exports) {
		VelocityEngine ve = instantiateVelocityEngine();
		if (!providers.isEmpty() || !exports.isEmpty()) {
			generateProviders(ve, providers);
			generateExports(ve, exports);
		} else {
			generateEndpoints(ve, parsedMethods);
			generateFilters(ve, filters);
			generateAssetsInfo(ve, sourceCodePath);
			generateService(ve, parsedMethods, filters);
		}
	}

	private void generateFilters(VelocityEngine ve, List<ParsedFilter> filters) {
		for (ParsedFilter filter : filters) {
			Context ctx = createContext();
			ctx.put("filter", filter);
			generateFromTemplate(ve, ctx, filter.getPackage() + "." + filter.getGeneratedClassName(), "filter.vm");
		}
	}

	private void generateProviders(VelocityEngine ve, List<Provider> providers) {
		for (Provider provider : providers) {
			Context ctx = createContext();
			ctx.put("provider", provider);
			generateFromTemplate(ve, ctx, PKG_PREFIX + provider.getGeneratedClassName(), "provider.vm");
		}
	}

	private void generateExports(VelocityEngine ve, List<Exported> exports) {
		for (Exported export : exports) {
			Context ctx = createContext();
			ctx.put("export", export);
			generateFromTemplate(ve, ctx, export.getPackage() + "." + export.getGeneratedClassName(), "export.vm");
		}
	}

	private void generateEndpoints(VelocityEngine ve, List<ParsedMethod> parsedMethods) {
		for (ParsedMethod pm : parsedMethods) {
			Context ctx = createContext();
			ctx.put("parsedMethod", pm);
			generateFromTemplate(ve, ctx, pm.getPackage() + "." + pm.getGeneratedClassName(), "endpoint.vm");
		}
	}

	private void generateService(VelocityEngine ve, List<ParsedMethod> parsedMethods, List<ParsedFilter> filters) {
		Context ctx = createContext();
		ctx.put("endpoints", parsedMethods);
		ctx.put("filters", filters);
		generateFromTemplate(ve, ctx, PKG_PREFIX + "MyServices");
	}

	private VelocityEngine instantiateVelocityEngine() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new VelocityLogger(getMessager()));
		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();
		return ve;
	}

	private VelocityContext createContext() {
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("esc", new EscTool());
		return velocityContext;
	}

	private void generateAssetsInfo(VelocityEngine ve, String sourceCodePath) {
		String currentProjectPath = substringBeforeLast(sourceCodePath, "/build/");
		final String mywebAssetDir = currentProjectPath + "/src/main/assets/reon";
		FluentIterable<File> filesAndDirs = fileTreeTraverser().breadthFirstTraversal(new File(mywebAssetDir));
		FluentIterable<File> files = filesAndDirs.filter(isFile());
		FluentIterable<AssetFile> assetFiles = files.transform(new Function<File, AssetFile>() {
			@Override
			public AssetFile apply(File f) {
				String relativePath = substringAfter(f.getAbsolutePath(), mywebAssetDir);
				return new AssetFile(relativePath, f.length());
			}
		});
		generateAssetInfo(ve, assetFiles.toList());
	}

	private void generateAssetInfo(VelocityEngine ve, List<AssetFile> assetFiles) {
		Context ctx = createContext();
		ctx.put("assetFiles", assetFiles);
		generateFromTemplate(ve, ctx, PKG_PREFIX + "MyAssetInfo");
	}

	private void generateFromTemplate(VelocityEngine ve, Context ctx, String classToGenerate) {
		generateFromTemplate(ve, ctx, classToGenerate, classToGenerate + ".vm");
	}

	private void generateFromTemplate(VelocityEngine ve, Context ctx, String classToGenerate, String templateName) {
		Template t = ve.getTemplate(templateName);
		Writer w = null;
		try {
			OutputStream os = null;
			JavaFileObject sourceFile = getProcessingEnv().getFiler().createSourceFile(classToGenerate);
			updateSourceCodePath(sourceFile, classToGenerate);
			os = sourceFile.openOutputStream();
			w = new PrintWriter(os);
		} catch (IOException e) {
			error("Cannot create file: " + e.toString());
			return;
		}
		t.merge(ctx, w);
		try {
			w.close();
		} catch (IOException e) {
		}
	}

	private void updateSourceCodePath(JavaFileObject sourceFile, String classToGenerate) {
		if (sourceCodePath.length()==0) {
			String fullPath = sourceFile.toUri().getPath();
			fullPath = fullPath.substring(0,fullPath.lastIndexOf("/"));
			String resourcePath = classToGenerate.replace(".","/");
			resourcePath = resourcePath.substring(0,resourcePath.lastIndexOf("/"));
			sourceCodePath = fullPath.replace(resourcePath, "");
		}
	}
}
