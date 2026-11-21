package com.github.dozedoff.similarImage.thread.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.github.dozedoff.similarImage.db.ImageRecord;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ImageQueryPipeline_ArrowTest {

    @Test
    public void applyQueriesGroupsAndRunsPostProcessingInOrder() {
        Path path = Paths.get("images");
        List<ImageRecord> images = Collections.emptyList();
        Multimap<Long, ImageRecord> initial = ArrayListMultimap.create();
        Multimap<Long, ImageRecord> afterFirst = ArrayListMultimap.create();
        Multimap<Long, ImageRecord> afterSecond = ArrayListMultimap.create();
        List<String> calls = new ArrayList<String>();

        Function<Path, List<ImageRecord>> query = input -> {
            assertSame(path, input);
            calls.add("query");
            return images;
        };
        Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> grouper = input -> {
            assertSame(images, input);
            calls.add("group");
            return initial;
        };
        Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>> first = input -> {
            assertSame(initial, input);
            calls.add("first");
            return afterFirst;
        };
        Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>> second = input -> {
            assertSame(afterFirst, input);
            calls.add("second");
            return afterSecond;
        };

        ImageQueryPipeline pipeline = new ImageQueryPipeline(query, grouper, Arrays.asList(first, second));

        assertSame(afterSecond, pipeline.apply(path));
        assertEquals(Arrays.asList("query", "group", "first", "second"), calls);
    }

    @Test
    public void applyReturnsGroupedResultsWhenThereAreNoPostProcessingStages() {
        Path path = Paths.get("images");
        Multimap<Long, ImageRecord> groups = ArrayListMultimap.create();
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                input -> Collections.<ImageRecord>emptyList(),
                input -> groups,
                Collections.<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>emptyList());

        assertSame(groups, pipeline.apply(path));
    }

    @Test
    public void getPostProcessingStagesReturnsImmutableCopy() {
        Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>> stage = input -> input;
        Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> stages =
                new ArrayList<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>();
        stages.add(stage);
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                input -> Collections.<ImageRecord>emptyList(),
                input -> ArrayListMultimap.<Long, ImageRecord>create(),
                stages);

        Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> result =
                pipeline.getPostProcessingStages();

        assertEquals(1, result.size());
        assertSame(stage, result.iterator().next());
        assertNotSame(stages, result);
        assertThrows(UnsupportedOperationException.class, () -> result.clear());

        stages.clear();
        assertEquals(1, result.size());
        assertEquals(0, pipeline.getPostProcessingStages().size());
    }

    @Test
    public void getImageGrouperReturnsConfiguredFunction() {
        Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> grouper =
                input -> ArrayListMultimap.<Long, ImageRecord>create();
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                input -> Collections.<ImageRecord>emptyList(),
                grouper,
                Collections.<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>emptyList());

        assertSame(grouper, pipeline.getImageGrouper());
    }
}
