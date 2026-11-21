package org.jeasy.batch.core.listener;

import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.job.JobParameters;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class CompositeJobListener_ArrowTest {

    @Mock
    private JobListener jobListener1;

    @Mock
    private JobListener jobListener2;

    @Mock
    private JobParameters jobParameters;

    @Mock
    private JobReport jobReport;

    private CompositeJobListener compositeJobListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        compositeJobListener = new CompositeJobListener();
    }

    @Test
    public void testBeforeJobShouldCallAllListeners() {
        // Given
        compositeJobListener.addJobListener(jobListener1);
        compositeJobListener.addJobListener(jobListener2);

        // When
        compositeJobListener.beforeJob(jobParameters);

        // Then
        verify(jobListener1).beforeJob(jobParameters);
        verify(jobListener2).beforeJob(jobParameters);
    }

    @Test
    public void testAfterJobShouldCallAllListenersInReverseOrder() {
        // Given
        compositeJobListener.addJobListener(jobListener1);
        compositeJobListener.addJobListener(jobListener2);

        // When
        compositeJobListener.afterJob(jobReport);

        // Then
        // Verify that jobListener2 was called first (reverse order)
        verify(jobListener2).afterJob(jobReport);
        verify(jobListener1).afterJob(jobReport);
    }

    @Test
    public void testAddJobListenerShouldAddListenerToList() {
        // Given
        List<JobListener> initialListeners = new ArrayList<>();
        CompositeJobListener compositeJobListener = new CompositeJobListener(initialListeners);

        // When
        compositeJobListener.addJobListener(jobListener1);

        // Then
        verify(jobListener1, never()).beforeJob(any());
        verify(jobListener1, never()).afterJob(any());
    }

    @Test
    public void testConstructorWithEmptyList() {
        // Given
        List<JobListener> emptyListeners = new ArrayList<>();

        // When
        CompositeJobListener compositeJobListener = new CompositeJobListener(emptyListeners);

        // Then
        // No exception should be thrown
    }

    @Test
    public void testBeforeJobWithEmptyListenersList() {
        // Given
        List<JobListener> emptyListeners = new ArrayList<>();
        CompositeJobListener compositeJobListener = new CompositeJobListener(emptyListeners);

        // When
        compositeJobListener.beforeJob(jobParameters);

        // Then
        // No exception should be thrown, no listeners to call
    }

    @Test
    public void testAfterJobWithEmptyListenersList() {
        // Given
        List<JobListener> emptyListeners = new ArrayList<>();
        CompositeJobListener compositeJobListener = new CompositeJobListener(emptyListeners);

        // When
        compositeJobListener.afterJob(jobReport);

        // Then
        // No exception should be thrown, no listeners to call
    }
}
