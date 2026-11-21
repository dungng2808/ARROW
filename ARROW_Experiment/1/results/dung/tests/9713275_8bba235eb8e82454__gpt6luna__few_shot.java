package com.github.dozedoff.similarImage.thread.pipeline;

import com.github.dozedoff.similarImage.db.ImageRecord;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class ImageQueryPipeline_ArrowTest {

    @Test
    public void applyRunsQueryGrouperAndPostProcessingInOrder() {
        final Path path = Paths.get("images");
        final List<ImageRecord> images = new ArrayList<ImageRecord>();
        final Multimap<Long, ImageRecord> grouped = ArrayListMultimap.create();
        final Multimap<Long, ImageRecord> processed = ArrayListMultimap.create();
        final List<String> calls = new ArrayList<String>();

        Function<Path, List<ImageRecord>> query = new Function<Path, List<ImageRecord>>() {
            @Override
            public List<ImageRecord> apply(Path input) {
                assertSame(path, input);
                calls.add("query");
                return images;
            }
        };
        Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> grouper =
                new Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Collection<ImageRecord> input) {
                        assertSame(images, input);
                        calls.add("group");
                        return grouped;
                    }
                };
        Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>> first =
                new Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Multimap<Long, ImageRecord> input) {
                        assertSame(grouped, input);
                        calls.add("first");
                        return processed;
                    }
                };
        Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>> second =
                new Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Multimap<Long, ImageRecord> input) {
                        assertSame(processed, input);
                        calls.add("second");
                        return input;
                    }
                };

        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                query, grouper, Arrays.asList(first, second));

        assertSame(processed, pipeline.apply(path));
        assertEquals(Arrays.asList("query", "group", "first", "second"), calls);
    }

    @Test
    public void applyReturnsGroupedResultsWhenThereAreNoPostProcessingStages() {
        final Multimap<Long, ImageRecord> grouped = ArrayListMultimap.create();
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                new Function<Path, List<ImageRecord>>() {
                    @Override
                    public List<ImageRecord> apply(Path path) {
                        return Collections.emptyList();
                    }
                },
                new Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Collection<ImageRecord> images) {
                        return grouped;
                    }
                },
                Collections.<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>emptyList());

        assertSame(grouped, pipeline.apply(null));
    }

    @Test
    public void getPostProcessingStagesReturnsImmutableCopy() {
        Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>> stage =
                new Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Multimap<Long, ImageRecord> input) {
                        return input;
                    }
                };
        List<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> stages =
                new ArrayList<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>();
        stages.add(stage);
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                new Function<Path, List<ImageRecord>>() {
                    @Override
                    public List<ImageRecord> apply(Path path) {
                        return Collections.emptyList();
                    }
                },
                new Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Collection<ImageRecord> images) {
                        return ArrayListMultimap.create();
                    }
                },
                stages);

        Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> result =
                pipeline.getPostProcessingStages();
        stages.clear();

        assertEquals(Collections.singletonList(stage), new ArrayList<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>(result));
        assertNotSame(stages, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getPostProcessingStagesReturnsUnmodifiableCollection() {
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                new Function<Path, List<ImageRecord>>() {
                    @Override
                    public List<ImageRecord> apply(Path path) {
                        return Collections.emptyList();
                    }
                },
                new Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Collection<ImageRecord> images) {
                        return ArrayListMultimap.create();
                    }
                },
                Collections.<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>emptyList());

        pipeline.getPostProcessingStages().clear();
    }

    @Test
    public void getImageGrouperReturnsConfiguredFunction() {
        Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> grouper =
                new Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>>() {
                    @Override
                    public Multimap<Long, ImageRecord> apply(Collection<ImageRecord> images) {
                        return ArrayListMultimap.create();
                    }
                };
        ImageQueryPipeline pipeline = new ImageQueryPipeline(
                new Function<Path, List<ImageRecord>>() {
                    @Override
                    public List<ImageRecord> apply(Path path) {
                        return Collections.emptyList();
                    }
                },
                grouper,
                Collections.<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>>emptyList());

        assertSame(grouper, pipeline.getImageGrouper());
    }
}
