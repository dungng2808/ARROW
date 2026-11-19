package ninja.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import freemarker.template.Configuration;
import ninja.Context;
import ninja.Result;
import ninja.exceptions.RenderingException;
import org.junit.Test;

public class TemplateEngineFreemarker_ArrowTest {

    private TemplateEngineFreemarker createEngine() throws Exception {
        return new TemplateEngineFreemarker(
                null, null, null, null, null, null, null, null, null);
    }

    @Test
    public void contentTypeIsHtml() throws Exception {
        TemplateEngineFreemarker engine = createEngine();

        assertEquals("text/html", engine.getContentType());
    }

    @Test
    public void defaultTemplateSuffixIsFreemarkerHtml() throws Exception {
        TemplateEngineFreemarker engine = createEngine();

        assertEquals(".ftl.html", engine.getSuffixOfTemplatingEngine());
    }

    @Test
    public void configurationIsAvailable() throws Exception {
        TemplateEngineFreemarker engine = createEngine();

        Configuration configuration = engine.getConfiguration();

        assertNotNull(configuration);
        assertEquals("UTF-8", configuration.getDefaultEncoding());
        assertEquals("UTF-8", configuration.getOutputEncoding());
        assertEquals("0.######", configuration.getNumberFormat());
    }

    @Test
    public void genericRenderingExceptionUsesKnownTemplatePath() throws Exception {
        TemplateEngineFreemarker engine = createEngine();
        Context context = null;
        Result result = null;
        IllegalStateException cause = new IllegalStateException("failure");

        try {
            engine.throwRenderingException(context, result, cause, "templates/page.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException exception) {
            assertEquals("failure", exception.getMessage());
            assertSame(cause, exception.getCause());
        }
    }
}
