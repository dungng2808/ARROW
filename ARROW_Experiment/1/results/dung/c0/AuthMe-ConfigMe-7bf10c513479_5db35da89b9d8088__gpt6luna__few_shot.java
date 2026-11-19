package ch.jalu.configme.configurationdata;

import ch.jalu.configme.exception.ConfigMeException;
import ch.jalu.configme.properties.Property;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PropertyListBuilder_ArrowTest {

    @Test
    public void createReturnsPropertiesInInsertionOrderGroupedByPath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<?> first = new Property<String>("group.first", "one");
        Property<?> second = new Property<String>("other", "two");
        Property<?> third = new Property<String>("group.nested.third", "three");

        builder.add(first);
        builder.add(second);
        builder.add(third);

        List<Property<?>> result = builder.create();

        assertEquals(3, result.size());
        assertEquals(first, result.get(0));
        assertEquals(third, result.get(1));
        assertEquals(second, result.get(2));
    }

    @Test
    public void createReturnsEmptyListWhenNoPropertiesWereAdded() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertEquals(0, builder.create().size());
    }

    @Test
    public void addRejectsDuplicatePath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<String>("group.value", "one"));

        assertThrows(ConfigMeException.class,
            () -> builder.add(new Property<String>("group.value", "two")));
    }

    @Test
    public void addRejectsAddingChildWherePropertyAlreadyExists() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<String>("group", "one"));

        assertThrows(ConfigMeException.class,
            () -> builder.add(new Property<String>("group.child", "two")));
    }

    @Test
    public void addRejectsMalformedPathWithLeadingDot() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertThrows(ConfigMeException.class,
            () -> builder.add(new Property<String>(".value", "one")));
    }

    @Test
    public void addRejectsMalformedPathWithTrailingDot() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertThrows(ConfigMeException.class,
            () -> builder.add(new Property<String>("group.", "one")));
    }

    @Test
    public void addRejectsMalformedPathWithConsecutiveDots() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertThrows(ConfigMeException.class,
            () -> builder.add(new Property<String>("group..value", "one")));
    }

    @Test
    public void createRejectsRootPropertyAlongsideOtherProperties() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<String>("", "root"));
        builder.add(new Property<String>("other", "value"));

        assertThrows(ConfigMeException.class, builder::create);
    }

    @Test
    public void createAllowsRootPropertyByItself() {
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<?> root = new Property<String>("", "root");
        builder.add(root);

        assertEquals(1, builder.create().size());
        assertEquals(root, builder.create().get(0));
    }
}
