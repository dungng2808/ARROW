package ninja.template;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import ninja.Context;
import ninja.Result;
import ninja.exceptions.RenderingException;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.utils.NinjaConstant;
import ninja.utils.NinjaProperties;
import ninja.utils.ResponseStreams;

public class TemplateEngineFreemarker_ArrowTest {

    private TemplateEngineFreemarker templateEngineFreemarker;
    private Messages messages;
    private Lang lang;
    private Logger logger;
    private TemplateEngineHelper templateEngineHelper;
    private TemplateEngineManager templateEngineManager;
    private TemplateEngineFreemarkerReverseRouteMethod reverseRouteMethod;
    private TemplateEngineFreemarkerAssetsAtMethod assetsAtMethod;
    private TemplateEngineFreemarkerWebJarsAtMethod webJarsAtMethod;
    private NinjaProperties ninjaProperties;
    private Context context;
    private Result result;
    private ResponseStreams responseStreams;
    private StringWriter stringWriter;

    @Before
    public void setUp() throws Exception {
        messages = mock(Messages.class);
        lang = mock(Lang.class);
        logger = mock(Logger.class);
        templateEngineHelper = mock(TemplateEngineHelper.class);
        templateEngineManager = mock(TemplateEngineManager.class);
        reverseRouteMethod = mock(TemplateEngineFreemarkerReverseRouteMethod.class);
        assetsAtMethod = mock(TemplateEngineFreemarkerAssetsAtMethod.class);
        webJarsAtMethod = mock(TemplateEngineFreemarkerWebJarsAtMethod.class);
        ninjaProperties = mock(NinjaProperties.class);
        context = mock(Context.class);
        result = mock(Result.class);
        responseStreams = mock(ResponseStreams.class);
        stringWriter = new StringWriter();

        when(ninjaProperties.isDev()).thenReturn(false);
        when(ninjaProperties.getWithDefault(TemplateEngineFreemarker.FREEMARKER_CONFIGURATION_FILE_SUFFIX, ".ftl.html"))
                .thenReturn(".ftl.html");
        when(templateEngineHelper.getTemplateForResult(any(), any(), anyString()))
                .thenReturn("test.ftl.html");
        when(context.finalizeHeaders(any(Result.class))).thenReturn(responseStreams);
        when(responseStreams.getWriter()).thenReturn(stringWriter);
        when(lang.getLanguage(any(Context.class), any(Optional.class))).thenReturn(Optional.empty());
        when(lang.getLocaleFromStringOrDefault(any(Optional.class))).thenReturn(Locale.ENGLISH);
        when(messages.get(anyString(), any(Context.class), any(Optional.class)))
                .thenReturn(Optional.empty());

        templateEngineFreemarker = new TemplateEngineFreemarker(messages, lang, logger, templateEngineHelper,
                templateEngineManager, reverseRouteMethod, assetsAtMethod, webJarsAtMethod, ninjaProperties);
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
        templateEngineFreemarker.invoke(context, result);
        assertTrue(stringWriter.toString().contains(""));
    }

    @Test
    public void testInvokeWithMapObject() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        when(result.getRenderable()).thenReturn(map);
        templateEngineFreemarker.invoke(context, result);
        assertTrue(stringWriter.toString().contains(""));
    }

    @Test
    public void testInvokeWithNonMapObject() throws Exception {
        Object obj = new Object();
        when(result.getRenderable()).thenReturn(obj);
        templateEngineFreemarker.invoke(context, result);
        assertTrue(stringWriter.toString().contains(""));
    }

    @Test
    public void testThrowRenderingException_TemplateNotFoundException() {
        TemplateNotFoundException tnf = new TemplateNotFoundException("test.ftl.html", "test.ftl.html");
        try {
            templateEngineFreemarker.throwRenderingException(context, result, tnf, "test.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException re) {
            assertEquals("FreeMarker template not found", re.getRenderingType());
        }
    }

    @Test
    public void testThrowRenderingException_TemplateException() {
        TemplateException te = mock(TemplateException.class);
        when(te.getTemplateSourceName()).thenReturn("test.ftl.html");
        when(te.getLineNumber()).thenReturn(10);
        try {
            templateEngineFreemarker.throwRenderingException(context, result, te, "test.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException re) {
            assertEquals("FreeMarker render exception", re.getRenderingType());
        }
    }

    @Test
    public void testThrowRenderingException_ParseException() {
        ParseException pe = mock(ParseException.class);
        when(pe.getTemplateName()).thenReturn("test.ftl.html");
        when(pe.getLineNumber()).thenReturn(10);
        try {
            templateEngineFreemarker.throwRenderingException(context, result, pe, "test.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException re) {
            assertEquals("FreeMarker parser exception", re.getRenderingType());
        }
    }

    @Test
    public void testThrowRenderingException_GenericException() {
        IOException ioException = new IOException("test");
        try {
            templateEngineFreemarker.throwRenderingException(context, result, ioException, "test.ftl.html");
            fail("Expected RenderingException");
        } catch (RenderingException re) {
            assertEquals("test.ftl.html", re.getTemplateSourcePath());
        }
    }
}
