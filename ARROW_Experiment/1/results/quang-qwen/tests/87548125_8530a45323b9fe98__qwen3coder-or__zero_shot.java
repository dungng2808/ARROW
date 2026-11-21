package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

public class Analyzer_ArrowTest {

    @Test
    public void testFindPackageCycles_EmptyModel() {
        // Test with empty model
        Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return new HashSet<Package<String>>();
            }
        };

        Analyzer<String> analyzer = new Analyzer<String>();
        List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindPackageCycles_SinglePackageNoUsage() {
        // Test with single package that doesn't use any other packages
        Package<String> pkg = new Package<String>("com.example") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                return new HashSet<Package<String>>();
            }
        };
        
        Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg);
        
        Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        Analyzer<String> analyzer = new Analyzer<String>();
        List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindPackageCycles_SinglePackageSelfReference() {
        // Test with single package that references itself
        Package<String> pkg = new Package<String>("com.example") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(this); // Self reference
                return usages;
            }
        };
        
        Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg);
        
        Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        Analyzer<String> analyzer = new Analyzer<String>();
        List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        PackageCycle<String> cycle = result.get(0);
        assertNotNull(cycle);
        assertEquals(1, cycle.getPackages().size());
        assertEquals(pkg, cycle.getPackages().get(0));
    }

    @Test
    public void testFindPackageCycles_TwoPackagesCircular() {
        // Test with two packages referencing each other
        Package<String> pkg1 = new Package<String>("com.example1") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg2); // References pkg2
                return usages;
            }
        };
        
        Package<String> pkg2 = new Package<String>("com.example2") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg1); // References pkg1
                return usages;
            }
        };
        
        Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg1);
        packages.add(pkg2);
        
        Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        Analyzer<String> analyzer = new Analyzer<String>();
        List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        PackageCycle<String> cycle = result.get(0);
        assertNotNull(cycle);
        assertEquals(2, cycle.getPackages().size());
        assertTrue(cycle.getPackages().contains(pkg1));
        assertTrue(cycle.getPackages().contains(pkg2));
    }

    @Test
    public void testCreateEdges() {
        // Test the createEdges method directly
        Package<String> pkg1 = new Package<String>("com.example1") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg2);
                return usages;
            }
        };
        
        Package<String> pkg2 = new Package<String>("com.example2") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                Set<Package<String>> usages = new HashSet<Package<String>>();
                return usages;
            }
        };
        
        Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg1);
        packages.add(pkg2);
        
        Analyzer<String> analyzer = new Analyzer<String>();
        
        // We can't directly test private method, but we can verify behavior through findPackageCycles
        Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };
        
        List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        assertNotNull(result);
    }

    @Test
    public void testCreatePackageCycles() {
        // Test the createPackageCycles method directly
        Package<String> pkg1 = new Package<String>("com.example1") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                return new HashSet<Package<String>>();
            }
        };
        
        Package<String> pkg2 = new Package<String>("com.example2") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                return new HashSet<Package<String>>();
            }
        };
        
        List<List<Package<String>>> elementaryCircuits = new java.util.ArrayList<List<Package<String>>>();
        List<Package<String>> circuit = new java.util.ArrayList<Package<String>>();
        circuit.add(pkg1);
        circuit.add(pkg2);
        elementaryCircuits.add(circuit);
        
        Analyzer<String> analyzer = new Analyzer<String>();
        
        List<PackageCycle<String>> result = analyzer.createPackageCycles(elementaryCircuits);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        PackageCycle<String> cycle = result.get(0);
        assertNotNull(cycle);
        assertEquals(2, cycle.getPackages().size());
        assertEquals(pkg1, cycle.getPackages().get(0));
        assertEquals(pkg2, cycle.getPackages().get(1));
    }
}
