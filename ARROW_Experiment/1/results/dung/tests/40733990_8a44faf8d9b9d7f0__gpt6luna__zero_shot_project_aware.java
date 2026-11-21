package com.github.ricksbrown.cowsay.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CowsayTask_ArrowTest {

    @Test
    public void executeStoresOutputInConfiguredProperty() {
        CowsayTask task = new CowsayTask();
        Project project = new Project();
        task.setProject(project);
        task.setProperty("cow.output");
        task.setMessage("Moo!");
        task.execute();

        assertNotNull(project.getProperty("cow.output"));
        assertEquals(true, project.getProperty("cow.output").contains("Moo!"));
    }

    @Test
    public void executeWithEmptyPropertyDoesNotThrow() {
        CowsayTask task = new CowsayTask();
        task.setProperty("");
        task.setMessage("Moo!");

        task.execute();
    }

    @Test(expected = BuildException.class)
    public void executeWithoutMessageThrowsBuildException() {
        CowsayTask task = new CowsayTask();
        task.setProperty("cow.output");
        task.execute();
    }

    @Test
    public void addTextUsesProjectPropertyReplacement() {
        CowsayTask task = new CowsayTask();
        Project project = new Project();
        project.setProperty("animal", "Moo!");
        task.setProject(project);
        task.setProperty("cow.output");
        task.addText("${animal}");
        task.execute();

        assertNotNull(project.getProperty("cow.output"));
        assertEquals(true, project.getProperty("cow.output").contains("Moo!"));
    }
}
