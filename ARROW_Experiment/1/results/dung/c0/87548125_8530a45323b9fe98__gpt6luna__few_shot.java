package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

public class Analyzer_ArrowTest {

    @Test
    public void findsNoCyclesInEmptyModel() {
        Model<String> model = new Model<>();
        Analyzer<String> analyzer = new Analyzer<>();

        List<PackageCycle<String>> cycles = analyzer.findPackageCycles(model);

        assertTrue(cycles.isEmpty());
    }

    @Test
    public void findsCycleForMutuallyUsingPackages() {
        Package<String> first = new Package<>("first");
        Package<String> second = new Package<>("second");
        first.getPackageUsages().add(second);
        second.getPackageUsages().add(first);

        Model<String> model = new Model<>();
        model.getPackages().add(first);
        model.getPackages().add(second);

        Analyzer<String> analyzer = new Analyzer<>();
        List<PackageCycle<String>> cycles = analyzer.findPackageCycles(model);

        assertEquals(1, cycles.size());
        assertEquals(2, cycles.get(0).getPackages().size());
        assertTrue(cycles.get(0).getPackages().contains(first));
        assertTrue(cycles.get(0).getPackages().contains(second));
    }

    @Test
    public void findsSelfCycle() {
        Package<String> only = new Package<>("only");
        only.getPackageUsages().add(only);

        Model<String> model = new Model<>();
        model.getPackages().add(only);

        Analyzer<String> analyzer = new Analyzer<>();
        List<PackageCycle<String>> cycles = analyzer.findPackageCycles(model);

        assertEquals(1, cycles.size());
        assertEquals(Collections.singletonList(only), cycles.get(0).getPackages());
    }

    @Test
    public void findsSeparateCycles() {
        Package<String> first = new Package<>("first");
        Package<String> second = new Package<>("second");
        Package<String> third = new Package<>("third");
        Package<String> fourth = new Package<>("fourth");

        first.getPackageUsages().add(second);
        second.getPackageUsages().add(first);
        third.getPackageUsages().add(fourth);
        fourth.getPackageUsages().add(third);

        Model<String> model = new Model<>();
        model.getPackages().add(first);
        model.getPackages().add(second);
        model.getPackages().add(third);
        model.getPackages().add(fourth);

        Analyzer<String> analyzer = new Analyzer<>();
        List<PackageCycle<String>> cycles = analyzer.findPackageCycles(model);

        assertEquals(2, cycles.size());
        List<List<Package<String>>> actual = new ArrayList<>();
        for (PackageCycle<String> cycle : cycles) {
            actual.add(cycle.getPackages());
        }
        assertTrue(actual.contains(Arrays.asList(first, second))
                || actual.contains(Arrays.asList(second, first)));
        assertTrue(actual.contains(Arrays.asList(third, fourth))
                || actual.contains(Arrays.asList(fourth, third)));
    }
}
