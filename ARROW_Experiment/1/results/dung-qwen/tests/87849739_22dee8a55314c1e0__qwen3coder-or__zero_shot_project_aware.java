package org.eclipse.ditto.model.things;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

public class MutablePermissions_ArrowTest {

    @Test
    public void testNoneReturnsEmptySet() {
        final Permissions permissions = MutablePermissions.none();
        assertThat(permissions).isNotNull();
        assertThat(permissions.size()).isEqualTo(0);
    }

    @Test
    public void testAllReturnsAllPermissions() {
        final Permissions permissions = MutablePermissions.all();
        assertThat(permissions).isNotNull();
        assertThat(permissions.size()).isEqualTo(Permission.values().length);
    }

    @Test
    public void testOfCreatesSetWithGivenPermissions() {
        final Permission perm1 = Permission.READ;
        final Permission perm2 = Permission.WRITE;
        final Permissions permissions = MutablePermissions.of(perm1, perm2);
        assertThat(permissions).isNotNull();
        assertThat(permissions.size()).isEqualTo(2);
        assertThat(permissions.contains(perm1)).isTrue();
        assertThat(permissions.contains(perm2)).isTrue();
    }

    @Test
    public void testOfWithNullPermissionThrowsNullPointerException() {
        assertThatThrownBy(() -> MutablePermissions.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("permission");
    }

    @Test
    public void testContainsWithNullPermissionThrowsNullPointerException() {
        final Permissions permissions = MutablePermissions.none();
        assertThatThrownBy(() -> permissions.contains(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("permission");
    }

    @Test
    public void testContainsWithNullFurtherPermissionsThrowsNullPointerException() {
        final Permissions permissions = MutablePermissions.none();
        assertThatThrownBy(() -> permissions.contains(Permission.READ, (Permission[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("further permissions");
    }

    @Test
    public void testContainsReturnsTrueWhenAllPermissionsArePresent() {
        final Permissions permissions = MutablePermissions.all();
        assertThat(permissions.contains(Permission.READ, Permission.WRITE)).isTrue();
    }

    @Test
    public void testContainsReturnsFalseWhenSomePermissionsAreMissing() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertThat(permissions.contains(Permission.READ, Permission.WRITE)).isFalse();
    }

    @Test
    public void testAddAddsPermissionAndReturnsTrueIfNew() {
        final Permissions permissions = MutablePermissions.none();
        final boolean result = permissions.add(Permission.READ);
        assertThat(result).isTrue();
        assertThat(permissions.size()).isEqualTo(1);
        assertThat(permissions.contains(Permission.READ)).isTrue();
    }

    @Test
    public void testAddDoesNotAddDuplicateAndReturnsFalse() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final boolean result = permissions.add(Permission.READ);
        assertThat(result).isFalse();
        assertThat(permissions.size()).isEqualTo(1);
    }

    @Test
    public void testAddWithNullThrowsNullPointerException() {
        final Permissions permissions = MutablePermissions.none();
        assertThatThrownBy(() -> permissions.add(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("permission to be added");
    }

    @Test
    public void testIteratorReturnsCorrectElements() {
        final Set<Permission> expected = new HashSet<>();
        expected.add(Permission.READ);
        expected.add(Permission.WRITE);
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);

        final Iterator<Permission> iterator = permissions.iterator();
        final Set<Permission> actual = new HashSet<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testSizeReturnsCorrectCount() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        assertThat(permissions.size()).isEqualTo(2);
    }

    @Test
    public void testSizeOfNoneReturnsZero() {
        final Permissions permissions = MutablePermissions.none();
        assertThat(permissions.size()).isEqualTo(0);
    }

    @Test
    public void testToJsonWithSchemaVersionAndPredicate() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final JsonObject jsonObject = permissions.toJson(JsonSchemaVersion.V_1, field -> true);
        assertThat(jsonObject).isNotNull();
        assertThat(jsonObject.getValue(Permission.READ.toJsonKey())).isPresent();
        assertThat(jsonObject.getValue(Permission.READ.toJsonKey()).get().asBoolean()).isTrue();
        assertThat(jsonObject.getValue(Permission.WRITE.toJsonKey())).isPresent();
        assertThat(jsonObject.getValue(Permission.WRITE.toJsonKey()).get().asBoolean()).isFalse();
    }

    @Test
    public void testToJsonWithPointer() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final JsonObject jsonObject = permissions.toJson(Permission.READ.toJsonKey().toPointer());
        assertThat(jsonObject).isNotNull();
        assertThat(jsonObject.getValue(Permission.READ.toJsonKey())).isPresent();
        assertThat(jsonObject.getValue(Permission.READ.toJsonKey()).get().asBoolean()).isTrue();
        assertThat(jsonObject.getValue(Permission.WRITE.toJsonKey())).isEmpty();
    }

    @Test
    public void testConstructorWithCollection() {
        final Set<Permission> input = EnumSet.of(Permission.READ, Permission.WRITE);
        final Permissions permissions = new MutablePermissions(input);
        assertThat(permissions.size()).isEqualTo(2);
        assertThat(permissions.contains(Permission.READ)).isTrue();
        assertThat(permissions.contains(Permission.WRITE)).isTrue();
    }

    @Test
    public void testConstructorWithEmptyCollection() {
        final Set<Permission> input = new HashSet<>();
        final Permissions permissions = new MutablePermissions(input);
        assertThat(permissions.size()).isEqualTo(0);
    }

    @Test
    public void testConstructorWithNullCollectionThrowsNullPointerException() {
        assertThatThrownBy(() -> new MutablePermissions((Set<Permission>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("permissions");
    }

    @Test
    public void testConstructorWithNullCollectionInConstructorThrowsNullPointerException() {
        assertThatThrownBy(() -> new MutablePermissions((Collection<Permission>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("permissions");
    }

}
