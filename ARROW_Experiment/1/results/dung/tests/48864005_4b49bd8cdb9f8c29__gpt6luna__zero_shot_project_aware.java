package com.homeadvisor.robusto;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class ApiCommand_ArrowTest
{
   @Test
   public void builderReturnsBuilderAndBuildsCommand()
   {
      ApiCommand.Builder<String> builder = ApiCommand.<String>builder()
            .withCommandGroup("test-group")
            .withUriProvider(callback -> "result")
            .withRemoteServiceCallback(null);

      assertNotNull(builder);
   }

   @Test
   public void builderRejectsMissingUriProvider()
   {
      ApiCommand.Builder<String> builder = ApiCommand.<String>builder()
            .withRemoteServiceCallback(null);

      try
      {
         builder.build();
         fail("Expected missing URI provider to be rejected");
      }
      catch(IllegalArgumentException expected)
      {
         assertEquals("UriProvider cannot be null", expected.getMessage());
      }
   }

   @Test
   public void builderRejectsMissingRemoteServiceCallback()
   {
      ApiCommand.Builder<String> builder = ApiCommand.<String>builder()
            .withUriProvider(callback -> "result");

      try
      {
         builder.build();
         fail("Expected missing callback to be rejected");
      }
      catch(IllegalArgumentException expected)
      {
         assertEquals("RemoteServiceCallback cannot be null", expected.getMessage());
      }
   }

   @Test
   public void builderMethodsReturnSameBuilder()
   {
      ApiCommand.Builder<String> builder = ApiCommand.<String>builder();

      assertSame(builder, builder.withUriProvider(callback -> "result"));
      assertSame(builder, builder.withRemoteServiceCallback(null));
      assertSame(builder, builder.withCommandGroup("group"));
      assertSame(builder, builder.withNumberOfRetries(2));
      assertSame(builder, builder.withBackoffPolicy(null));
      assertSame(builder, builder.withRetryListener(null));
      assertSame(builder, builder.withCommandCache(null, null));
      assertSame(builder, builder.withExceptionRetryPolicy(Exception.class, true));
      assertSame(builder, builder.withHystrixCommandProperties(HystrixCommandProperties.Setter()));
      assertSame(builder, builder.withHystrixThreadProperties(HystrixThreadPoolProperties.Setter()));
   }
}
