package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
		Model<String> model = mock(Model.class);
		Package<String> pkg = mock(Package.class);
		Set<Package<String>> packages = new HashSet<>();
		packages.add(pkg);
		when(model.getPackages()).thenReturn(packages);
		when(pkg.getPackageUsages()).thenReturn(new HashSet<Package<String>>());
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
		
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testFindPackageCyclesSinglePackageWithUsage() {
		Model<String> model = mock(Model.class);
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		Set<Package<String>> packages = new HashSet<>();
		packages.add(pkg1);
		packages.add(pkg2);
		when(model.getPackages()).thenReturn(packages);
		Set<Package<String>> usages1 = new HashSet<>();
		usages1.add(pkg2);
		when(pkg1.getPackageUsages()).thenReturn(usages1);
		when(pkg2.getPackageUsages()).thenReturn(new HashSet<Package<String>>());
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.findPackageCycles(model);
		
		assertNotNull(result);
		assertEquals(0, result.size());
	}
	
	@Test
	public void testCreateEdges() {
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		Set<Package<String>> packages = new HashSet<>();
		packages.add(pkg1);
		packages.add(pkg2);
		Set<Package<String>> usages1 = new HashSet<>();
		usages1.add(pkg2);
		when(pkg1.getPackageUsages()).thenReturn(usages1);
		when(pkg2.getPackageUsages()).thenReturn(new HashSet<Package<String>>());
		
		Analyzer<String> analyzer = new Analyzer<>();
		java.util.Map<Package<String>, Set<Package<String>>> result = analyzer.createEdges(packages);
		
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(usages1, result.get(pkg1));
		assertEquals(new HashSet<Package<String>>(), result.get(pkg2));
	}
	
	@Test
	public void testCreatePackageCycles() {
		Package<String> pkg1 = mock(Package.class);
		Package<String> pkg2 = mock(Package.class);
		List<Package<String>> circuit = new ArrayList<>();
		circuit.add(pkg1);
		circuit.add(pkg2);
		List<List<Package<String>>> elementaryCircuits = new ArrayList<>();
		elementaryCircuits.add(circuit);
		
		Analyzer<String> analyzer = new Analyzer<>();
		List<PackageCycle<String>> result = analyzer.createPackageCycles(elementaryCircuits);
		
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(circuit, result.get(0).getPackages());
	}
}
