package org.eclipse.ditto.model.things;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.junit.Test;

public class MutablePermissions_ArrowTest {

    @Test
    public void noneCreatesAnEmptySet() {
        Permissions permissions = MutablePermissions.none();

        assertTrue(permissions.isEmpty());
        assertEquals(0, permissions.size());
    }

    @Test
    public void allContainsEveryPermission() {
        Permissions permissions = MutablePermissions.all();

        assertEquals(EnumSet.allOf(Permission.class), permissions);
    }

    @Test
    public void ofContainsTheSpecifiedPermissions() {
        Permission[] permissionsArray = Permission.values();
        Permission first = permissionsArray[0];
        Permission second = permissionsArray.length > 1 ? permissionsArray[1] : first;

        Permissions permissions = MutablePermissions.of(first, second);

        assertTrue(permissions.contains(first));
        assertEquals(permissionsArray.length > 1 ? 2 : 1, permissions.size());
    }

    @Test
    public void containsChecksAllRequestedPermissions() {
        Permission[] permissionsArray = Permission.values();
        Permission first = permissionsArray[0];
        Permission second = permissionsArray.length > 1 ? permissionsArray[1] : first;
        MutablePermissions permissions = new MutablePermissions(Arrays.asList(first, second));

        assertTrue(permissions.contains(first, second));
        if (permissionsArray.length > 2) {
            assertFalse(permissions.contains(first, permissionsArray[2]));
        }
    }

    @Test
    public void addReturnsWhetherThePermissionWasNew() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.<Permission>emptySet());

        assertTrue(permissions.add(permission));
        assertFalse(permissions.add(permission));
        assertEquals(1, permissions.size());
    }

    @Test
    public void iteratorVisitsEachPermission() {
        Permission[] permissionsArray = Permission.values();
        Permission first = permissionsArray[0];
        Permission second = permissionsArray.length > 1 ? permissionsArray[1] : first;
        MutablePermissions permissions = new MutablePermissions(Arrays.asList(first, second));

        Iterator<Permission> iterator = permissions.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(permissions.contains(iterator.next()));
        if (permissionsArray.length > 1) {
            assertTrue(iterator.hasNext());
            assertTrue(permissions.contains(iterator.next()));
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    public void toJsonIncludesBooleanValuesForEveryPermission() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.singleton(permission));

        JsonObject json = permissions.toJson((JsonSchemaVersion) null, null);

        for (Permission value : Permission.values()) {
            assertEquals(JsonFactory.newValue(value == permission),
                    json.getValue(value.toJsonKey()).get());
        }
    }

    @Test
    public void toJsonWithPointerIncludesOnlyTheMatchingPermission() {
        Permission permission = Permission.values()[0];
        MutablePermissions permissions = new MutablePermissions(Collections.singleton(permission));
        JsonPointer pointer = JsonPointer.of(permission.toJsonKey());

        JsonObject json = permissions.toJson(pointer);

        assertEquals(1, json.getSize());
        assertEquals(JsonFactory.newValue(true), json.getValue(permission.toJsonKey()).get());
    }
}
