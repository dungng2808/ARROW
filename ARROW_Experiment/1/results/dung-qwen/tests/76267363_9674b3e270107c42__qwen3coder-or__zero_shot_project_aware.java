package io.github.jhipster.service.filter;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Filter_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        Filter<String> filter = new Filter<>();
        assertThat(filter.getEquals()).isNull();
        assertThat(filter.getNotEquals()).isNull();
        assertThat(filter.getSpecified()).isNull();
        assertThat(filter.getIn()).isNull();
        assertThat(filter.getNotIn()).isNull();
    }

    @Test
    public void testCopyConstructor() {
        Filter<String> original = new Filter<>();
        original.setEquals("test");
        original.setNotEquals("notTest");
        original.setSpecified(true);
        List<String> inList = new ArrayList<>();
        inList.add("a");
        inList.add("b");
        original.setIn(inList);
        List<String> notInList = new ArrayList<>();
        notInList.add("c");
        notInList.add("d");
        original.setNotIn(notInList);

        Filter<String> copy = new Filter<>(original);

        assertThat(copy.getEquals()).isEqualTo("test");
        assertThat(copy.getNotEquals()).isEqualTo("notTest");
        assertThat(copy.getSpecified()).isTrue();
        assertThat(copy.getIn()).containsExactly("a", "b");
        assertThat(copy.getNotIn()).containsExactly("c", "d");

        // Ensure the lists are independent
        copy.getIn().add("new");
        assertThat(original.getIn()).hasSize(2);
        assertThat(copy.getIn()).hasSize(3);
    }

    @Test
    public void testCopyMethod() {
        Filter<String> original = new Filter<>();
        original.setEquals("test");

        Filter<String> copy = original.copy();

        assertThat(copy.getEquals()).isEqualTo("test");
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    public void testSetAndGetEquals() {
        Filter<String> filter = new Filter<>();
        filter.setEquals("value");
        assertThat(filter.getEquals()).isEqualTo("value");
    }

    @Test
    public void testSetAndGetNotEquals() {
        Filter<String> filter = new Filter<>();
        filter.setNotEquals("value");
        assertThat(filter.getNotEquals()).isEqualTo("value");
    }

    @Test
    public void testSetAndGetSpecified() {
        Filter<String> filter = new Filter<>();
        filter.setSpecified(true);
        assertThat(filter.getSpecified()).isTrue();
        filter.setSpecified(false);
        assertThat(filter.getSpecified()).isFalse();
    }

    @Test
    public void testSetAndGetIn() {
        Filter<String> filter = new Filter<>();
        List<String> inList = new ArrayList<>();
        inList.add("a");
        inList.add("b");
        filter.setIn(inList);
        assertThat(filter.getIn()).containsExactly("a", "b");
    }

    @Test
    public void testSetAndGetNotIn() {
        Filter<String> filter = new Filter<>();
        List<String> notInList = new ArrayList<>();
        notInList.add("a");
        notInList.add("b");
        filter.setNotIn(notInList);
        assertThat(filter.getNotIn()).containsExactly("a", "b");
    }

    @Test
    public void testEquals() {
        Filter<String> filter1 = new Filter<>();
        filter1.setEquals("test");
        filter1.setSpecified(true);

        Filter<String> filter2 = new Filter<>();
        filter2.setEquals("test");
        filter2.setSpecified(true);

        Filter<String> filter3 = new Filter<>();
        filter3.setEquals("different");
        filter3.setSpecified(true);

        assertThat(filter1).isEqualTo(filter2);
        assertThat(filter1).isNotEqualTo(filter3);
        assertThat(filter1).isNotEqualTo(null);
        assertThat(filter1).isNotEqualTo(new Object());
    }

    @Test
    public void testHashCode() {
        Filter<String> filter1 = new Filter<>();
        filter1.setEquals("test");
        filter1.setSpecified(true);

        Filter<String> filter2 = new Filter<>();
        filter2.setEquals("test");
        filter2.setSpecified(true);

        assertThat(filter1.hashCode()).isEqualTo(filter2.hashCode());
    }

    @Test
    public void testToString() {
        Filter<String> filter = new Filter<>();
        filter.setEquals("test");
        filter.setSpecified(true);
        filter.setIn(new ArrayList<>());

        String result = filter.toString();
        assertThat(result).contains("Filter [");
        assertThat(result).contains("equals=test");
        assertThat(result).contains("specified=true");
        assertThat(result).contains("in=[]");
    }

    @Test
    public void testGetFilterName() {
        Filter<String> filter = new Filter<>();
        assertThat(filter.getFilterName()).isEqualTo("Filter");
    }
}
