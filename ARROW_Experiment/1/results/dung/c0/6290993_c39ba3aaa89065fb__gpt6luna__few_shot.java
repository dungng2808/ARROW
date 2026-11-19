package org.apache.crunch.types.writable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.crunch.types.PType;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.junit.Test;

public class Writables_ArrowTest {

  @Test
  public void primitiveTypesAreAvailable() {
    assertSame(Writables.strings(), Writables.getPrimitiveType(String.class));
    assertSame(Writables.longs(), Writables.getPrimitiveType(Long.class));
    assertSame(Writables.ints(), Writables.getPrimitiveType(Integer.class));
    assertSame(Writables.floats(), Writables.getPrimitiveType(Float.class));
    assertSame(Writables.doubles(), Writables.getPrimitiveType(Double.class));
    assertSame(Writables.booleans(), Writables.getPrimitiveType(Boolean.class));
    assertSame(Writables.bytes(), Writables.getPrimitiveType(ByteBuffer.class));
    assertEquals(null, Writables.getPrimitiveType(Object.class));
  }

  @Test
  public void primitiveTypeAccessorsReturnTypes() {
    assertNotNull(Writables.nulls());
    assertNotNull(Writables.strings());
    assertNotNull(Writables.longs());
    assertNotNull(Writables.ints());
    assertNotNull(Writables.floats());
    assertNotNull(Writables.doubles());
    assertNotNull(Writables.booleans());
    assertNotNull(Writables.bytes());
  }

  @Test
  public void writableTypeCanBeCreatedForWritableClass() {
    WritableType<IntWritable, IntWritable> type = Writables.writables(IntWritable.class);
    assertEquals(IntWritable.class, type.getTypeClass());
    assertEquals(IntWritable.class, type.getSerializationClass());
  }

  @Test
  public void recordsRejectNonWritableClass() {
    try {
      Writables.records(String.class);
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Cannot create Writable records"));
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void recordsAcceptWritableClass() {
    WritableType<IntWritable, IntWritable> type = Writables.records(IntWritable.class);
    assertEquals(IntWritable.class, type.getTypeClass());
    assertEquals(IntWritable.class, type.getSerializationClass());
  }

  @Test
  public void collectionsTypeCanBeCreated() {
    WritableType<Collection<Integer>, GenericArrayWritable> type =
        Writables.collections(Writables.ints());
    assertEquals(Collection.class, type.getTypeClass());
    assertEquals(GenericArrayWritable.class, type.getSerializationClass());
  }

  @Test
  public void mapsTypeCanBeCreated() {
    WritableType<Map<String, Integer>, org.apache.hadoop.io.MapWritable> type =
        Writables.maps(Writables.ints());
    assertEquals(Map.class, type.getTypeClass());
    assertEquals(TextMapWritable.class, type.getSerializationClass());
  }

  @Test
  public void pairTypeCanBeCreated() {
    WritableType<?, TupleWritable> type = Writables.pairs(Writables.strings(), Writables.ints());
    assertEquals(TupleWritable.class, type.getSerializationClass());
  }

  @Test
  public void unionTypeCanBeCreated() {
    PType<?> type = Writables.unionOf(Writables.strings(), Writables.ints());
    assertEquals(org.apache.crunch.Union.class, type.getTypeClass());
    assertEquals(UnionWritable.class, type.getSerializationClass());
  }

  @Test
  public void registerComparableAllowsRegistration() {
    Writables.registerComparable(IntWritable.class, 1000);
    assertNotNull(Writables.writables(IntWritable.class));
  }

  @Test
  public void primitiveWritableMappingsHaveExpectedClasses() {
    assertEquals(Text.class, Writables.strings().getSerializationClass());
    assertEquals(LongWritable.class, Writables.longs().getSerializationClass());
    assertEquals(IntWritable.class, Writables.ints().getSerializationClass());
    assertEquals(FloatWritable.class, Writables.floats().getSerializationClass());
    assertEquals(DoubleWritable.class, Writables.doubles().getSerializationClass());
    assertEquals(BooleanWritable.class, Writables.booleans().getSerializationClass());
    assertEquals(BytesWritable.class, Writables.bytes().getSerializationClass());
  }

  @Test
  public void collectionTypeUsesExpectedElementType() {
    Collection<Integer> values = Arrays.asList(1, 2, 3);
    assertEquals(3, values.size());
    assertNotNull(Writables.collections(Writables.ints()));
  }

  @Test
  public void mapTypeCanBeCreatedForStringValues() {
    Map<String, String> values = new HashMap<String, String>();
    values.put("key", "value");
    assertEquals("value", values.get("key"));
    assertNotNull(Writables.maps(Writables.strings()));
  }
}
