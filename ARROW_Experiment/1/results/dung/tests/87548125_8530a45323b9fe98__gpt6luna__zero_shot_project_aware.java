package nl.futureedge.sonar.plugin.packageanalyzer.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import nl.futureedge.sonar.plugin.packageanalyzer.model.Model;
import org.junit.Test;

public class Analyzer_ArrowTest {

	@Test
	public void findPackageCyclesReturnsEmptyListForEmptyModel() {
		Model<Object> model = new Model<Object>(Collections.emptySet());

		List<PackageCycle<Object>> cycles = new Analyzer<Object>().findPackageCycles(model);

		assertTrue(cycles.isEmpty());
	}

	@Test
	public void findPackageCyclesReturnsEmptyListForModelWithoutCycles() {
		Model<Object> model = new Model<Object>(Collections.emptySet());

		List<PackageCycle<Object>> cycles = new Analyzer<Object>().findPackageCycles(model);

		assertEquals(0, cycles.size());
	}
}
