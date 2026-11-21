package org.eclipse.ditto.model.things;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class MutablePermissions_ArrowTest {

    @Test
    public void testNoneReturnsEmptySet() {
        final Permissions permissions = MutablePermissions.none();
        assertEquals(0, permissions.size());
    }

    @Test
    public void testAllReturnsAllPermissions() {
        final Permissions permissions = MutablePermissions.all();
        assertEquals(Permission.values().length, permissions.size());
    }

    @Test
    public void testOfCreatesSetWithGivenPermissions() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        assertTrue(permissions.contains(Permission.READ));
        assertTrue(permissions.contains(Permission.WRITE));
        assertFalse(permissions.contains(Permission.DELETE));
        assertEquals(2, permissions.size());
    }

    @Test
    public void testContainsWithSinglePermission() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertTrue(permissions.contains(Permission.READ));
        assertFalse(permissions.contains(Permission.WRITE));
    }

    @Test
    public void testContainsWithMultiplePermissions() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        assertTrue(permissions.contains(Permission.READ, Permission.WRITE));
        assertFalse(permissions.contains(Permission.READ, Permission.DELETE));
    }

    @Test
    public void testAddNewPermission() {
        final Permissions permissions = MutablePermissions.none();
        assertTrue(permissions.add(Permission.READ));
        assertTrue(permissions.contains(Permission.READ));
        assertEquals(1, permissions.size());
    }

    @Test
    public void testAddExistingPermission() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        assertFalse(permissions.add(Permission.READ));
        assertEquals(1, permissions.size());
    }

    @Test
    public void testIterator() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE);
        final Set<Permission> retrieved = new HashSet<>();
        for (final Permission p : permissions) {
            retrieved.add(p);
        }
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.contains(Permission.READ));
        assertTrue(retrieved.contains(Permission.WRITE));
    }

    @Test
    public void testSize() {
        final Permissions permissions = MutablePermissions.of(Permission.READ, Permission.WRITE, Permission.DELETE);
        assertEquals(3, permissions.size());
    }

    @Test
    public void testConstructorWithCollection() {
        final Set<Permission> input = EnumSet.of(Permission.READ, Permission.WRITE);
        final Permissions permissions = new MutablePermissions(input);
        assertTrue(permissions.contains(Permission.READ));
        assertTrue(permissions.contains(Permission.WRITE));
        assertEquals(2, permissions.size());
    }

    @Test
    public void testConstructorWithEmptyCollection() {
        final Set<Permission> input = EnumSet.noneOf(Permission.class);
        final Permissions permissions = new MutablePermissions(input);
        assertEquals(0, permissions.size());
    }

    @Test
    public void testToJsonWithPredicate() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final JsonObject jsonObject = permissions.toJson(JsonSchemaVersion.V_2, field -> true);
        assertTrue(jsonObject.getValue(Permission.READ.toJsonKey()).get().asBoolean());
        assertFalse(jsonObject.getValue(Permission.WRITE.toJsonKey()).get().asBoolean());
    }

    @Test
    public void testToJsonWithPointer() {
        final Permissions permissions = MutablePermissions.of(Permission.READ);
        final JsonObject jsonObject = permissions.toJson(JsonPointer.of("read"));
        assertTrue(jsonObject.getValue(Permission.READ.toJsonKey()).get().asBoolean());
        assertFalse(jsonObject.getValue(Permission.WRITE.toJsonKey()).get().asBoolean());
    }

}
