package org.jeasy.batch.core.listener;

import org.jeasy.batch.core.job.JobParameters;
import org.jeasy.batch.core.job.JobReport;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CompositeJobListener_ArrowTest {

    @Test
    public void beforeJobCallsListenersInInsertionOrder() {
        final List<String> calls = new ArrayList<>();
        JobListener first = new JobListener() {
            @Override
            public void beforeJob(JobParameters jobParameters) {
                calls.add("first");
            }

            @Override
            public void afterJob(JobReport jobReport) {
            }
        };
        JobListener second = new JobListener() {
            @Override
            public void beforeJob(JobParameters jobParameters) {
                calls.add("second");
            }

            @Override
            public void afterJob(JobReport jobReport) {
            }
        };
        CompositeJobListener composite = new CompositeJobListener(Arrays.asList(first, second));

        composite.beforeJob(null);

        assertEquals(Arrays.asList("first", "second"), calls);
    }

    @Test
    public void afterJobCallsListenersInReverseInsertionOrder() {
        final List<String> calls = new ArrayList<>();
        JobListener first = new JobListener() {
            @Override
            public void beforeJob(JobParameters jobParameters) {
            }

            @Override
            public void afterJob(JobReport jobReport) {
                calls.add("first");
            }
        };
        JobListener second = new JobListener() {
            @Override
            public void beforeJob(JobParameters jobParameters) {
            }

            @Override
            public void afterJob(JobReport jobReport) {
                calls.add("second");
            }
        };
        CompositeJobListener composite = new CompositeJobListener(Arrays.asList(first, second));

        composite.afterJob(null);

        assertEquals(Arrays.asList("second", "first"), calls);
    }

    @Test
    public void addJobListenerAddsDelegate() {
        final List<String> calls = new ArrayList<>();
        CompositeJobListener composite = new CompositeJobListener();
        composite.addJobListener(new JobListener() {
            @Override
            public void beforeJob(JobParameters jobParameters) {
                calls.add("added");
            }

            @Override
            public void afterJob(JobReport jobReport) {
            }
        });

        composite.beforeJob(null);

        assertEquals(Arrays.asList("added"), calls);
    }

    @Test
    public void emptyCompositeDoesNotCallAnyListeners() {
        CompositeJobListener composite = new CompositeJobListener();
        composite.beforeJob(null);
        composite.afterJob(null);
        assertEquals(0, new ArrayList<Object>().size());
    }
}
