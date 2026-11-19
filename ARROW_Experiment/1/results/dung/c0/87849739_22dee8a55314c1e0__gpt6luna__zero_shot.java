package org.eclipse.ditto.model.things;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

public class MutablePermissions_ArrowTest {

    @Test
    public void noneIsEmpty() {
        Permissions permissions = MutablePermissions.none();

        assertTrue(permissions.isEmpty());
        assertEquals(0, permissions.size());
    }

    @Test
    public void allContainsEveryPermission() {
        Permissions permissions = MutablePermissions.all();

        assertEquals(Permission.values().length, permissions.size());
        for (Permission permission : Permission.values()) {
            assertTrue(permissions.contains(permission));
        }
    }

    @Test
    public void ofContainsRequestedPermissions() {
        Permission first = Permission.values()[0];
        Permission second = Permission.values().length > 1 ? Permission.values()[1] : first;
        Permissions permissions = MutablePermissions.of(first, second);

        assertTrue(permissions.contains(first));
        assertTrue(permissions.contains(first, second));
    }

    @Test
    public void containsRequiresAllRequestedPermissions() {
        Permission first = Permission.values()[0];
        Permission second = Permission.values().length > 1 ? Permission.values()[1] : first;
        MutablePermissions permissions = new MutablePermissions(Collections.singleton(first));

        assertTrue(permissions.contains(first));
        assertFalse(permissions.contains(first, second));
    }

    @Test
    public void addReturnsWhetherPermissionWasNew() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.<Permission>emptySet());

        assertTrue(permissions.add(permission));
        assertFalse(permissions.add(permission));
        assertEquals(1, permissions.size());
    }

    @Test
    public void collectionConstructorCopiesPermissions() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Arrays.asList(permission));

        assertEquals(1, permissions.size());
        assertTrue(permissions.contains(permission));
    }

    @Test
    public void iteratorVisitsContainedPermissions() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.singleton(permission));
        Iterator<Permission> iterator = permissions.iterator();

        assertTrue(iterator.hasNext());
        assertEquals(permission, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void toJsonIncludesBooleanForEveryPermission() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.singleton(permission));
        JsonObject expected = JsonFactory.newObjectBuilder()
                .set(permission.toJsonKey(), JsonFactory.newValue(true))
                .build();

        JsonObject actual = permissions.toJson((JsonSchemaVersion) null, field -> true);

        assertEquals(expected, actual);
    }

    @Test
    public void toJsonForPointerIncludesMatchingPermission() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.singleton(permission));
        JsonPointer pointer = JsonPointer.of(permission.toJsonKey().toString());
        JsonObject expected = JsonFactory.newObjectBuilder()
                .set(permission.toJsonKey(), JsonFactory.newValue(true))
                .build();

        assertEquals(expected, permissions.toJson(pointer));
    }

    @Test
    public void toJsonForNonMatchingPointerIsEmpty() {
        MutablePermissions permissions = new MutablePermissions(Collections.<Permission>emptySet());

        assertEquals(JsonFactory.newObjectBuilder().build(), permissions.toJson(JsonPointer.of("/unknown")));
    }
}
