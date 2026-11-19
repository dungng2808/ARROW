package ch.jalu.configme.configurationdata;

import ch.jalu.configme.exception.ConfigMeException;
import ch.jalu.configme.properties.Property;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyListBuilder_ArrowTest {

    @Test
    public void shouldAddPropertyAndCreateList() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property1 = mock(Property.class);
        when(property1.getPath()).thenReturn("database.host");
        Property<String> property2 = mock(Property.class);
        when(property2.getPath()).thenReturn("database.port");

        // When
        builder.add(property1);
        builder.add(property2);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result.size(), is(2));
        assertThat(result.get(0), is(property1));
        assertThat(result.get(1), is(property2));
    }

    @Test
    public void shouldGroupPropertiesByPath() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property1 = mock(Property.class);
        when(property1.getPath()).thenReturn("database.host");
        Property<String> property2 = mock(Property.class);
        when(property2.getPath()).thenReturn("database.port");
        Property<String> property3 = mock(Property.class);
        when(property3.getPath()).thenReturn("security.enabled");

        // When
        builder.add(property1);
        builder.add(property2);
        builder.add(property3);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result.size(), is(3));
        assertThat(result.get(0), is(property1));
        assertThat(result.get(1), is(property2));
        assertThat(result.get(2), is(property3));
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenAddingDuplicateProperty() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property1 = mock(Property.class);
        when(property1.getPath()).thenReturn("database.host");
        Property<String> property2 = mock(Property.class);
        when(property2.getPath()).thenReturn("database.host");

        // When
        builder.add(property1);
        builder.add(property2);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionForMalformedPathWithEmptyLastElement() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("database.");

        // When
        builder.add(property);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionForMalformedPathWithEmptyFirstElement() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn(".database.host");

        // When
        builder.add(property);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionForMalformedPathWithConsecutiveDots() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("database..host");

        // When
        builder.add(property);
        builder.create();
    }

    @Test
    public void shouldHandleRootPropertyCorrectly() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("");

        // When
        builder.add(property);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(property));
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenRootPropertyExistsWithOtherProperties() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> rootProperty = mock(Property.class);
        when(rootProperty.getPath()).thenReturn("");
        Property<String> otherProperty = mock(Property.class);
        when(otherProperty.getPath()).thenReturn("database.host");

        // When
        builder.add(rootProperty);
        builder.add(otherProperty);
        builder.create();
    }

    @Test
    public void shouldHandleNestedPropertiesCorrectly() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property1 = mock(Property.class);
        when(property1.getPath()).thenReturn("database.mysql.host");
        Property<String> property2 = mock(Property.class);
        when(property2.getPath()).thenReturn("database.mysql.port");
        Property<String> property3 = mock(Property.class);
        when(property3.getPath()).thenReturn("database.postgresql.host");

        // When
        builder.add(property1);
        builder.add(property2);
        builder.add(property3);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result.size(), is(3));
        assertThat(result.get(0), is(property1));
        assertThat(result.get(1), is(property2));
        assertThat(result.get(2), is(property3));
    }
}
