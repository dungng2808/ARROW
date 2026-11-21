package com.github.dozedoff.similarImage.thread.pipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.github.dozedoff.similarImage.db.ImageRecord;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

public class ImageQueryPipeline_ArrowTest {

	private Function<Path, List<ImageRecord>> imageQueryStage;
	private Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> imageGrouper;
	private Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> postProcessingStages;
	private ImageQueryPipeline pipeline;

	@Before
	public void setUp() {
		imageQueryStage = mock(Function.class);
		imageGrouper = mock(Function.class);
		postProcessingStages = mock(Collection.class);
		pipeline = new ImageQueryPipeline(imageQueryStage, imageGrouper, postProcessingStages);
	}

	@Test
	public void testConstructor() {
		assertThat(pipeline, is(notNullValue()));
	}

	@Test
	public void testApply() throws Exception {
		Path path = mock(Path.class);
		List<ImageRecord> images = Arrays.asList(mock(ImageRecord.class));
		Multimap<Long, ImageRecord> groups = mock(Multimap.class);
		Multimap<Long, ImageRecord> processedGroups = mock(Multimap.class);

		when(imageQueryStage.apply(path)).thenReturn(images);
		when(imageGrouper.apply(images)).thenReturn(groups);
		when(postProcessingStages.iterator()).thenReturn(Arrays.<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>asList(
				input -> processedGroups).iterator());

		Multimap<Long, ImageRecord> result = pipeline.apply(path);

		assertThat(result, is(processedGroups));
	}

	@Test
	public void testGetPostProcessingStages() {
		Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> result = pipeline.getPostProcessingStages();

		assertThat(result, is(notNullValue()));
		assertThat(result, is(ImmutableList.copyOf(postProcessingStages)));
	}

	@Test
	public void testGetImageGrouper() {
		Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> result = pipeline.getImageGrouper();

		assertThat(result, is(imageGrouper));
	}
}
