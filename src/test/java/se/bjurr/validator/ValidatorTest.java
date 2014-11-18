package se.bjurr.validator;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.htmlparser.sax.HtmlSerializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This is a test case that, given a URL, will validate it as HTML5.<br>
 * The parameterization is added so that the test can generate a graph in
 * Jenkins, with number of failing test cases.
 */
@RunWith(Parameterized.class)
public class ValidatorTest {
	private static class Reportable {
		public static enum Level {
			ERROR, FATAL, WARNING
		}

		private final SAXParseException exception;
		private final Level level;

		public Reportable(Level level, SAXParseException exception) {
			this.level = level;
			this.exception = exception;
		}

		public Throwable getException() {
			return exception;
		}

		public Level getLevel() {
			return level;
		}

		@Override
		public String toString() {
			return level.name() + " Line: " + exception.getLineNumber() + " Column: " + exception.getColumnNumber()
					+ " Message: " + exception.getMessage();
		}
	}

	private static List<String> urls = newArrayList("http://www.forsakringskassan.se/privatpers/");

	@Parameters(name = "{index} {0}")
	public static List<Object[]> before() throws IOException, SAXException {
		final List<Object[]> reportables = newArrayList();
		final HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
		parser.setContentHandler(new HtmlSerializer(System.out));
		urls.forEach(url -> {
			parser.setErrorHandler(new ErrorHandler() {
				@Override
				public void error(SAXParseException exception) throws SAXException {
					final Object[] array = { new Reportable(Reportable.Level.ERROR, exception) };
					reportables.add(array);
				}

				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					final Object[] array = { new Reportable(Reportable.Level.FATAL, exception) };
					reportables.add(array);
				}

				@Override
				public void warning(SAXParseException exception) throws SAXException {
					final Object[] array = { new Reportable(Reportable.Level.WARNING, exception) };
					reportables.add(array);
				}
			});

			try {
				parser.parse(new InputSource(new URL(url).openConnection().getInputStream()));
			} catch (final Exception e) {
				propagate(e);
			}
		});
		return reportables;
	}

	private final Reportable reportable;

	public ValidatorTest(Reportable reportable) {
		this.reportable = reportable;
	}

	@Test
	public void reportFailure() {
		if (!Reportable.Level.WARNING.equals(reportable.getLevel())) {
			fail(reportable.getException().getMessage());
		}
		System.out.println(reportable);
	}
}
