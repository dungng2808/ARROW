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
        final Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return new HashSet<Package<String>>();
            }
        };

        final Analyzer<String> analyzer = new Analyzer<String>();
        final List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindPackageCycles_SinglePackageNoUsage() {
        // Test with single package that doesn't use any other packages
        final Package<String> pkg = new Package<String>("com.example") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                return new HashSet<Package<String>>();
            }
        };

        final Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg);

        final Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        final Analyzer<String> analyzer = new Analyzer<String>();
        final List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindPackageCycles_SinglePackageSelfReference() {
        // Test with single package that references itself
        final Package<String> pkg = new Package<String>("com.example") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                final Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(this);
                return usages;
            }
        };

        final Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg);

        final Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        final Analyzer<String> analyzer = new Analyzer<String>();
        final List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(0).getPackages());
        assertEquals(1, result.get(0).getPackages().size());
        assertEquals(pkg, result.get(0).getPackages().get(0));
    }

    @Test
    public void testFindPackageCycles_TwoPackagesCircularReference() {
        // Test with two packages referencing each other
        final Package<String> pkg1 = new Package<String>("com.example1") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                final Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg2);
                return usages;
            }
        };

        final Package<String> pkg2 = new Package<String>("com.example2") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                final Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg1);
                return usages;
            }
        };

        final Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg1);
        packages.add(pkg2);

        final Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        final Analyzer<String> analyzer = new Analyzer<String>();
        final List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(0).getPackages());
        assertEquals(2, result.get(0).getPackages().size());
        assertTrue(result.get(0).getPackages().contains(pkg1));
        assertTrue(result.get(0).getPackages().contains(pkg2));
    }

    @Test
    public void testCreateEdges() {
        // Test the createEdges method directly
        final Package<String> pkg1 = new Package<String>("com.example1") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                final Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg2);
                return usages;
            }
        };

        final Package<String> pkg2 = new Package<String>("com.example2") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                final Set<Package<String>> usages = new HashSet<Package<String>>();
                usages.add(pkg1);
                return usages;
            }
        };

        final Set<Package<String>> packages = new HashSet<Package<String>>();
        packages.add(pkg1);
        packages.add(pkg2);

        final Analyzer<String> analyzer = new Analyzer<String>();
        
        // We can't directly test private method, but we can verify behavior through findPackageCycles
        final Model<String> model = new Model<String>() {
            @Override
            public Set<Package<String>> getPackages() {
                return packages;
            }
        };

        final List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testCreatePackageCycles() {
        // Test the createPackageCycles method directly
        final Package<String> pkg1 = new Package<String>("com.example1") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                return new HashSet<Package<String>>();
            }
        };

        final Package<String> pkg2 = new Package<String>("com.example2") {
            @Override
            public Set<Package<String>> getPackageUsages() {
                return new HashSet<Package<String>>();
            }
        };

        final List<List<Package<String>>> elementaryCircuits = new ArrayList<List<Package<String>>>();
        final List<Package<String>> circuit = new ArrayList<Package<String>>();
        circuit.add(pkg1);
        circuit.add(pkg2);
        elementaryCircuits.add(circuit);

        final Analyzer<String> analyzer = new Analyzer<String>();
        final List<PackageCycle<String>> result = analyzer.createPackageCycles(elementaryCircuits);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(0).getPackages());
        assertEquals(2, result.get(0).getPackages().size());
        assertEquals(pkg1, result.get(0).getPackages().get(0));
        assertEquals(pkg2, result.get(0).getPackages().get(1));
    }
}
