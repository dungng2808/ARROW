package org.apache.crunch.types.writable;

import com.google.common.collect.ImmutableBiMap;
import org.apache.crunch.PType;
import org.apache.crunch.Tuple;
import org.apache.crunch.Tuple3;
import org.apache.crunch.Tuple4;
import org.apache.crunch.TupleN;
import org.apache.crunch.Union;
import org.apache.crunch.fn.IdentityFn;
import org.apache.crunch.types.WritableTypeFamily;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

public class Writables_ArrowTest {

  @Test
  public void testRegisterComparable() {
    // Test basic registration
    Writables.registerComparable(BooleanWritable.class);
    assertNotNull(Writables.WRITABLE_CODES.inverse().get(BooleanWritable.class));
  }

  @Test
  public void testRegisterComparableWithCode() {
    // Test registration with specific code
    Writables.registerComparable(BooleanWritable.class, 100);
    assertEquals(BooleanWritable.class, Writables.WRITABLE_CODES.get(100));
  }

  @Test
  public void testSerializeWritableComparableCodes() throws IOException {
    Configuration conf = new Configuration();
    Writables.serializeWritableComparableCodes(conf);
    assertNotNull(conf.get(Writables.WRITABLE_COMPARABLE_CODES));
  }

  @Test
  public void testReloadWritableComparableCodes() throws Exception {
    Configuration conf = new Configuration();
    Writables.serializeWritableComparableCodes(conf);
    Writables.reloadWritableComparableCodes(conf);
    assertNotNull(conf.get(Writables.WRITABLE_COMPARABLE_CODES));
  }

  @Test
  public void testGetPrimitiveType() {
    // Test primitive types
    assertEquals(Writables.strings(), Writables.getPrimitiveType(String.class));
    assertEquals(Writables.longs(), Writables.getPrimitiveType(Long.class));
    assertEquals(Writables.ints(), Writables.getPrimitiveType(Integer.class));
    assertEquals(Writables.floats(), Writables.getPrimitiveType(Float.class));
    assertEquals(Writables.doubles(), Writables.getPrimitiveType(Double.class));
    assertEquals(Writables.booleans(), Writables.getPrimitiveType(Boolean.class));
    assertEquals(Writables.bytes(), Writables.getPrimitiveType(ByteBuffer.class));
  }

  @Test
  public void testRegister() {
    // Test registering custom type
    WritableType<String, Text> customType = Writables.writables(Text.class);
    Writables.register(String.class, customType);
    assertEquals(customType, Writables.getPrimitiveType(String.class));
  }

  @Test
  public void testNulls() {
    assertEquals(Writables.nulls, Writables.nulls());
  }

  @Test
  public void testStrings() {
    assertEquals(Writables.strings, Writables.strings());
  }

  @Test
  public void testLongs() {
    assertEquals(Writables.longs, Writables.longs());
  }

  @Test
  public void testInts() {
    assertEquals(Writables.ints, Writables.ints());
  }

  @Test
  public void testFloats() {
    assertEquals(Writables.floats, Writables.floats());
  }

  @Test
  public void testDoubles() {
    assertEquals(Writables.doubles, Writables.doubles());
  }

  @Test
  public void testBooleans() {
    assertEquals(Writables.booleans, Writables.booleans());
  }

  @Test
  public void testBytes() {
    assertEquals(Writables.bytes, Writables.bytes());
  }

  @Test
  public void testRecords() {
    // Test records with Writable class
    WritableType<Text, Text> textType = Writables.records(Text.class);
    assertEquals(Text.class, textType.getTypeClass());
    assertEquals(Text.class, textType.getSerializationClass());
  }

  @Test
  public void testWritables() {
    // Test writables method
    WritableType<Text, Text> textType = Writables.writables(Text.class);
    assertEquals(Text.class, textType.getTypeClass());
    assertEquals(Text.class, textType.getSerializationClass());
    assertEquals(IdentityFn.getInstance(), textType.getInputMapFn());
    assertEquals(IdentityFn.getInstance(), textType.getOutputMapFn());
  }

