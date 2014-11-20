package se.bjurr.validator;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;
import com.google.gson.Gson;

/**
 * This is a test case that, given a URL, will validate it as HTML5.<br>
 * The parameterization is added so that the test can generate a graph in
 * Jenkins, with number of failing test cases.
 */
@RunWith(Parameterized.class)
public class ValidatorTest {

	private static class Reportable {
		public static enum Level {
			error, fatal, info, warning
		}

		private final Level level;
		private final ValidationResponseMessage validationResponseMessage;

		public Reportable(Level level, ValidationResponseMessage vrm) {
			this.level = level;
			this.validationResponseMessage = vrm;
		}

		@Override
		public String toString() {
			return level.name() + " " + validationResponseMessage;
		}
	}

	private static final int PLUSMIN = 5;
	private static final LoadingCache<String, String> urlContent = newBuilder().maximumSize(100).build(
			new CacheLoader<String, String>() {
				@Override
				public String load(String key) throws Exception {
					return Resources.toString(new URL(key), UTF_8);
				}
			});

	private static List<String> urls = newArrayList("http://fk.se/");

	@Parameters(name = "{index} {0}")
	public static List<Object[]> before() throws Exception {
		final Server server = startServer();
		final List<Object[]> reportables = newArrayList();
		for (final String url : urls) {
			final ValidationResponse validationReponse = doValidate(url);
			for (final ValidationResponseMessage vrm : validationReponse.getMessages()) {
				final se.bjurr.validator.ValidatorTest.Reportable.Level level = Reportable.Level.valueOf(vrm.getType());
				final Object[] array = { validationReponse, new Reportable(level, vrm) };
				reportables.add(array);
			}
		}
		server.stop();
		return reportables;
	}

	private static ValidationResponse doValidate(String url) throws MalformedURLException, IOException {
		final String apiUrl = "http://localhost:8080/?laxtype=yes&asciiquotes=yes&out=json&doc=" + url;
		final String apiResponse = Resources.toString(new URL(apiUrl), Charsets.UTF_8);
		System.out.println(apiUrl);
		System.out.println(apiResponse);
		return new Gson().fromJson(apiResponse, ValidationResponse.class);
	}

	private static Server startServer() throws Exception, InterruptedException {
		final Server server = new Server(8080);
		final WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		final URL warResource = ValidatorTest.class.getClassLoader().getResource("vnu.war");
		webapp.setWar(warResource.getFile());
		server.setHandler(webapp);
		server.start();
		return server;
	}

	private final Reportable reportable;
	private final ValidationResponse validationReponse;

	public ValidatorTest(ValidationResponse validationReponse, Reportable reportable) {
		this.validationReponse = validationReponse;
		this.reportable = reportable;
	}

	private String context(String string, Integer at, int plusMin) {
		if (at == null) {
			return "";
		}
		final StringBuffer sb = new StringBuffer();
		final String[] lines = string.split("\n");
		for (int i = at - plusMin; i < at + plusMin; i++) {
			if (i >= 0 && i < lines.length) {
				sb.append(i + 1 + "> " + lines[i]);
			}
		}
		return sb.toString();
	}

	@Test
	public void reportFailure() throws ExecutionException {
		fail(reportable.toString()
				+ "\n"
				+ context(
						urlContent.get(validationReponse.getUrl()),
						firstNonNull(reportable.validationResponseMessage.getFirstLine(),
								firstNonNull(reportable.validationResponseMessage.getLastLine(), 0)), PLUSMIN));
	}
}
