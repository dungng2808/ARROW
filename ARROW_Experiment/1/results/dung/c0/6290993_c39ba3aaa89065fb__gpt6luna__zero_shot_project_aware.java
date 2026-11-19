package org.apache.crunch.types.writable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.crunch.Pair;
import org.apache.crunch.Tuple3;
import org.apache.crunch.Union;
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
  }

  @Test
  public void unknownPrimitiveTypeIsNull() {
    assertNull(Writables.getPrimitiveType(Object.class));
  }

  @Test
  public void primitiveFactoriesReturnTypes() {
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
  public void writableTypeUsesWritableClass() {
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
  public void registeredRecordTypeIsReturned() {
    WritableType<String, Text> type = Writables.strings();
    Writables.register(String.class, type);
    assertSame(type, Writables.records(String.class));
  }

  @Test
  public void pairTypeHasTupleWritableSerialization() {
    WritableType<Pair<String, Integer>, TupleWritable> type =
        Writables.pairs(Writables.strings(), Writables.ints());
    assertEquals(Pair.class, type.getTypeClass());
    assertEquals(TupleWritable.class, type.getSerializationClass());
  }

  @Test
  public void tripleTypeHasTupleWritableSerialization() {
    WritableType<Tuple3<String, Integer, Long>, TupleWritable> type =
        Writables.triples(Writables.strings(), Writables.ints(), Writables.longs());
    assertEquals(Tuple3.class, type.getTypeClass());
    assertEquals(TupleWritable.class, type.getSerializationClass());
  }

  @Test
  public void collectionTypeHasGenericArraySerialization() {
    WritableType<Collection<String>, GenericArrayWritable> type =
        Writables.collections(Writables.strings());
    assertEquals(Collection.class, type.getTypeClass());
    assertEquals(GenericArrayWritable.class, type.getSerializationClass());
  }

  @Test
  public void mapTypeHasTextMapSerialization() {
    WritableType<Map<String, Integer>, org.apache.hadoop.io.MapWritable> type =
        Writables.maps(Writables.ints());
    assertEquals(Map.class, type.getTypeClass());
    assertEquals(TextMapWritable.class, type.getSerializationClass());
  }

  @Test
  public void unionTypeHasUnionWritableSerialization() {
    PType<Union> type = Writables.unionOf(Writables.strings(), Writables.ints());
    assertEquals(Union.class, type.getTypeClass());
    assertEquals(UnionWritable.class, type.getSerializationClass());
  }

  @Test
  public void tableTypeAcceptsWritableKeyAndValue() {
    WritableTableType<String, Integer> type =
        Writables.tableOf(Writables.strings(), Writables.ints());
    assertNotNull(type);
  }

  @Test
  public void tableTypeRejectsNonWritableKey() {
    try {
      Writables.tableOf(null, Writables.ints());
    } catch (IllegalArgumentException expected) {
      assertEquals("Key type must be of class WritableType", expected.getMessage());
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void tableTypeRejectsNonWritableValue() {
    try {
      Writables.tableOf(Writables.strings(), null);
    } catch (IllegalArgumentException expected) {
      assertEquals("Value type must be of class WritableType", expected.getMessage());
      return;
    }
    throw new AssertionError("Expected IllegalArgumentException");
  }

  @Test
  public void stringOutputMapFnCreatesText() {
    Text text = Writables.strings().getOutputMapFn().map("hello");
    assertEquals("hello", text.toString());
  }

  @Test
  public void stringInputMapFnReadsText() {
    String value = Writables.strings().getInputMapFn().map(new Text("hello"));
    assertEquals("hello", value);
  }

  @Test
  public void integerOutputMapFnCreatesWritable() {
    IntWritable value = Writables.ints().getOutputMapFn().map(Integer.valueOf(17));
    assertEquals(17, value.get());
  }

  @Test
  public void integerInputMapFnReadsWritable() {
    Integer value = Writables.ints().getInputMapFn().map(new IntWritable(17));
    assertEquals(Integer.valueOf(17), value);
  }

  @Test
  public void longOutputMapFnCreatesWritable() {
    LongWritable value = Writables.longs().getOutputMapFn().map(Long.valueOf(23L));
    assertEquals(23L, value.get());
  }

  @Test
  public void floatOutputMapFnCreatesWritable() {
    FloatWritable value = Writables.floats().getOutputMapFn().map(Float.valueOf(2.5f));
    assertEquals(2.5f, value.get(), 0.0f);
  }

  @Test
  public void doubleOutputMapFnCreatesWritable() {
    DoubleWritable value = Writables.doubles().getOutputMapFn().map(Double.valueOf(3.5d));
    assertEquals(3.5d, value.get(), 0.0d);
  }

  @Test
  public void booleanOutputMapFnCreatesWritable() {
    BooleanWritable value = Writables.booleans().getOutputMapFn().map(Boolean.TRUE);
    assertTrue(value.get());
  }

  @Test
  public void byteBufferOutputMapFnCreatesBytesWritable() {
    ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 4, 5, 6 });
    BytesWritable value = Writables.bytes().getOutputMapFn().map(buffer);
    assertEquals(3, value.getLength());
    assertEquals(4, value.getBytes()[0]);
    assertEquals(5, value.getBytes()[1]);
    assertEquals(6, value.getBytes()[2]);
  }

  @Test
  public void byteBufferInputMapFnUsesWritableLength() {
    BytesWritable writable = new BytesWritable(new byte[] { 7, 8, 9 });
    ByteBuffer value = Writables.bytes().getInputMapFn().map(writable);
    assertEquals(3, value.remaining());
    assertEquals((byte) 7, value.get(0));
    assertEquals((byte) 8, value.get(1));
    assertEquals((byte) 9, value.get(2));
  }

  @Test
  public void emptyCollectionCanBeMappedToWritableArray() {
    Collection<String> values = new ArrayList<String>();
    GenericArrayWritable writable = Writables.collections(Writables.strings())
        .getOutputMapFn().map(values);
    assertNotNull(writable);
    assertEquals(0, writable.get().length);
  }

  @Test
  public void emptyMapCanBeMappedToTextMapWritable() {
    Map<String, Integer> values = new HashMap<String, Integer>();
    TextMapWritable writable = Writables.maps(Writables.ints()).getOutputMapFn().map(values);
    assertNotNull(writable);
    assertTrue(writable.isEmpty());
  }
}