  @Test
  public void testPairs() {
    PType<String> stringType = Writables.strings();
    PType<Integer> intType = Writables.ints();
    PType<org.apache.crunch.Pair<String, Integer>> pairType = Writables.pairs(stringType, intType);
    assertEquals(org.apache.crunch.Pair.class, pairType.getTypeClass());
    assertEquals(TupleWritable.class, pairType.getSerializationClass());
  }

  @Test
  public void testTriples() {
    PType<String> stringType = Writables.strings();
    PType<Integer> intType = Writables.ints();
    PType<Double> doubleType = Writables.doubles();
    PType<Tuple3<String, Integer, Double>> tripleType = Writables.triples(stringType, intType, doubleType);
    assertEquals(Tuple3.class, tripleType.getTypeClass());
    assertEquals(TupleWritable.class, tripleType.getSerializationClass());
  }

  @Test
  public void testQuads() {
    PType<String> stringType = Writables.strings();
    PType<Integer> intType = Writables.ints();
    PType<Double> doubleType = Writables.doubles();
    PType<Boolean> booleanType = Writables.booleans();
    PType<Tuple4<String, Integer, Double, Boolean>> quadType = Writables.quads(stringType, intType, doubleType, booleanType);
    assertEquals(Tuple4.class, quadType.getTypeClass());
    assertEquals(TupleWritable.class, quadType.getSerializationClass());
  }

  @Test
  public void testTuples() {
    PType<String> stringType = Writables.strings();
    PType<Integer> intType = Writables.ints();
    PType<TupleN> tupleType = Writables.tuples(stringType, intType);
    assertEquals(TupleN.class, tupleType.getTypeClass());
    assertEquals(TupleWritable.class, tupleType.getSerializationClass());
  }

  @Test
  public void testUnionOf() {
    PType<String> stringType = Writables.strings();
    PType<Integer> intType = Writables.ints();
    PType<Union> unionType = Writables.unionOf(stringType, intType);
    assertEquals(Union.class, unionType.getTypeClass());
    assertEquals(UnionWritable.class, unionType.getSerializationClass());
  }

  @Test
  public void testDerived() {
    PType<String> stringType = Writables.strings();
    PType<Integer> intType = Writables.ints();
    PType<Integer> derivedType = Writables.derived(Integer.class, new org.apache.crunch.MapFn<String, Integer>() {
      @Override
      public Integer map(String input) {
        return input.length();
      }
    }, new org.apache.crunch.MapFn<Integer, String>() {
      @Override
      public String map(Integer input) {
        return String.valueOf(input);
      }
    }, stringType);
    assertEquals(Integer.class, derivedType.getTypeClass());
    assertEquals(Text.class, derivedType.getSerializationClass());
  }

  @Test
  public void testCollections() {
    PType<String> stringType = Writables.strings();
    PType<Collection<String>> collectionType = Writables.collections(stringType);
    assertEquals(Collection.class, collectionType.getTypeClass());
    assertEquals(GenericArrayWritable.class, collectionType.getSerializationClass());
  }

  @Test
  public void testMaps() {
    PType<String> stringType = Writables.strings();
    PType<Map<String, String>> mapType = Writables.maps(stringType);
    assertEquals(Map.class, mapType.getTypeClass());
    assertEquals(TextMapWritable.class, mapType.getSerializationClass());
  }

  @Test
  public void testJsons() {
    PType<String> jsonType = Writables.jsons(String.class);
    assertNotNull(jsonType);
  }

  @Test
  public void testAsBytesWritable() {
    Text text = new Text("test");
    BytesWritable result = Writables.asBytesWritable(text);
    assertEquals(text.getBytes(), result.getBytes());
    assertEquals(text.getLength(), result.getLength());
  }

  @Test
  public void testCreate() {
    Text text = new Text("test");
    BytesWritable bytes = new BytesWritable();
    bytes.set(text.getBytes(), 0, text.getLength());
    Text result = Writables.create(Text.class, bytes);
    assertEquals(text.toString(), result.toString());
  }
}
