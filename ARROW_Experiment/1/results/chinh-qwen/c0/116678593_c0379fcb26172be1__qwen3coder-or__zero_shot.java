package biz.netcentric.aem.applysystemenvinstallhook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class VariablesMerger_ArrowTest {

    private InstallHookLogger logger;
    private VariablesMerger variablesMerger;

    @Before
    public void setUp() {
        logger = mock(InstallHookLogger.class);
        variablesMerger = new VariablesMerger(logger);
    }

    @Test
    public void testGetEnvVarDeclarations() {
        String text = "Hello ${var1} and ${var2:default} world";
        List<EnvVarDeclaration> declarations = variablesMerger.getEnvVarDeclarations(text);
        
        assertEquals(2, declarations.size());
        assertEquals("var1", declarations.get(0).name);
        assertEquals(null, declarations.get(0).defaultVal);
        assertEquals("var2", declarations.get(1).name);
        assertEquals("default", declarations.get(1).defaultVal);
    }

    @Test
    public void testApplyEnvVarsWithSourceValue() {
        String text = "Hello ${var1} world";
        VariablesSource source = mock(VariablesSource.class);
        VariablesSource.NamedValue namedValue = mock(VariablesSource.NamedValue.class);
        String path = "/test/path.config";
        
        // Mock the behavior of VariablesSource
        java.util.List<VariablesSource.NamedValue> values = new ArrayList<>();
        values.add(namedValue);
        
        // Setup expectations
        java.util.Map<String, VariablesSource.NamedValue> map = new java.util.HashMap<>();
        map.put("var1", namedValue);
        java.util.function.Function<String, VariablesSource.NamedValue> func = map::get;
        
        // Mock the source to return a value
        java.util.Optional<VariablesSource.NamedValue> optionalValue = java.util.Optional.of(namedValue);
        java.util.function.Predicate<String> predicate = map::containsKey;
        
        // Direct approach - since we can't easily mock the complex behavior, we'll test the actual logic
        // We'll just verify that the method doesn't crash and logs appropriately
        
        // Since we can't easily mock the complex behavior, let's test with a simpler approach
        // Let's directly test the replacement logic with a mock that returns a value
        VariablesSource mockSource = new VariablesSource() {
            @Override
            public NamedValue get(String name) {
                if ("var1".equals(name)) {
                    return new VariablesSource.NamedValue() {
                        @Override
                        public Object getValue() {
                            return "replacement";
                        }
                        
                        @Override
                        public String getSourceName() {
                            return "testSource";
                        }
                    };
                }
                return null;
            }
        };
        
        String result = variablesMerger.applyEnvVars(text, mockSource, path);
        assertEquals("Hello replacement world", result);
        verify(logger, times(1)).log("/test/path: var1=\"replacement\" (replaced from testSource)");
    }

    @Test
    public void testApplyEnvVarsWithDefaultValue() {
        String text = "Hello ${var1:defaultVal} world";
        VariablesSource source = mock(VariablesSource.class);
        String path = "/test/path.config";
        
        VariablesSource mockSource = new VariablesSource() {
            @Override
            public NamedValue get(String name) {
                return null; // No value found
            }
        };
        
        String result = variablesMerger.applyEnvVars(text, mockSource, path);
        assertEquals("Hello defaultVal world", result);
        verify(logger, times(1)).log("/test/path: var1=\"defaultVal\" (default in package)");
    }

    @Test
    public void testApplyEnvVarsWithoutDefaultValue() {
        String text = "Hello ${var1} world";
        VariablesSource source = mock(VariablesSource.class);
        String path = "/test/path.config";
        
        VariablesSource mockSource = new VariablesSource() {
            @Override
            public NamedValue get(String name) {
                return null; // No value found
            }
        };
        
        String result = variablesMerger.applyEnvVars(text, mockSource, path);
        assertEquals("Hello ${var1} world", result);
        verify(logger, times(1)).log("/test/path: var1=\"${var1}\" (var not found (no default provided))");
    }

    @Test
    public void testApplyEnvVarsWithConfigExtension() {
        String text = "Hello ${var1} world";
        VariablesSource source = mock(VariablesSource.class);
        String path = "/test/path.config";
        
        VariablesSource mockSource = new VariablesSource() {
            @Override
            public NamedValue get(String name) {
                if ("var1".equals(name)) {
                    return new VariablesSource.NamedValue() {
                        @Override
                        public Object getValue() {
                            return "value=with=special\"chars";
                        }
                        
                        @Override
                        public String getSourceName() {
                            return "testSource";
                        }
                    };
                }
                return null;
            }
        };
        
        String result = variablesMerger.applyEnvVars(text, mockSource, path);
        assertEquals("Hello value\\=with\\=special\\\"chars world", result);
        verify(logger, times(1)).log("/test/path: var1=\"value\\=with\\=special\\\"chars\" (replaced from testSource)");
    }

    @Test
    public void testGetReplacementSummary() {
        // First, trigger some replacements to populate counts
        VariablesSource mockSource = new VariablesSource() {
            @Override
            public NamedValue get(String name) {
                if ("var1".equals(name)) {
                    return new VariablesSource.NamedValue() {
                        @Override
                        public Object getValue() {
                            return "value1";
                        }
                        
                        @Override
                        public String getSourceName() {
                            return "source1";
                        }
                    };
                }
                return null;
            }
        };
        
        String text = "Hello ${var1} world";
        variablesMerger.applyEnvVars(text, mockSource, "/test/path.config");
        
        String summary = variablesMerger.getReplacementSummary();
        assertNotNull(summary);
        // Should contain replacement count for source1, default count, and total
        // The exact format depends on internal implementation but should include relevant info
    }

    @Test
    public void testIncrementCount() {
        // Test incrementing various counts
        variablesMerger.getClass().getDeclaredMethod("incrementCount", String.class).invoke(variablesMerger, "testKey");
        variablesMerger.getClass().getDeclaredMethod("incrementCount", String.class).invoke(variablesMerger, "testKey");
        variablesMerger.getClass().getDeclaredMethod("incrementCount", String.class).invoke(variablesMerger, "default");
        variablesMerger.getClass().getDeclaredMethod("incrementCount", String.class).invoke(variablesMerger, "not found");
        
        // Verify that counts were incremented properly
        // This requires reflection access to internal state, which is not ideal
        // But we can at least verify that the method doesn't throw an exception
    }
}
