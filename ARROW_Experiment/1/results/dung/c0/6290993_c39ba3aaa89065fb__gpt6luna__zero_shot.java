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
    assertNotNull(Writables.getPrimitiveType(String.class));
    assertNotNull(Writables.getPrimitiveType(Long.class));
    assertNotNull(Writables.getPrimitiveType(Integer.class));
    assertNotNull(Writables.getPrimitiveType(Float.class));
    assertNotNull(Writables.getPrimitiveType(Double.class));
    assertNotNull(Writables.getPrimitiveType(Boolean.class));
    assertNotNull(Writables.getPrimitiveType(ByteBuffer.class));
    assertEquals(null, Writables.getPrimitiveType(Void.class));
  }

  @Test
  public void primitiveTypeAccessorsReturnTheirTypes() {
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
  public void recordsReturnsRegisteredExtension() {
    WritableType<String, Text> registered = Writables.strings();
    Writables.register(String.class, registered);
    assertSame(registered, Writables.records(String.class));
  }

  @Test
  public void recordsAcceptsWritableClasses() {
    WritableType<IntWritable, IntWritable> type = Writables.records(IntWritable.class);
    assertNotNull(type);
    assertEquals(IntWritable.class, type.getTypeClass());
    assertEquals(IntWritable.class, type.getSerializationClass());
  }

  @Test(expected = IllegalArgumentException.class)
  public void recordsRejectsNonWritableClasses() {
    Writables.records(Object.class);
  }

  @Test
  public void writablesCreatesIdentityType() {
    WritableType<Text, Text> type = Writables.writables(Text.class);
    assertEquals(Text.class, type.getTypeClass());
    assertEquals(Text.class, type.getSerializationClass());
  }

  @Test
  public void tableOfAcceptsWritablePrimitiveTypes() {
    WritableTableType<String, Integer> table = Writables.tableOf(Writables.strings(), Writables.ints());
    assertNotNull(table);
    assertEquals(Writables.strings(), table.getKeyType());
    assertEquals(Writables.ints(), table.getValueType());
  }

  @Test
  public void pairsCreatesTupleType() {
    WritableType<?, ?> type = Writables.pairs(Writables.strings(), Writables.ints());
    assertEquals(org.apache.crunch.Pair.class, type.getTypeClass());
    assertEquals(TupleWritable.class, type.getSerializationClass());
    assertEquals(2, type.getSubTypes().size());
  }

  @Test
  public void triplesCreatesTupleType() {
    WritableType<?, ?> type = Writables.triples(Writables.strings(), Writables.ints(), Writables.longs());
    assertEquals(org.apache.crunch.Tuple3.class, type.getTypeClass());
    assertEquals(TupleWritable.class, type.getSerializationClass());
    assertEquals(3, type.getSubTypes().size());
  }

  @Test
  public void quadsCreatesTupleType() {
    WritableType<?, ?> type = Writables.quads(
        Writables.strings(), Writables.ints(), Writables.longs(), Writables.booleans());
    assertEquals(org.apache.crunch.Tuple4.class, type.getTypeClass());
    assertEquals(TupleWritable.class, type.getSerializationClass());
    assertEquals(4, type.getSubTypes().size());
  }

  @Test
  public void tuplesCreatesTupleNType() {
    WritableType<?, ?> type = Writables.tuples(Writables.strings(), Writables.ints());
    assertEquals(org.apache.crunch.TupleN.class, type.getTypeClass());
    assertEquals(TupleWritable.class, type.getSerializationClass());
    assertEquals(2, type.getSubTypes().size());
  }

  @Test
  public void unionOfCreatesUnionType() {
    PType<org.apache.crunch.Union> type = Writables.unionOf(Writables.strings(), Writables.ints());
    assertEquals(org.apache.crunch.Union.class, type.getTypeClass());
    assertEquals(UnionWritable.class, type.getSerializationClass());
    assertEquals(2, type.getSubTypes().size());
  }

  @Test
  public void collectionsCreatesCollectionType() {
    WritableType<Collection<String>, GenericArrayWritable> type = Writables.collections(Writables.strings());
    assertEquals(Collection.class, type.getTypeClass());
    assertEquals(GenericArrayWritable.class, type.getSerializationClass());
    assertEquals(1, type.getSubTypes().size());
  }

  @Test
  public void mapsCreatesMapType() {
    WritableType<Map<String, Integer>, org.apache.hadoop.io.MapWritable> type = Writables.maps(Writables.ints());
    assertEquals(Map.class, type.getTypeClass());
    assertEquals(TextMapWritable.class, type.getSerializationClass());
    assertEquals(1, type.getSubTypes().size());
  }

  @Test
  public void registerComparableAllowsSameClassAtSameCode() {
    Writables.registerComparable(IntWritable.class, 1000);
    Writables.registerComparable(IntWritable.class, 1000);
    assertTrue(true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void registerComparableRejectsCodeAlreadyAssignedToDifferentClass() {
    Writables.registerComparable(IntWritable.class, 1001);
    Writables.registerComparable(LongWritable.class, 1001);
  }
}
