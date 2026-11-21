package ch.jalu.configme.configurationdata;

import ch.jalu.configme.exception.ConfigMeException;
import ch.jalu.configme.properties.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class PropertyListBuilder_ArrowTest {

    @Mock
    private Property<String> property1;

    @Mock
    private Property<String> property2;

    @Mock
    private Property<String> property3;

    @Test
    public void shouldCreateEmptyListWhenNoPropertiesAdded() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();

        // when
        List<Property<?>> result = builder.create();

        // then
        assertThat(result, empty());
    }

    @Test
    public void shouldAddSingleProperty() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("test");

        // when
        builder.add(property1);
        List<Property<?>> result = builder.create();

        // then
        assertThat(result, contains(property1));
    }

    @Test
    public void shouldAddMultiplePropertiesInOrder() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("a");
        property2 = new MockProperty("b");
        property3 = new MockProperty("c");

        // when
        builder.add(property1);
        builder.add(property2);
        builder.add(property3);
        List<Property<?>> result = builder.create();

        // then
        assertThat(result, contains(property1, property2, property3));
    }

    @Test
    public void shouldGroupPropertiesByPath() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("database.host");
        property2 = new MockProperty("database.port");
        property3 = new MockProperty("security.enabled");

        // when
        builder.add(property1);
        builder.add(property2);
        builder.add(property3);
        List<Property<?>> result = builder.create();

        // then
        assertThat(result, contains(property1, property2, property3));
    }

    @Test
    public void shouldThrowExceptionWhenAddingDuplicateProperty() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("test");

        // when
        builder.add(property1);
        ConfigMeException ex = assertThrows(ConfigMeException.class, () -> builder.add(property1));

        // then
        assertEquals("Path at 'test' already exists", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenAddingConflictingProperty() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("database.host");
        property2 = new MockProperty("database");

        // when
        builder.add(property1);
        ConfigMeException ex = assertThrows(ConfigMeException.class, () -> builder.add(property2));

        // then
        assertEquals("Path at 'database' already exists", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionForMalformedPathWithTrailingDot() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("test.");

        // when
        ConfigMeException ex = assertThrows(ConfigMeException.class, () -> builder.add(property1));

        // then
        assertEquals("The path at 'test.' is malformed: dots may not be at the beginning or end "
            + "of a path, and dots may not appear multiple times successively.", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionForMalformedPathWithLeadingDot() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty(".test");

        // when
        ConfigMeException ex = assertThrows(ConfigMeException.class, () -> builder.add(property1));

        // then
        assertEquals("The path at '.test' is malformed: dots may not be at the beginning or end "
            + "of a path, and dots may not appear multiple times successively.", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionForMalformedPathWithDoubleDot() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("test..property");

        // when
        ConfigMeException ex = assertThrows(ConfigMeException.class, () -> builder.add(property1));

        // then
        assertEquals("The path at 'test..property' is malformed: dots may not be at the beginning or end "
            + "of a path, and dots may not appear multiple times successively.", ex.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenRootPathExistsWithOtherPaths() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("");
        property2 = new MockProperty("test");

        // when
        builder.add(property1);
        builder.add(property2);
        ConfigMeException ex = assertThrows(ConfigMeException.class, builder::create);

        // then
        assertEquals("A property at the root path (\"\") cannot be defined alongside "
            + "other properties as the paths would conflict", ex.getMessage());
    }

    @Test
    public void shouldReturnRootEntries() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();

        // when
        Object result = builder.getRootEntries();

        // then
        assertThat(result, org.hamcrest.Matchers.instanceOf(java.util.Map.class));
    }

    @Test
    public void shouldHandleNestedPropertiesCorrectly() {
        // given
        PropertyListBuilder builder = new PropertyListBuilder();
        property1 = new MockProperty("a.b.c");
        property2 = new MockProperty("a.b.d");
        property3 = new MockProperty("a.e");

        // when
        builder.add(property1);
        builder.add(property2);
        builder.add(property3);
        List<Property<?>> result = builder.create();

        // then
        assertThat(result, contains(property1, property2, property3));
    }

    private static class MockProperty implements Property<String> {
        private final String path;

        MockProperty(String path) {
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String defaultValue() {
            return null;
        }

        @Override
        public String getType() {
            return "String";
        }

        @Override
        public String toString() {
            return "MockProperty{" +
                "path='" + path + '\'' +
                '}';
        }
    }
}
