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
        GenericColumn column = new GenericColumn() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfValues() {
                return 0;
            }

            @Override
            public String getValue(int index) {
                return null;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> builder.addColumn(column));
    }

    @Test
    public void testAddColumnWithEmptyName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfValues() {
                return 0;
            }

            @Override
            public String getValue(int index) {
                return null;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> builder.addColumn(column));
    }

    @Test
    public void testAddColumnWithDuplicateName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column1 = new GenericColumn() {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfValues() {
                return 0;
            }

            @Override
            public String getValue(int index) {
                return null;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        GenericColumn column2 = new GenericColumn() {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfValues() {
                return 0;
            }

            @Override
            public String getValue(int index) {
                return null;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(column1);
        assertThrows(IllegalArgumentException.class, () -> builder.addColumn(column2));
    }

    @Test
    public void testAddTargetColumn() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn() {
            @Override
            public String getName() {
                return "target";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfValues() {
                return 0;
            }

            @Override
            public String getValue(int index) {
                return null;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        AnchorTabularBuilderByName result = builder.addTargetColumn(column);
        assertEquals(builder, result);
    }

    @Test
    public void testGetColumnDescriptions() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn() {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[]{"val1", "val2"};
            }

            @Override
            public int getNumberOfValues() {
                return 2;
            }

            @Override
            public String getValue(int index) {
                return getValues()[index];
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        GenericColumn col2 = new GenericColumn() {
            @Override
            public String getName() {
                return "col2";
            }

            @Override
            public String[] getValues() {
                return new String[]{"val3", "val4"};
            }

            @Override
            public int getNumberOfValues() {
                return 2;
            }

            @Override
            public String getValue(int index) {
                return getValues()[index];
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(col1);
        builder.addColumn(col2);

        // Simulate header registration
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
        GenericColumn col1 = new GenericColumn() {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[0];
            }

            @Override
            public int getNumberOfValues() {
                return 0;
            }

            @Override
            public String getValue(int index) {
                return null;
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(col1);

        // Simulate header registration with missing column
        builder.registerRemovedRow(new String[]{"col2"});

        assertThrows(IllegalArgumentException.class, () -> builder.getColumnDescriptions(1));
    }

    @Test
    public void testBuildWithExcludeFirstTrue() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        assertThrows(IllegalArgumentException.class, () -> builder.build(Collections.emptyList(), true));
    }

    @Test
    public void testBuildSuccess() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn() {
            @Override
            public String getName() {
                return "col1";
            }

            @Override
            public String[] getValues() {
                return new String[]{"val1", "val2"};
            }

            @Override
            public int getNumberOfValues() {
                return 2;
            }

            @Override
            public String getValue(int index) {
                return getValues()[index];
            }

            @Override
            public boolean isCategorical() {
                return false;
            }
        };

        builder.addColumn(col1);
        builder.registerRemovedRow(new String[]{"col1"});

        Collection<String[]> data = Arrays.asList(new String[]{"val1"}, new String[]{"val2"});
        AnchorTabular result = builder.build(data, false);
        assertNotNull(result);
    }

    @Test
    public void testRegisterRemovedRowWithDuplicates() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        assertThrows(IllegalArgumentException.class, () -> builder.registerRemovedRow(new String[]{"col1", "col1"}));
    }

    @Test
    public void testRegisterRemovedRowSuccess() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        String[] headers = {"col1", "col2"};
        builder.registerRemovedRow(headers);
        assertArrayEquals(headers, builder.headers);
    }
}
