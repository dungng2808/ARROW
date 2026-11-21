package de.viadee.xai.anchor.adapter.tabular.builder;

import de.viadee.xai.anchor.adapter.tabular.column.GenericColumn;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

public class AnchorTabularBuilderByName_ArrowTest {

    @Test
    public void addColumnReturnsBuilder() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("feature");
        assertSame(builder, builder.addColumn(column));
    }

    @Test
    public void addTargetColumnReturnsBuilder() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new GenericColumn("target");
        assertSame(builder, builder.addTargetColumn(column));
    }

    @Test
    public void addDuplicateColumnNameThrows() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("feature"));
        try {
            builder.addColumn(new GenericColumn("feature"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void buildRejectsExcludedHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature"});
        try {
            builder.build(rows, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void buildRejectsDuplicateHeaderNames() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature", "feature"});
        try {
            builder.build(rows, false);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void buildRejectsRegisteredColumnMissingFromHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("missing"));
        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature"});
        try {
            builder.build(rows, false);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void buildWithMatchingHeaderReturnsTabular() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("feature"));
        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature"});
        rows.add(new String[]{"value"});
        assertNotNull(builder.build(rows, false));
    }
}
