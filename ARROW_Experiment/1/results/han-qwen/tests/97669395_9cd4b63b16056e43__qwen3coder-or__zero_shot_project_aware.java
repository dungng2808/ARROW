package ca.derekcormier.recipe.generator.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.derekcormier.recipe.cookbook.Cookbook;
import java.util.HashMap;
import java.util.Map;
import liqp.filters.Filter;
import org.junit.Test;

public class JavaParamFilter_ArrowTest {

  @Test
  public void testApply_NormalCase() {
    Cookbook cookbook = mock(Cookbook.class);
    Filter typeFilter = mock(Filter.class);
    Filter identifierFilter = mock(Filter.class);
    JavaParamFilter filter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);

    Map<String, Object> param = new HashMap<>();
    param.put("name", "myParam");
    param.put("type", "String");

    when(typeFilter.apply("String", false)).thenReturn("java.lang.String");
    when(identifierFilter.apply("myParam")).thenReturn("myParam");

    Object result = filter.apply(param);
    assertEquals("java.lang.String myParam", result);
  }

  @Test
  public void testApply_VarargAsArrayTrue() {
    Cookbook cookbook = mock(Cookbook.class);
    Filter typeFilter = mock(Filter.class);
    Filter identifierFilter = mock(Filter.class);
    JavaParamFilter filter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);

    Map<String, Object> param = new HashMap<>();
    param.put("name", "args");
    param.put("type", "String[]");

    when(typeFilter.apply("String[]", true)).thenReturn("String[]");
    when(identifierFilter.apply("args")).thenReturn("args");

    Object result = filter.apply(param, true);
    assertEquals("String[] args", result);
  }

  @Test
  public void testApply_VarargAsArrayFalse() {
    Cookbook cookbook = mock(Cookbook.class);
    Filter typeFilter = mock(Filter.class);
    Filter identifierFilter = mock(Filter.class);
    JavaParamFilter filter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);

    Map<String, Object> param = new HashMap<>();
    param.put("name", "args");
    param.put("type", "String[]");

    when(typeFilter.apply("String[]", false)).thenReturn("java.lang.String[]");
    when(identifierFilter.apply("args")).thenReturn("args");

    Object result = filter.apply(param, false);
    assertEquals("java.lang.String[] args", result);
  }

  @Test
  public void testApply_EmptyParams() {
    Cookbook cookbook = mock(Cookbook.class);
    Filter typeFilter = mock(Filter.class);
    Filter identifierFilter = mock(Filter.class);
    JavaParamFilter filter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);

    Map<String, Object> param = new HashMap<>();
    param.put("name", "param");
    param.put("type", "int");

    when(typeFilter.apply("int", false)).thenReturn("int");
    when(identifierFilter.apply("param")).thenReturn("param");

    Object result = filter.apply(param);
    assertEquals("int param", result);
  }
}
