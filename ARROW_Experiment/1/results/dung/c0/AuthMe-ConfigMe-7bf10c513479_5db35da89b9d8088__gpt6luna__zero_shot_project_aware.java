package ch.jalu.configme.configurationdata;

import ch.jalu.configme.exception.ConfigMeException;
import ch.jalu.configme.properties.Property;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PropertyListBuilder_ArrowTest {

    @Test
    public void createReturnsEmptyListWhenNoPropertiesWereAdded() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertEquals(0, builder.create().size());
    }

    @Test
    public void addAndCreatePreserveInsertionOrderAndGroupByPath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<?> first = Property.of("DataSource.mysql.host", "localhost");
        Property<?> second = Property.of("security.enabled", true);
        Property<?> third = Property.of("DataSource.mysql.port", 3306);

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
    public void addRejectsDuplicatePath() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(Property.of("app.name", "first"));

        assertThrows(ConfigMeException.class, () -> builder.add(Property.of("app.name", "second")));
    }

    @Test
    public void addRejectsPropertyWhereParentPathIsAlreadyAProperty() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(Property.of("app", "value"));

        assertThrows(ConfigMeException.class, () -> builder.add(Property.of("app.name", "name")));
    }

    @Test
    public void addRejectsPropertyThatWouldReplaceExistingChildren() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(Property.of("app.name", "name"));

        assertThrows(ConfigMeException.class, () -> builder.add(Property.of("app", "value")));
    }

    @Test
    public void addRejectsPathWithLeadingDot() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertThrows(ConfigMeException.class, () -> builder.add(Property.of(".app", "value")));
    }

    @Test
    public void addRejectsPathWithTrailingDot() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertThrows(ConfigMeException.class, () -> builder.add(Property.of("app.", "value")));
    }

    @Test
    public void addRejectsPathWithConsecutiveDots() {
        PropertyListBuilder builder = new PropertyListBuilder();

        assertThrows(ConfigMeException.class, () -> builder.add(Property.of("app..name", "value")));
    }

    @Test
    public void createRejectsRootPathAlongsideOtherProperties() {
        PropertyListBuilder builder = new PropertyListBuilder();
        builder.add(Property.of("", "root"));
        builder.add(Property.of("app.name", "name"));

        assertThrows(ConfigMeException.class, builder::create);
    }

    @Test
    public void createAllowsRootPathByItself() {
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<?> root = Property.of("", "root");
        builder.add(root);

        assertEquals(1, builder.create().size());
        assertEquals(root, builder.create().get(0));
    }
}
