package de.viadee.xai.anchor.adapter.tabular.builder;

import de.viadee.xai.anchor.adapter.tabular.AnchorTabular;
import de.viadee.xai.anchor.adapter.tabular.column.GenericColumn;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertThrows(IllegalArgumentException.class,
                () -> builder.addColumn(new GenericColumn("feature")));
    }

    @Test
    public void addColumnRejectsEmptyName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();

        assertThrows(IllegalArgumentException.class,
                () -> builder.addColumn(new GenericColumn("")));
    }

    @Test
    public void buildUsesHeaderOrderAndIgnoredColumns() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("feature"));

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

        assertThrows(IllegalArgumentException.class,
                () -> builder.build(new ArrayList<String[]>(), true));
    }

    @Test
    public void buildRejectsDuplicateHeaders() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"feature", "feature"});
        rows.add(new String[]{"value", "value"});

        assertThrows(IllegalArgumentException.class,
                () -> builder.build(rows, false));
    }

    @Test
    public void buildRejectsRegisteredColumnMissingFromHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new GenericColumn("missing"));

        Collection<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"present"});
        rows.add(new String[]{"value"});

        assertThrows(IllegalArgumentException.class,
                () -> builder.build(rows, false));
    }
}
