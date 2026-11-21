package org.doble.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CommandNew_ArrowTest {

    @Test
    public void commandNewCanBeCreated() {
        CommandNew command = new CommandNew();
        assertNotNull(command);
    }

    @Test
    public void titlePartsFieldCanBeInitialized() throws Exception {
        CommandNew command = new CommandNew();
        Field field = CommandNew.class.getDeclaredField("adrTitleParts");
        field.setAccessible(true);
        List<String> titleParts = new ArrayList<String>(Arrays.asList("A", "new", "decision"));
        field.set(command, titleParts);

        assertEquals(titleParts, field.get(command));
    }
}
