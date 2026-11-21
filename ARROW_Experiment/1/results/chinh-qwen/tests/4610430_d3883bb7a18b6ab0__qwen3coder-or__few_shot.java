package ninja.template;

import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import ninja.Context;
import ninja.Result;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class TemplateEngineFreemarker_ArrowTest {

    @Mock
    private ninja.i18n.Messages messages;

    @Mock
    private ninja.i18n.Lang lang;

    @Mock
    private Logger logger;

    @Mock
    private TemplateEngineHelper templateEngineHelper;

    @Mock
    private TemplateEngineManager templateEngineManager;

    @Mock
    private TemplateEngineFreemarkerReverseRouteMethod templateEngineFreemarkerReverseRouteMethod;

    @Mock
    private TemplateEngineFreemarkerAssetsAtMethod templateEngineFreemarkerAssetsAtMethod;

    @Mock
    private TemplateEngineFreemarkerWebJarsAtMethod templateEngineFreemarkerWebJarsAtMethod;

    @Mock
    private NinjaProperties ninjaProperties;

    @Mock
    private Context context;

    @Mock
    private Result result;

    @Mock
    private ResponseStreams responseStreams;

    @Mock
    private Writer writer;

    private TemplateEngineFreemarker templateEngineFreemarker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(ninjaProperties.isDev()).thenReturn(false);
        when(ninjaProperties.getWithDefault(TemplateEngineFreemarker.FREEMARKER_CONFIGURATION_FILE_SUFFIX, ".ftl.html"))
                .thenReturn(".ftl.html");
        when(context.finalizeHeaders(any(Result.class))).thenReturn(responseStreams);
        when(responseStreams.getWriter()).thenReturn(writer);
        templateEngineFreemarker = new TemplateEngineFreemarker(messages, lang, logger, templateEngineHelper,
                templateEngineManager, templateEngineFreemarkerReverseRouteMethod, templateEngineFreemarkerAssetsAtMethod,
                templateEngineFreemarkerWebJarsAtMethod, ninjaProperties);
    }

    @Test
    public void testGetContentType() {
        assertEquals("text/html", templateEngineFreemarker.getContentType());
    }

    @Test
    public void testGetSuffixOfTemplatingEngine() {
        assertEquals(".ftl.html", templateEngineFreemarker.getSuffixOfTemplatingEngine());
    }

    @Test
    public void testGetConfiguration() {
        Configuration config = templateEngineFreemarker.getConfiguration();
        assertNotNull(config);
    }

    @Test
    public void testInvokeWithNullObject() throws Exception {
        when(result.getRenderable()).thenReturn(null);
        when(templateEngineHelper.getTemplateForResult(any(), any(), anyString())).thenReturn("test.ftl.html");

        templateEngineFreemarker.invoke(context, result);

        verify(context).finalizeHeaders(result);
        verify(responseStreams).getWriter();
    }

    @Test
    public void testInvokeWithMapObject() throws Exception {
        Map<String, Object> map = new HashMap<>();
        when(result.getRenderable()).thenReturn(map);
        when(templateEngineHelper.getTemplateForResult(any(), any(), anyString())).thenReturn("test.ftl.html");

        templateEngineFreemarker.invoke(context, result);

        verify(context).finalizeHeaders(result);
        verify(responseStreams).getWriter();
    }

    @Test
    public void testInvokeWithNonMapObject() throws Exception {
        Object obj = new Object();
        when(result.getRenderable()).thenReturn(obj);
        when(templateEngineHelper.getTemplateForResult(any(), any(), anyString())).thenReturn("test.ftl.html");

        templateEngineFreemarker.invoke(context, result);

        verify(context).finalizeHeaders(result);
        verify(responseStreams).getWriter();
    }

    @Test
    public void testThrowRenderingExceptionWithTemplateNotFoundException() {
        try {
            templateEngineFreemarker.throwRenderingException(context, result, new IOException(new freemarker.template.TemplateNotFoundException("test", null)), "test.ftl.html");
        } catch (ninja.exceptions.RenderingException e) {
            assertEquals("FreeMarker template not found", e.getRenderingType());
        }
    }

    @Test
    public void testThrowRenderingExceptionWithTemplateException() {
        try {
            templateEngineFreemarker.throwRenderingException(context, result, new freemarker.template.TemplateException("test", null), "test.ftl.html");
        } catch (ninja.exceptions.RenderingException e) {
            assertEquals("FreeMarker render exception", e.getRenderingType());
        }
    }

    @Test
    public void testThrowRenderingExceptionWithParseException() {
        try {
            templateEngineFreemarker.throwRenderingException(context, result, new freemarker.core.ParseException("test", null), "test.ftl.html");
        } catch (ninja.exceptions.RenderingException e) {
            assertEquals("FreeMarker parser exception", e.getRenderingType());
        }
    }

    @Test
    public void testThrowRenderingExceptionWithGenericException() {
        try {
            templateEngineFreemarker.throwRenderingException(context, result, new RuntimeException("test"), "test.ftl.html");
        } catch (ninja.exceptions.RenderingException e) {
            assertEquals("test.ftl.html", e.getTemplateSourcePath());
        }
    }
}
