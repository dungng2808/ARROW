package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;

public class Analyzer_ArrowTest {

	@Test
	public void findPackageCyclesWithEmptyModelReturnsNoCycles() {
		Model<Object> model = new Model<>();
		Analyzer<Object> analyzer = new Analyzer<>();

		List<PackageCycle<Object>> cycles = analyzer.findPackageCycles(model);

		assertTrue(cycles.isEmpty());
	}
}
