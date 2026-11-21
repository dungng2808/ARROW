package ca.derekcormier.recipe.generator.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.derekcormier.recipe.cookbook.Cookbook;
import java.util.HashMap;
import java.util.Map;
import liqp.filters.Filter;
import org.junit.Before;
import org.junit.Test;

public class JavaParamFilter_ArrowTest {

    private Cookbook cookbook;
    private Filter typeFilter;
    private Filter identifierFilter;
    private JavaParamFilter javaParamFilter;

    @Before
    public void setUp() {
        cookbook = mock(Cookbook.class);
        typeFilter = mock(Filter.class);
        identifierFilter = mock(Filter.class);
        javaParamFilter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);
    }

    @Test
    public void testApply_NormalCase() {
        Map<String, Object> param = new HashMap<>();
        param.put("name", "myVariable");
        param.put("type", "String");

        when(typeFilter.apply("String", false)).thenReturn("java.lang.String");
        when(identifierFilter.apply("myVariable")).thenReturn("myVariable");

        Object result = javaParamFilter.apply(param);

        assertEquals("java.lang.String myVariable", result);
    }

    @Test
    public void testApply_VarargAsArrayTrue() {
        Map<String, Object> param = new HashMap<>();
        param.put("name", "args");
        param.put("type", "String...");

        when(typeFilter.apply("String...", true)).thenReturn("String[]");
        when(identifierFilter.apply("args")).thenReturn("args");

        Object result = javaParamFilter.apply(param, true);

        assertEquals("String[] args", result);
    }

    @Test
    public void testApply_VarargAsArrayFalse() {
        Map<String, Object> param = new HashMap<>();
        param.put("name", "args");
        param.put("type", "String...");

        when(typeFilter.apply("String...", false)).thenReturn("String...");
        when(identifierFilter.apply("args")).thenReturn("args");

        Object result = javaParamFilter.apply(param, false);

        assertEquals("String... args", result);
    }
}
