package org.jeasy.batch.core.listener;

import org.jeasy.batch.core.job.JobReport;
import org.jeasy.batch.core.job.JobParameters;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class CompositeJobListener_ArrowTest {

    @Test
    public void testBeforeJob() {
        // Given
        JobParameters jobParameters = mock(JobParameters.class);
        JobListener listener1 = mock(JobListener.class);
        JobListener listener2 = mock(JobListener.class);
        List<JobListener> listeners = new ArrayList<>();
        listeners.add(listener1);
        listeners.add(listener2);
        CompositeJobListener compositeJobListener = new CompositeJobListener(listeners);

        // When
        compositeJobListener.beforeJob(jobParameters);

        // Then
        verify(listener1).beforeJob(jobParameters);
        verify(listener2).beforeJob(jobParameters);
    }

    @Test
    public void testAfterJob() {
        // Given
        JobReport jobReport = mock(JobReport.class);
        JobListener listener1 = mock(JobListener.class);
        JobListener listener2 = mock(JobListener.class);
        List<JobListener> listeners = new ArrayList<>();
        listeners.add(listener1);
        listeners.add(listener2);
        CompositeJobListener compositeJobListener = new CompositeJobListener(listeners);

        // When
        compositeJobListener.afterJob(jobReport);

        // Then
        verify(listener2).afterJob(jobReport);
        verify(listener1).afterJob(jobReport);
    }

    @Test
    public void testAddJobListener() {
        // Given
        JobListener listener1 = mock(JobListener.class);
        JobListener listener2 = mock(JobListener.class);
        CompositeJobListener compositeJobListener = new CompositeJobListener();

        // When
        compositeJobListener.addJobListener(listener1);
        compositeJobListener.addJobListener(listener2);

        // Then
        // Verify that listeners were added correctly by checking the size
        // Note: We cannot directly access the internal list, but we can verify behavior
        JobParameters jobParameters = mock(JobParameters.class);
        JobReport jobReport = mock(JobReport.class);
        
        compositeJobListener.beforeJob(jobParameters);
        verify(listener1).beforeJob(jobParameters);
        verify(listener2).beforeJob(jobParameters);
        
        compositeJobListener.afterJob(jobReport);
        verify(listener2).afterJob(jobReport);
        verify(listener1).afterJob(jobReport);
    }

    @Test
    public void testEmptyListeners() {
        // Given
        CompositeJobListener compositeJobListener = new CompositeJobListener();
        JobParameters jobParameters = mock(JobParameters.class);
        JobReport jobReport = mock(JobReport.class);

        // When & Then
        // Should not throw any exception
        compositeJobListener.beforeJob(jobParameters);
        compositeJobListener.afterJob(jobReport);
    }
}
