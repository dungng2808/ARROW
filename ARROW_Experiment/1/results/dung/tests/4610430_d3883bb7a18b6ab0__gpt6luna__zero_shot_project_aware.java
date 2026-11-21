package ninja.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import freemarker.template.Configuration;
import ninja.Context;
import ninja.Result;
import ninja.exceptions.RenderingException;

public class TemplateEngineFreemarker_ArrowTest {

    @Test
    public void getContentTypeReturnsHtml() throws Exception {
        TemplateEngineFreemarker engine = new TemplateEngineFreemarker(
                null, null, null, null, null, null, null, null, null);

        assertEquals("text/html", engine.getContentType());
    }

    @Test
    public void defaultSuffixIsFreemarkerHtml() throws Exception {
        TemplateEngineFreemarker engine = new TemplateEngineFreemarker(
                null, null, null, null, null, null, null, null, null);

        assertEquals(".ftl.html", engine.getSuffixOfTemplatingEngine());
    }

    @Test
    public void configurationIsAvailable() throws Exception {
        TemplateEngineFreemarker engine = new TemplateEngineFreemarker(
                null, null, null, null, null, null, null, null, null);

        Configuration configuration = engine.getConfiguration();

        assertNotNull(configuration);
        assertEquals("UTF-8", configuration.getDefaultEncoding());
        assertEquals("UTF-8", configuration.getOutputEncoding());
    }

    @Test
    public void throwRenderingExceptionUsesKnownPathForGenericCause() throws Exception {
        TemplateEngineFreemarker engine = new TemplateEngineFreemarker(
                null, null, null, null, null, null, null, null, null);
        Result result = new Result();
        IllegalStateException cause = new IllegalStateException("failure");

        try {
            engine.throwRenderingException(null, result, cause, "templates/page.ftl.html");
        } catch (RenderingException exception) {
            assertEquals("failure", exception.getMessage());
            assertSame(cause, exception.getCause());
            assertEquals("templates/page.ftl.html", exception.getTemplateSourcePath());
            return;
        }
        throw new AssertionError("Expected RenderingException");
    }
}
