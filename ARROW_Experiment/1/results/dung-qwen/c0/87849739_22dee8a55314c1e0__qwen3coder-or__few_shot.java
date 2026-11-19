package org.eclipse.ditto.model.things;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

public class MutablePermissions_ArrowTest {

    @Test
    public void noneReturnsEmptySet() {
        final Permissions permissions = MutablePermissions.none();
        assertThat(permissions).isEmpty();
    }

    @Test
    public void allReturnsAllPermissions() {
        final Permissions permissions = MutablePermissions.all();
        assertThat(permissions).containsExactlyInAnyOrder(Permission.values());
    }

    @Test
    public void ofCreatesSetWithGivenPermissions() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        assertThat(permissions).containsExactlyInAnyOrder(Permission.READ, Permission.WRITE);
    }

    @Test
    public void ofThrowsNullPointerExceptionWhenPermissionIsNull() {
        assertThatThrownBy(() -> MutablePermissions.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("permission is null");
    }

    @Test
    public void ofThrowsNullPointerExceptionWhenFurtherPermissionsIsNull() {
        assertThatThrownBy(() -> MutablePermissions.of(Permission.READ, (Permission[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("furtherPermissions is null");
    }

    @Test
    public void containsReturnsTrueWhenPermissionIsPresent() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertThat(permissions.contains(Permission.READ)).isTrue();
    }

    @Test
    public void containsReturnsFalseWhenPermissionIsNotPresent() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertThat(permissions.contains(Permission.WRITE)).isFalse();
    }

    @Test
    public void containsThrowsNullPointerExceptionWhenPermissionIsNull() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertThatThrownBy(() -> permissions.contains(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("permission whose presence is to be checked is null");
    }

    @Test
    public void containsThrowsNullPointerExceptionWhenFurtherPermissionsIsNull() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertThatThrownBy(() -> permissions.contains(Permission.READ, (Permission[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("further permissions whose presence are to be checked is null");
    }

    @Test
    public void addAddsPermissionAndReturnsTrue() {
        final Permissions permissions = MutablePermissions.none();
        final boolean result = permissions.add(Permission.READ);
        assertThat(result).isTrue();
        assertThat(permissions).containsExactly(Permission.READ);
    }

    @Test
    public void addThrowsNullPointerExceptionWhenPermissionIsNull() {
        final Permissions permissions = MutablePermissions.none();
        assertThatThrownBy(() -> permissions.add(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("permission to be added is null");
    }

    @Test
    public void addReturnsFalseWhenPermissionAlreadyExists() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final boolean result = permissions.add(Permission.READ);
        assertThat(result).isFalse();
        assertThat(permissions).containsExactly(Permission.READ);
    }

    @Test
    public void iteratorReturnsCorrectIterator() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        final Set<Permission> result = new HashSet<>();
        for (final Permission permission : permissions) {
            result.add(permission);
        }
        assertThat(result).containsExactlyInAnyOrder(Permission.READ, Permission.WRITE);
    }

    @Test
    public void sizeReturnsCorrectSize() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        assertThat(permissions.size()).isEqualTo(2);
    }

    @Test
    public void sizeReturnsZeroForEmptySet() {
        final Permissions permissions = MutablePermissions.none();
        assertThat(permissions.size()).isEqualTo(0);
    }

    @Test
    public void toJsonReturnsCorrectJsonObject() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final JsonObject jsonObject = permissions.toJson(JsonSchemaVersion.V_1, field -> true);
        assertThat(jsonObject.getValue(Permission.READ.toJsonKey())).hasValue(true);
        assertThat(jsonObject.getValue(Permission.WRITE.toJsonKey())).hasValue(false);
    }

    @Test
    public void toJsonWithPointerReturnsCorrectJsonObject() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final JsonObject jsonObject = permissions.toJson(Permission.READ.toJsonKey());
        assertThat(jsonObject.getValue(Permission.READ.toJsonKey())).hasValue(true);
    }

    @Test
    public void constructorWithSetWorksCorrectly() {
        final Set<Permission> permissionsSet = EnumSet.of(Permission.READ, Permission.WRITE);
        final Permissions permissions = new MutablePermissions(permissionsSet);
        assertThat(permissions).containsExactlyInAnyOrder(Permission.READ, Permission.WRITE);
    }

    @Test
    public void constructorWithCollectionWorksCorrectly() {
        final Set<Permission> permissionsSet = EnumSet.of(Permission.READ, Permission.WRITE);
        final Permissions permissions = new MutablePermissions(permissionsSet);
        assertThat(permissions).containsExactlyInAnyOrder(Permission.READ, Permission.WRITE);
    }

    @Test
    public void constructorWithEmptyCollectionWorksCorrectly() {
        final Set<Permission> permissionsSet = EnumSet.noneOf(Permission.class);
        final Permissions permissions = new MutablePermissions(permissionsSet);
        assertThat(permissions).isEmpty();
    }

    @Test
    public void constructorThrowsNullPointerExceptionWhenCollectionIsNull() {
        assertThatThrownBy(() -> new MutablePermissions((Set<Permission>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("permissions is null");
    }

    @Test
    public void constructorThrowsNullPointerExceptionWhenCollectionIsNullForCollectionConstructor() {
        assertThatThrownBy(() -> new MutablePermissions((Collection<Permission>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("permissions is null");
    }

    @Test
    public void equalsAndHashCodeWorkCorrectly() {
        final Permissions permissions1 = MutablePermissions.of(Permission.READ);
        final Permissions permissions2 = MutablePermissions.of(Permission.READ);
        assertThat(permissions1).isEqualTo(permissions2);
        assertThat(permissions1.hashCode()).isEqualTo(permissions2.hashCode());
    }

    @Test
    public void equalsReturnsFalseForDifferentPermissions() {
        final Permissions permissions1 = MutablePermissions.of(Permission.READ);
        final Permissions permissions2 = MutablePermissions.of(Permission.WRITE);
        assertThat(permissions1).isNotEqualTo(permissions2);
    }

    @Test
    public void toStringReturnsCorrectString() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertThat(permissions.toString()).contains("READ");
    }
}
