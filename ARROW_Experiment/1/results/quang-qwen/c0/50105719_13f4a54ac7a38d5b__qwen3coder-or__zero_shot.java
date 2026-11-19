package enkan.component;

import enkan.exception.MisconfigurationException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ComponentRelationship_ArrowTest {

    @Test
    public void testComponentBuilder() {
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        assertNotNull(relationship);
    }

    @Test
    public void testInjectWithValidComponents() {
        // Create mock SystemComponent instances
        SystemComponent targetComponent = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
        };
        
        SystemComponent dep1Component = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
        };
        
        SystemComponent dep2Component = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
        };

        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);
        components.put("dep1", dep1Component);
        components.put("dep2", dep2Component);

        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        relationship.inject(components);
    }

    @Test(expected = MisconfigurationException.class)
    public void testInjectWithMissingTargetComponent() {
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("dep1", new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
        });

        ComponentRelationship relationship = ComponentRelationship.component("missingTarget").using("dep1");
        relationship.inject(components);
    }

    @Test(expected = MisconfigurationException.class)
    public void testInjectWithMissingDependentComponent() {
        SystemComponent targetComponent = new SystemComponent() {
            private Map<String, SystemComponent> dependencies = new HashMap<>();
            
            @Override
            public Map<String, SystemComponent> getAllDependencies() {
                return dependencies;
            }
            
            @Override
            public void setDependencies(Map<String, SystemComponent> dependencies) {
                this.dependencies = dependencies;
            }
        };

        Map<String, SystemComponent> components = new HashMap<>();
        components.put("target", targetComponent);

        ComponentRelationship relationship = ComponentRelationship.component("target").using("missingDep");
        relationship.inject(components);
    }

    @Test
    public void testSortWhenDependentsAreBeforeTarget() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("dep1", "dep2", "target"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        relationship.sort(componentsOrder);
        assertEquals(Arrays.asList("dep1", "dep2", "target"), componentsOrder);
    }

    @Test
    public void testSortWhenDependentsAreAfterTarget() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("target", "dep1", "dep2"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1", "dep2");
        relationship.sort(componentsOrder);
        assertEquals(Arrays.asList("dep1", "target", "dep2"), componentsOrder);
    }

    @Test
    public void testSortWhenDependentsAreNotInList() {
        LinkedList<String> componentsOrder = new LinkedList<>(Arrays.asList("target"));
        ComponentRelationship relationship = ComponentRelationship.component("target").using("dep1");
        relationship.sort(componentsOrder);
        assertEquals(Arrays.asList("target"), componentsOrder);
    }

    @Test
    public void testIndexFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = ComponentRelationship.component("target").using();
        assertEquals(1, relationship.index(list, "b"));
    }

    @Test
    public void testIndexNotFound() {
        List<String> list = Arrays.asList("a", "b", "c");
        ComponentRelationship relationship = ComponentRelationship.component("target").using();
        assertEquals(-1, relationship.index(list, "d"));
    }
}
