package org.doble.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommandNew_ArrowTest {

    @Test
    public void commandCanBeConstructed() {
        CommandNew command = new CommandNew();
        assertEquals(CommandNew.class, command.getClass());
    }
}
