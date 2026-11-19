package org.eclipse.ditto.model.things;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

public class MutablePermissions_ArrowTest {

    @Test
    public void noneIsEmpty() {
        final Permissions permissions = MutablePermissions.none();

        assertTrue(permissions.isEmpty());
        assertEquals(0, permissions.size());
    }

    @Test
    public void allContainsEveryPermission() {
        final Permissions permissions = MutablePermissions.all();

        assertEquals(EnumSet.allOf(Permission.class), permissions);
    }

    @Test
    public void ofContainsRequestedPermissions() {
        final Permission permission = Permission.values()[0];
        final Permissions permissions = MutablePermissions.of(permission);

        assertTrue(permissions.contains(permission));
        assertEquals(Collections.singleton(permission), permissions);
    }

    @Test
    public void containsChecksAllRequestedPermissions() {
        final Permission[] permissions = Permission.values();
        final MutablePermissions mutablePermissions =
                new MutablePermissions(Collections.singleton(permissions[0]));

        assertTrue(mutablePermissions.contains(permissions[0]));
        assertFalse(mutablePermissions.contains(permissions[0], permissions[1]));
    }

    @Test
    public void addReturnsWhetherPermissionWasNew() {
        final Permission permission = Permission.values()[0];
        final MutablePermissions mutablePermissions = new MutablePermissions(Collections.<Permission>emptySet());

        assertTrue(mutablePermissions.add(permission));
        assertFalse(mutablePermissions.add(permission));
        assertEquals(Collections.singleton(permission), mutablePermissions);
    }

    @Test
    public void collectionConstructorCopiesPermissions() {
        final Permission permission = Permission.values()[0];
        final Set<Permission> initial = new HashSet<Permission>();
        initial.add(permission);
        final MutablePermissions mutablePermissions = new MutablePermissions(initial);

        initial.clear();

        assertEquals(Collections.singleton(permission), mutablePermissions);
    }

    @Test
    public void toJsonIncludesBooleanForEveryPermission() {
        final Permission permission = Permission.values()[0];
        final MutablePermissions mutablePermissions =
                new MutablePermissions(Collections.singleton(permission));
        final JsonObject expected = JsonFactory.newObjectBuilder()
                .set(permission.toJsonKey(), JsonFactory.newValue(true))
                .build();

        final JsonObject actual = mutablePermissions.toJson((JsonSchemaVersion) null, field -> true);

        assertEquals(expected, actual);
    }
}
