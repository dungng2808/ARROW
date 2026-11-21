package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import nl.futureedge.sonar.plugin.packageanalyzer.model.Package;

public class Analyzer_ArrowTest {

	@Test
	public void testFindPackageCyclesEmptyModel() {
		Model<String> model = mock(Model.class);
		Set<Package<String>> packages = new HashSet<>();
		when(model.getPackages()).thenReturn(packages);
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
		
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testFindPackageCyclesSinglePackageNoUsage() {
		Package<String> pkg = mock(Package.class);
		Set<Package<String>> usages = new HashSet<>();
		when(pkg.getPackageUsages()).thenReturn(usages);
		
		Model<String> model = mock(Model.class);
		Set<Package<String>> packages = new HashSet<>(Arrays.asList(pkg));
		when(model.getPackages()).thenReturn(packages);
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
		
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testFindPackageCyclesSinglePackageWithUsage() {
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		Set<Package<String>> usages = new HashSet<>(Arrays.asList(pkg2));
		when(pkg1.getPackageUsages()).thenReturn(usages);
		
		Model<String> model = mock(Model.class);
		Set<Package<String>> packages = new HashSet<>(Arrays.asList(pkg1));
		when(model.getPackages()).thenReturn(packages);
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
		
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testFindPackageCyclesTwoPackagesCycle() {
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		Set<Package<String>> usages1 = new HashSet<>(Arrays.asList(pkg2));
		Set<Package<String>> usages2 = new HashSet<>(Arrays.asList(pkg1));
		when(pkg1.getPackageUsages()).thenReturn(usages1);
		when(pkg2.getPackageUsages()).thenReturn(usages2);
		
		Model<String> model = mock(Model.class);
		Set<Package<String>> packages = new HashSet<>(Arrays.asList(pkg1, pkg2));
		when(model.getPackages()).thenReturn(packages);
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		PackageCycle<String> cycle = result.get(0);
		assertNotNull(cycle);
		assertEquals(2, cycle.getPackages().size());
	}
	
	@Test
	public void testCreateEdges() {
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		Set<Package<String>> usages = new HashSet<>(Arrays.asList(pkg2));
		when(pkg1.getPackageUsages()).thenReturn(usages);
		
		Set<Package<String>> packages = new HashSet<>(Arrays.asList(pkg1));
		
		Analyzer<String> analyzer = new Analyzer<>();
		java.util.Map<Package<String>, Set<Package<String>>> result = analyzer.createEdges(packages);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(usages, result.get(pkg1));
	}
	
	@Test
	public void testCreatePackageCycles() {
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		List<Package<String>> circuit = Arrays.asList(pkg1, pkg2);
		
		List<List<Package<String>>> elementaryCircuits = Arrays.asList(circuit);
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.createPackageCycles(elementaryCircuits);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		PackageCycle<String> cycle = result.get(0);
		assertNotNull(cycle);
		assertEquals(2, cycle.getPackages().size());
	}
}
