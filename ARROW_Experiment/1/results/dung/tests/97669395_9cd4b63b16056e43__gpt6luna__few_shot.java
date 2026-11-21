package ca.derekcormier.recipe.generator.filter;

import ca.derekcormier.recipe.cookbook.Cookbook;
import java.util.HashMap;
import java.util.Map;
import liqp.filters.Filter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaParamFilter_ArrowTest {

  @Test
  public void formatsParameterUsingBothFilters() {
    Filter typeFilter = new Filter() {
      @Override
      public Object apply(Object value, Object... params) {
        return value + (params.length > 0 && Boolean.TRUE.equals(params[0]) ? "[]" : "");
      }
    };
    Filter identifierFilter = new Filter() {
      @Override
      public Object apply(Object value, Object... params) {
        return "id_" + value;
      }
    };

    JavaParamFilter filter = new JavaParamFilter(null, typeFilter, identifierFilter);
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("name", "count");
    parameter.put("type", "int");

    assertEquals("int id_count", filter.apply(parameter));
  }

  @Test
  public void passesVarargAsArrayToTypeFilter() {
    Filter typeFilter = new Filter() {
      @Override
      public Object apply(Object value, Object... params) {
        return value + (params.length > 0 && Boolean.TRUE.equals(params[0]) ? "[]" : "");
      }
    };
    Filter identifierFilter = new Filter() {
      @Override
      public Object apply(Object value, Object... params) {
        return value;
      }
    };

    JavaParamFilter filter = new JavaParamFilter(null, typeFilter, identifierFilter);
    Map<String, Object> parameter = new HashMap<String, Object>();
    parameter.put("name", "items");
    parameter.put("type", "String");

    assertEquals("String[] items", filter.apply(parameter, true));
  }
}
