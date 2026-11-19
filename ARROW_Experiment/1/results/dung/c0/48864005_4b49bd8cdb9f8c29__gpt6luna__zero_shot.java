package com.homeadvisor.robusto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class ApiCommand_ArrowTest
{
   @Test
   public void builderReturnsBuilder()
   {
      ApiCommand.Builder<String> builder = ApiCommand.builder();
      assertNotNull(builder);
   }

   @Test
   public void buildRejectsMissingUriProvider()
   {
      ApiCommand.Builder<String> builder = ApiCommand.builder()
            .withRemoteServiceCallback(null);

      try
      {
         builder.build();
         fail("Expected IllegalArgumentException");
      }
      catch(IllegalArgumentException exception)
      {
         assertEquals("UriProvider cannot be null", exception.getMessage());
      }
   }

   @Test
   public void buildRejectsMissingRemoteServiceCallback()
   {
      ApiCommand.Builder<String> builder = ApiCommand.builder()
            .withUriProvider(null);

      try
      {
         builder.build();
         fail("Expected IllegalArgumentException");
      }
      catch(IllegalArgumentException exception)
      {
         assertEquals("UriProvider cannot be null", exception.getMessage());
      }
   }

   @Test
   public void builderMethodsReturnSameBuilder()
   {
      ApiCommand.Builder<String> builder = ApiCommand.builder();

      assertSame(builder, builder.withCommandGroup("test"));
      assertSame(builder, builder.withNumberOfRetries(2));
      assertSame(builder, builder.withBackoffPolicy(null));
      assertSame(builder, builder.withRetryListener(null));
      assertSame(builder, builder.withCommandCache(null, null));
      assertSame(builder, builder.withExceptionRetryPolicy(Exception.class, true));
      assertSame(builder, builder.withCommandTimeoutInMilliseconds(100));
      assertSame(builder, builder.withNumberOfThreads(2));
      assertSame(builder, builder.withRollingWindowMetricLengthMs(1000));
      assertSame(builder, builder.withCircuitBreakerSleepWindow(1000));
      assertSame(builder, builder.withCircuitBreakerEnabled(false));
   }
}
