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
        project.init();
        task.setProject(project);
        task.setProperty("cowsay.output");
        task.setMessage("Moo!");

        task.execute();

        String output = project.getProperty("cowsay.output");
        assertNotNull(output);
        assertEquals(true, output.contains("Moo!"));
    }

    @Test
    public void executeWithoutMessageThrowsBuildException() {
        CowsayTask task = new CowsayTask();
        Project project = new Project();
        project.init();
        task.setProject(project);
        task.setProperty("cowsay.output");

        try {
            task.execute();
        } catch (BuildException expected) {
            assertNotNull(expected.getCause());
            return;
        }
        throw new AssertionError("Expected BuildException");
    }

    @Test
    public void settersAcceptConfigurationValues() {
        CowsayTask task = new CowsayTask();
        task.setCowfile("default");
        task.setThink(true);
        task.setLang("en");
        task.setAlt("cow");
        task.setHtml(true);
        task.setTongue("U ");
        task.setWrap("0");
        task.setMessage("Moo!");
        task.setMode("b");

        assertNotNull(task);
    }
}
