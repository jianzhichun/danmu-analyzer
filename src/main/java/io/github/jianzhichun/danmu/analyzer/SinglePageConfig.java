package io.github.jianzhichun.danmu.analyzer;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class SinglePageConfig extends WebMvcConfigurerAdapter {

	private static final String API_PATH = "/api";
	private static final String PATH_PATTERNS = "/**";
	private static final String FRONT_CONTROLLER = "index.html";

	private final ResourceProperties resourceProperties;

	public SinglePageConfig(ResourceProperties resourceProperties) {
		this.resourceProperties = resourceProperties;
	}

	@Override public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(PATH_PATTERNS).addResourceLocations(resourceProperties.getStaticLocations())
				.resourceChain(true).addResolver(new SinglePageAppResourceResolver());
	}

	private class SinglePageAppResourceResolver extends PathResourceResolver {

		public static final String DIRECTORY_SEPARATOR = "/";

		@Override protected Resource getResource(String resourcePath, Resource location) throws IOException {
			Resource resource = location.createRelative(resourcePath);
			if (resource.exists() && resource.isReadable()) {
				if (checkResource(resource, location)) {
					return resource;
				}
				else if (logger.isTraceEnabled()) {
					logger.trace("Resource path=\"" + resourcePath + "\" was successfully resolved " +
							"but resource=\"" +	resource.getURL() + "\" is neither under the " +
							"current location=\"" + location.getURL() + "\" nor under any of the " +
							"allowed locations=" + Arrays.asList(getAllowedLocations()));
				}
			}

			// do not serve a Resource on an reserved URI
			if ((DIRECTORY_SEPARATOR + resourcePath).startsWith(API_PATH)) {
				return null;
			}

			resource = location.createRelative(FRONT_CONTROLLER);
			if (resource.exists() && resource.isReadable()) {
				return resource;
			}

			return null;
		}
	}
}