package de.viadee.xai.anchor.adapter.tabular.builder;

import de.viadee.xai.anchor.adapter.tabular.AnchorTabular;
import de.viadee.xai.anchor.adapter.tabular.column.GenericColumn;
import de.viadee.xai.anchor.adapter.tabular.column.IgnoredColumn;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AnchorTabularBuilderByName_ArrowTest {

    @Test
    public void testAddColumnValid() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("testColumn");
        
        // Register headers first to avoid NPE
        builder.registerRemovedRow(new String[]{"testColumn"});
        
        AnchorTabularBuilderByName result = builder.addColumn(column);
        
        assertSame(builder, result);
        assertTrue(builder.getColumnDescriptions(1).contains(column));
    }

    @Test
    public void testAddColumnNullName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.addColumn(column));
        assertEquals("A column name needs to be present", exception.getMessage());
    }

    @Test
    public void testAddColumnEmptyName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.addColumn(column));
        assertEquals("A column name needs to be present", exception.getMessage());
    }

    @Test
    public void testAddColumnDuplicateName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column1 = new GenericColumn("test");
        GenericColumn column2 = new GenericColumn("test");
        builder.addColumn(column1);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.addColumn(column2));
        assertEquals("Another column has already been registered containing the same name", exception.getMessage());
    }

    @Test
    public void testAddTargetColumn() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("target");
        
        // Register headers first to avoid NPE
        builder.registerRemovedRow(new String[]{"target"});
        
        AnchorTabularBuilderByName result = builder.addTargetColumn(column);
        
        assertSame(builder, result);
        assertTrue(builder.getColumnDescriptions(1).contains(column));
    }

    @Test
    public void testGetColumnDescriptionsWithHeaders() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1");
        GenericColumn col2 = new GenericColumn("col2");
        builder.addColumn(col1);
        builder.addColumn(col2);
        
        // Simulate registering headers
        String[] headers = {"col1", "col2"};
        builder.registerRemovedRow(headers);
        
        Collection<GenericColumn> result = builder.getColumnDescriptions(2);
        
        assertEquals(2, result.size());
        assertTrue(result.contains(col1));
        assertTrue(result.contains(col2));
    }

    @Test
    public void testGetColumnDescriptionsMissingHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1");
        builder.addColumn(col1);
        
        // Headers don't contain col1
        String[] headers = {"col2"};
        builder.registerRemovedRow(headers);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.getColumnDescriptions(1));
        assertEquals("Not all specified column names were found in the header list", exception.getMessage());
    }

    @Test
    public void testBuildWithExcludeFirst() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1");
        builder.addColumn(col1);
        
        String[] headers = {"col1"};
        builder.registerRemovedRow(headers);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.build(Collections.emptyList(), true));
        assertEquals("A header must be included when adding columns by name", exception.getMessage());
    }

    @Test
    public void testRegisterRemovedRowWithDuplicates() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        String[] row = {"col1", "col1"};
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> builder.registerRemovedRow(row));
        assertEquals("There must be no duplicate variable names", exception.getMessage());
    }

    @Test
    public void testRegisterRemovedRowWithoutDuplicates() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        String[] row = {"col1", "col2"};
        
        builder.registerRemovedRow(row);
        
        // Accessing headers via reflection or other means is not allowed per instructions
        // So we verify indirectly through the getColumnDescriptions method
        // Since headers are set, and we have no columns, we expect to get two IgnoredColumns
        Collection<GenericColumn> result = builder.getColumnDescriptions(2);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetColumnDescriptionsOrdering() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1");
        GenericColumn col2 = new GenericColumn("col2");
        GenericColumn col3 = new GenericColumn("col3");
        builder.addColumn(col1);
        builder.addColumn(col3);
        builder.addColumn(col2);
        
        // Headers define the order
        String[] headers = {"col3", "col1", "col2"};
        builder.registerRemovedRow(headers);
        
        Collection<GenericColumn> result = builder.getColumnDescriptions(3);
        
        assertEquals(3, result.size());
        List<GenericColumn> resultList = Arrays.asList(result.toArray(new GenericColumn[0]));
        
        // Check that the order matches the headers
        assertEquals(col3, resultList.get(0));
        assertEquals(col1, resultList.get(1));
        assertEquals(col2, resultList.get(2));
    }

    @Test
    public void testGetColumnDescriptionsWithIgnoredColumns() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn col1 = new GenericColumn("col1");
        builder.addColumn(col1);
        
        // Headers include both col1 and col2
        String[] headers = {"col1", "col2"};
        builder.registerRemovedRow(headers);
        
        Collection<GenericColumn> result = builder.getColumnDescriptions(2);
        
        assertEquals(2, result.size());
        assertTrue(result.contains(col1));
        
        // Check that col2 is represented as an IgnoredColumn
        GenericColumn col2Result = null;
        for (GenericColumn col : result) {
            if ("col2".equals(col.getName())) {
                col2Result = col;
                break;
            }
        }
        assertNotNull(col2Result);
        assertTrue(col2Result instanceof IgnoredColumn);
    }
}
