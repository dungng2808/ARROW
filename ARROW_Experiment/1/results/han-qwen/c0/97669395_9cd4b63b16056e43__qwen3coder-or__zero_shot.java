package ca.derekcormier.recipe.generator.filter;

import ca.derekcormier.recipe.cookbook.Cookbook;
import java.util.HashMap;
import java.util.Map;
import liqp.filters.Filter;
import org.junit.Test;
import static org.junit.Assert.*;

public class JavaParamFilter_ArrowTest {

    @Test
    public void testApplyWithVarargAsArray() {
        Cookbook cookbook = new Cookbook();
        Filter typeFilter = new Filter() {
            @Override
            public Object apply(Object value, Object... params) {
                return "String";
            }
        };
        Filter identifierFilter = new Filter() {
            @Override
            public Object apply(Object value, Object... params) {
                return "myParam";
            }
        };

        JavaParamFilter filter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);

        Map<String, Object> param = new HashMap<>();
        param.put("name", "myParam");
        param.put("type", "java.lang.String");

        Object result = filter.apply(param, true);
        assertEquals("String myParam", result);
    }

    @Test
    public void testApplyWithoutVarargAsArray() {
        Cookbook cookbook = new Cookbook();
        Filter typeFilter = new Filter() {
            @Override
            public Object apply(Object value, Object... params) {
                return "int";
            }
        };
        Filter identifierFilter = new Filter() {
            @Override
            public Object apply(Object value, Object... params) {
                return "count";
            }
        };

        JavaParamFilter filter = new JavaParamFilter(cookbook, typeFilter, identifierFilter);

        Map<String, Object> param = new HashMap<>();
        param.put("name", "count");
        param.put("type", "int");

        Object result = filter.apply(param);
        assertEquals("int count", result);
    }
}
