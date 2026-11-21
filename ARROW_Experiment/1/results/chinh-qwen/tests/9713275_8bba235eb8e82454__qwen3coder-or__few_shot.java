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

import org.junit.Before;
import org.junit.Test;

import com.github.dozedoff.similarImage.db.ImageRecord;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

public class ImageQueryPipeline_ArrowTest {
    
    private ImageQueryPipeline pipeline;
    private Path mockPath;
    private List<ImageRecord> mockImages;
    private Multimap<Long, ImageRecord> mockGroups;
    private Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> mockPostProcessingStages;
    
    @Before
    public void setUp() throws Exception {
        mockPath = mock(Path.class);
        mockImages = Arrays.asList(mock(ImageRecord.class));
        mockGroups = mock(Multimap.class);
        
        @SuppressWarnings("unchecked")
        Function<Path, List<ImageRecord>> mockQueryStage = mock(Function.class);
        @SuppressWarnings("unchecked")
        Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> mockGrouper = mock(Function.class);
        mockPostProcessingStages = ImmutableList.of();
        
        when(mockQueryStage.apply(mockPath)).thenReturn(mockImages);
        when(mockGrouper.apply(mockImages)).thenReturn(mockGroups);
        
        pipeline = new ImageQueryPipeline(mockQueryStage, mockGrouper, mockPostProcessingStages);
    }
    
    @Test
    public void testApplyReturnsGroups() throws Exception {
        Multimap<Long, ImageRecord> result = pipeline.apply(mockPath);
        assertThat(result, is(notNullValue()));
    }
    
    @Test
    public void testGetPostProcessingStagesReturnsImmutableCopy() throws Exception {
        Collection<Function<Multimap<Long, ImageRecord>, Multimap<Long, ImageRecord>>> result = 
                pipeline.getPostProcessingStages();
        assertThat(result, is(notNullValue()));
        assertThat(result, is(ImmutableList.of()));
    }
    
    @Test
    public void testGetImageGrouperReturnsCorrectInstance() throws Exception {
        Function<Collection<ImageRecord>, Multimap<Long, ImageRecord>> result = 
                pipeline.getImageGrouper();
        assertThat(result, is(notNullValue()));
    }
}
