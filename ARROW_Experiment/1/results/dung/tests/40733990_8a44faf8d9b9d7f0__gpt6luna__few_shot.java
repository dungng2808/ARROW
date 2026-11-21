package com.github.ricksbrown.cowsay.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CowsayTask_ArrowTest {

    @Test
    public void executeSetsConfiguredProjectProperty() {
        CowsayTask task = new CowsayTask();
        Project project = new Project();
        task.setProject(project);
        task.setProperty("cowsay.output");
        task.setMessage("Moo!");
        task.execute();

        String output = project.getProperty("cowsay.output");
        assertNotNull(output);
        assertTrue(output.contains("Moo!"));
    }

    @Test
    public void executeWithThinkModeSetsConfiguredProjectProperty() {
        CowsayTask task = new CowsayTask();
        Project project = new Project();
        task.setProject(project);
        task.setProperty("cowsay.output");
        task.setThink(true);
        task.setMessage("Moo!");
        task.execute();

        String output = project.getProperty("cowsay.output");
        assertNotNull(output);
        assertTrue(output.contains("Moo!"));
    }

    @Test
    public void executeWithEmptyPropertyDoesNotThrow() {
        CowsayTask task = new CowsayTask();
        task.setProperty("");
        task.setMessage("Moo!");

        task.execute();
    }

    @Test
    public void executeWithoutMessageThrowsBuildException() {
        CowsayTask task = new CowsayTask();
        task.setProject(new Project());
        task.setProperty("cowsay.output");

        try {
            task.execute();
        } catch (BuildException exception) {
            assertNotNull(exception);
            return;
        }
        throw new AssertionError("Expected BuildException");
    }
}
