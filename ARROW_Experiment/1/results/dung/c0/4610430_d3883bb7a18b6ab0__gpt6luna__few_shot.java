package ninja.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
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
    public void defaultSuffixIsFreemarkerHtml() throws Exception {
        TemplateEngineFreemarker engine = createEngine();
        assertEquals(".ftl.html", engine.getSuffixOfTemplatingEngine());
    }

    @Test
    public void configurationIsAvailable() throws Exception {
        TemplateEngineFreemarker engine = createEngine();
        Configuration configuration = engine.getConfiguration();
        assertNotNull(configuration);
        assertSame(configuration, engine.getConfiguration());
    }

    @Test
    public void unknownRenderingFailureUsesGenericRenderingException() throws Exception {
        TemplateEngineFreemarker engine = createEngine();
        Result result = new Result();
        Exception cause = new Exception("failure");

        try {
            engine.throwRenderingException(null, result, cause, "template.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException exception) {
            assertEquals("failure", exception.getMessage());
            assertSame(cause, exception.getCause());
        }
    }

    @Test
    public void templateNotFoundUsesSpecificRenderingException() throws Exception {
        TemplateEngineFreemarker engine = createEngine();
        Result result = new Result();
        freemarker.template.TemplateNotFoundException cause =
                new freemarker.template.TemplateNotFoundException(
                        "missing.ftl.html", null, "not found");

        try {
            engine.throwRenderingException(null, result, cause, "missing.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException exception) {
            assertEquals("not found", exception.getMessage());
            assertSame(cause, exception.getCause());
        }
    }
}
