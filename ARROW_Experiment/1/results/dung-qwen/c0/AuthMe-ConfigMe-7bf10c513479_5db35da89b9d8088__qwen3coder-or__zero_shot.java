package ch.jalu.configme.configurationdata;

import ch.jalu.configme.exception.ConfigMeException;
import ch.jalu.configme.properties.Property;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertyListBuilder_ArrowTest {

    @Test
    public void shouldCreateEmptyListWhenNoPropertiesAdded() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();

        // When
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result, empty());
    }

    @Test
    public void shouldAddSingleProperty() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("test.property");

        // When
        builder.add(property);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result, contains(property));
    }

    @Test
    public void shouldAddMultiplePropertiesInOrder() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property1 = mock(Property.class);
        when(property1.getPath()).thenReturn("a.property");
        Property<String> property2 = mock(Property.class);
        when(property2.getPath()).thenReturn("b.property");

        // When
        builder.add(property1);
        builder.add(property2);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result, contains(property1, property2));
    }

    @Test
    public void shouldGroupPropertiesByPath() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> dataSourceProp = mock(Property.class);
        when(dataSourceProp.getPath()).thenReturn("DataSource.host");
        Property<String> dataSourcePortProp = mock(Property.class);
        when(dataSourcePortProp.getPath()).thenReturn("DataSource.port");
        Property<String> securityProp = mock(Property.class);
        when(securityProp.getPath()).thenReturn("Security.enabled");

        // When
        builder.add(dataSourceProp);
        builder.add(securityProp);
        builder.add(dataSourcePortProp);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result, contains(dataSourceProp, dataSourcePortProp, securityProp));
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenAddingDuplicateProperty() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("test.property");

        // When
        builder.add(property);
        builder.add(property);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenAddingPropertyWithMalformedPath() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn(".test.property");

        // When
        builder.add(property);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenAddingPropertyWithMalformedPathEndingWithDot() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("test.property.");

        // When
        builder.add(property);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenAddingPropertyWithConsecutiveDots() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> property = mock(Property.class);
        when(property.getPath()).thenReturn("test..property");

        // When
        builder.add(property);
        builder.create();
    }

    @Test(expected = ConfigMeException.class)
    public void shouldThrowExceptionWhenAddingRootPropertyAlongsideOtherProperties() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> rootProperty = mock(Property.class);
        when(rootProperty.getPath()).thenReturn("");
        Property<String> otherProperty = mock(Property.class);
        when(otherProperty.getPath()).thenReturn("some.property");

        // When
        builder.add(rootProperty);
        builder.add(otherProperty);
        builder.create();
    }

    @Test
    public void shouldHandleNestedPropertiesCorrectly() {
        // Given
        PropertyListBuilder builder = new PropertyListBuilder();
        Property<String> nestedProp = mock(Property.class);
        when(nestedProp.getPath()).thenReturn("parent.child.grandchild");
        Property<String> parentProp = mock(Property.class);
        when(parentProp.getPath()).thenReturn("parent");

        // When
        builder.add(nestedProp);
        builder.add(parentProp);
        List<Property<?>> result = builder.create();

        // Then
        assertThat(result, contains(parentProp, nestedProp));
    }
}
