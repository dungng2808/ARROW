package de.viadee.xai.anchor.adapter.tabular.builder;

import de.viadee.xai.anchor.adapter.tabular.AnchorTabular;
import de.viadee.xai.anchor.adapter.tabular.column.GenericColumn;
import de.viadee.xai.anchor.adapter.tabular.column.IgnoredColumn;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AnchorTabularBuilderByName_ArrowTest {

    @Test
    public void testAddColumnWithNullName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("test") {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public String[] getUniqueValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 0;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addColumn(column);
        });
        assertEquals("A column name needs to be present", exception.getMessage());
    }

    @Test
    public void testAddColumnWithEmptyName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("test") {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public String[] getUniqueValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 0;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addColumn(column);
        });
        assertEquals("A column name needs to be present", exception.getMessage());
    }

    @Test
    public void testAddColumnWithDuplicateName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column1 = new GenericColumn("col1") {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public String[] getUniqueValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 0;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        GenericColumn column2 = new GenericColumn("col2") {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public String[] getUniqueValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 0;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(column1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.addColumn(column2);
        });
        assertEquals("Another column has already been registered containing the same name", exception.getMessage());
    }

    @Test
    public void testAddTargetColumn() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("target") {
            @Override
            public String getName() {
                return "target";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public String[] getUniqueValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 0;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        AnchorTabularBuilderByName result = builder.addTargetColumn(column);
        assertSame(builder, result);
    }

    @Test
    public void testGetColumnDescriptions() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1") {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[]{"val1", "val2"};
            }

            @Override
            public String[] getUniqueValues() {
                return new String[]{"val1", "val2"};
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 2;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        GenericColumn col2 = new GenericColumn("col2") {
            @Override
            public String getName() {
                return "col2";
            }

            @Override
            public String[] getValues() {
                return new String[]{"val3", "val4"};
            }

            @Override
            public String[] getUniqueValues() {
                return new String[]{"val3", "val4"};
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 2;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(col1);
        builder.addColumn(col2);

        // Simulate registering headers
        builder.registerRemovedRow(new String[]{"col1", "col2"});

        Collection<GenericColumn> result = builder.getColumnDescriptions(2);
        assertNotNull(result);
        assertEquals(2, result.size());

        Iterator<GenericColumn> iterator = result.iterator();
        GenericColumn first = iterator.next();
        GenericColumn second = iterator.next();

        assertEquals("col1", first.getName());
        assertEquals("col2", second.getName());
    }

    @Test
    public void testGetColumnDescriptionsWithUnassignedHeaders() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1") {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public String[] getUniqueValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 0;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(col1);

        // Simulate registering headers
        builder.registerRemovedRow(new String[]{"col2"});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.getColumnDescriptions(1);
        });
        assertEquals("Not all specified column names were found in the header list", exception.getMessage());
    }

    @Test
    public void testBuildWithExcludeFirstTrue() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.build(Collections.emptyList(), true);
        });
        assertEquals("A header must be included when adding columns by name", exception.getMessage());
    }

    @Test
    public void testRegisterRemovedRowWithDuplicates() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            builder.registerRemovedRow(new String[]{"col1", "col1"});
        });
        assertEquals("There must be no duplicate variable names", exception.getMessage());
    }

    @Test
    public void testRegisterRemovedRowSuccess() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        String[] headers = {"col1", "col2"};
        builder.registerRemovedRow(headers);
        assertArrayEquals(headers, builder.headers);
    }

    @Test
    public void testBuildWithValidData() throws Exception {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1") {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[]{"val1", "val2"};
            }

            @Override
            public String[] getUniqueValues() {
                return new String[]{"val1", "val2"};
            }

            @Override
            public int getNumberOfUniqueValues() {
                return 2;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(col1);
        builder.registerRemovedRow(new String[]{"col1"});

        Collection<String[]> data = Arrays.asList(
                new String[]{"val1"},
                new String[]{"val2"}
        );

        AnchorTabular result = builder.build(data, false);
        assertNotNull(result);
    }
}
