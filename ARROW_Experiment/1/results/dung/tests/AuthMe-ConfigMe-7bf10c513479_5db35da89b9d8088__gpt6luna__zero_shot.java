package ch.jalu.configme.configurationdata;

import ch.jalu.configme.properties.Property;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyListBuilder_ArrowTest {

    @Test
    public void createReturnsEmptyListWhenNoPropertiesWereAdded() {
        PropertyListBuilder builder = new PropertyListBuilder();

        List<Property<?>> result = builder.create();

        assertTrue(result.isEmpty());
    }

    @Test
    public void createReturnsPropertiesInInsertionOrderGroupedByPath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<?> first = new Property<Object>("group.first") { };
        Property<?> second = new Property<Object>("other") { };
        Property<?> third = new Property<Object>("group.nested.third") { };

        builder.add(first);
        builder.add(second);
        builder.add(third);

        List<Property<?>> result = builder.create();

        assertEquals(3, result.size());
        assertEquals(first, result.get(0));
        assertEquals(third, result.get(1));
        assertEquals(second, result.get(2));
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void addRejectsDuplicatePath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>("same") { });
        builder.add(new Property<Object>("same") { });
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void addRejectsPropertyAtPathAlreadyUsedAsParent() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>("group.child") { });
        builder.add(new Property<Object>("group") { });
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void addRejectsPropertyBelowExistingProperty() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>("group") { });
        builder.add(new Property<Object>("group.child") { });
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void addRejectsPathWithLeadingDot() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>(".leading") { });
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void addRejectsPathWithConsecutiveDots() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>("group..child") { });
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void addRejectsPathWithTrailingDot() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>("trailing.") { });
    }

    @Test
    public void createReturnsSinglePropertyAtRootPath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<?> root = new Property<Object>("") { };

        builder.add(root);

        List<Property<?>> result = builder.create();

        assertEquals(1, result.size());
        assertEquals(root, result.get(0));
    }

    @Test(expected = ch.jalu.configme.exception.ConfigMeException.class)
    public void createRejectsRootPathAlongsideOtherProperties() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(new Property<Object>("") { });
        builder.add(new Property<Object>("other") { });

        builder.create();
    }
}
