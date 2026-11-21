package de.viadee.xai.anchor.adapter.tabular.builder;

import de.viadee.xai.anchor.adapter.tabular.AnchorTabular;
import de.viadee.xai.anchor.adapter.tabular.column.GenericColumn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class AnchorTabularBuilderByName_ArrowTest {

    @Test
    public void addColumnReturnsBuilder() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("feature");

        assertSame(builder, builder.addColumn(column));
    }

    @Test
    public void addColumnRejectsDuplicateName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("feature"));

        try {
            builder.addColumn(new GenericColumn("feature"));
            fail("Expected duplicate column name to be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    @Test
    public void addColumnRejectsEmptyName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();

        try {
            builder.addColumn(new GenericColumn(""));
            fail("Expected empty column name to be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    @Test
    public void buildUsesHeaderOrderAndIgnoredColumns() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn feature = new GenericColumn("feature");
        builder.addColumn(feature);

        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature", "other"});
        rows.add(new String[]{"value", "ignored"});

        AnchorTabular result = builder.build(rows, false);

        assertNotNull(result);
        assertEquals(1, rows.size());
    }

    @Test
    public void buildRejectsExcludedHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();

        try {
            builder.build(new ArrayList<String[]>(), true);
            fail("Expected excluded header to be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    @Test
    public void buildRejectsDuplicateHeaders() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature", "feature"});
        rows.add(new String[]{"value", "value"});

        try {
            builder.build(rows, false);
            fail("Expected duplicate headers to be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

    @Test
    public void buildRejectsRegisteredColumnMissingFromHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("missing"));

        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"present"});
        rows.add(new String[]{"value"});

        try {
            builder.build(rows, false);
            fail("Expected missing registered column to be rejected");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }
}
