package ca.derekcormier.recipe.generator.filter;

import static org.junit.Assert.assertEquals;

import ca.derekcormier.recipe.cookbook.Cookbook;
import java.util.HashMap;
import java.util.Map;
import liqp.filters.Filter;
import org.junit.Test;

public class JavaParamFilter_ArrowTest {

  @Test
  public void applyFormatsParameterWithoutVarargFlag() {
    Filter typeFilter =
        new Filter() {
          @Override
          public Object apply(Object value, Object... params) {
            assertEquals("String", value);
            assertEquals(1, params.length);
            assertEquals(Boolean.FALSE, params[0]);
            return "String";
          }
        };
    Filter identifierFilter =
        new Filter() {
          @Override
          public Object apply(Object value, Object... params) {
            assertEquals("title", value);
            return "title";
          }
        };
    JavaParamFilter filter = new JavaParamFilter(null, typeFilter, identifierFilter);
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("name", "title");
    parameter.put("type", "String");

    assertEquals("String title", filter.apply(parameter));
  }

  @Test
  public void applyFormatsVarargParameterAsArray() {
    Filter typeFilter =
        new Filter() {
          @Override
          public Object apply(Object value, Object... params) {
            assertEquals("String", value);
            assertEquals(1, params.length);
            assertEquals(Boolean.TRUE, params[0]);
            return "String[]";
          }
        };
    Filter identifierFilter =
        new Filter() {
          @Override
          public Object apply(Object value, Object... params) {
            assertEquals("titles", value);
            return "titles";
          }
        };
    JavaParamFilter filter = new JavaParamFilter(null, typeFilter, identifierFilter);
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("name", "titles");
    parameter.put("type", "String");

    assertEquals("String[] titles", filter.apply(parameter, true));
  }
}
