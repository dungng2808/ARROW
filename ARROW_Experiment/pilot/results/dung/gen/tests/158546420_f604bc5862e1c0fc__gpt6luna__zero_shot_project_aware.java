package de.viadee.xai.anchor.adapter.tabular.builder;

import de.viadee.xai.anchor.adapter.tabular.AnchorTabular;
import de.viadee.xai.anchor.adapter.tabular.column.GenericColumn;
import de.viadee.xai.anchor.adapter.tabular.column.IgnoredColumn;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class AnchorTabularBuilderByName_ArrowTest {

    @Test
    public void addColumnReturnsBuilder() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new IgnoredColumn("feature");

        assertSame(builder, builder.addColumn(column));
    }

    @Test
    public void addTargetColumnReturnsBuilder() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        GenericColumn column = new IgnoredColumn("target");

        assertSame(builder, builder.addTargetColumn(column));
    }

    @Test
    public void buildUsesHeaderOrderAndAddsIgnoredColumns() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new IgnoredColumn("second"));
        builder.addColumn(new IgnoredColumn("first"));

        Collection<String[]> rows = Arrays.asList(
                new String[]{"first", "second"},
                new String[]{"a", "b"}
        );

        AnchorTabular result = builder.build(rows, false);

        assertNotNull(result);
    }

    @Test
    public void buildRejectsExcludedHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();

        assertThrows(IllegalArgumentException.class,
                () -> builder.build(Collections.<String[]>emptyList(), true));
    }

    @Test
    public void buildRejectsDuplicateHeaderNames() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        Collection<String[]> rows = Collections.singletonList(new String[]{"same", "same"});

        assertThrows(IllegalArgumentException.class, () -> builder.build(rows, false));
    }

    @Test
    public void buildRejectsRegisteredColumnMissingFromHeader() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new IgnoredColumn("missing"));
        Collection<String[]> rows = Collections.singletonList(new String[]{"present"});

        assertThrows(IllegalArgumentException.class, () -> builder.build(rows, false));
    }

    @Test
    public void addColumnRejectsDuplicateNames() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();
        builder.addColumn(new IgnoredColumn("feature"));

        assertThrows(IllegalArgumentException.class,
                () -> builder.addColumn(new IgnoredColumn("feature")));
    }

    @Test
    public void addColumnRejectsEmptyName() {
        AnchorTabularBuilderByName builder = new AnchorTabularBuilderByName();

        assertThrows(IllegalArgumentException.class,
                () -> builder.addColumn(new IgnoredColumn("")));
    }
}
