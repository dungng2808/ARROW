package org.apache.crunch.types.writable;

import com.google.common.collect.ImmutableMap;
import org.apache.crunch.PType;
import org.apache.crunch.Tuple;
import org.apache.crunch.Tuple3;
import org.apache.crunch.Tuple4;
import org.apache.crunch.TupleN;
import org.apache.crunch.Union;
import org.apache.crunch.fn.IdentityFn;
import org.apache.crunch.types.PTypes;
import org.apache.crunch.types.TupleFactory;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class Writables_ArrowTest {

    @Test
    public void testRegisterComparable() {
        // Test registering a new comparable class
        Writables.registerComparable(TestWritable.class);
        
        // Verify that the class was registered
        assertTrue(Writables.WRITABLE_CODES.containsValue(TestWritable.class));
    }

    @Test
    public void testRegisterComparableWithCode() {
        // Test registering a comparable class with a specific code
        Writables.registerComparable(TestWritable2.class, 100);
        
        // Verify that the class was registered with the correct code
        assertEquals(TestWritable2.class, Writables.WRITABLE_CODES.get(100));
    }

    @Test
    public void testGetPrimitiveType() {
        // Test getting primitive types
        PType<String> stringType = Writables.getPrimitiveType(String.class);
        assertNotNull(stringType);
        assertEquals(Text.class, stringType.getSerializationClass());
        
        PType<Integer> intType = Writables.getPrimitiveType(Integer.class);
        assertNotNull(intType);
        assertEquals(IntWritable.class, intType.getSerializationClass());
        
        PType<Long> longType = Writables.getPrimitiveType(Long.class);
        assertNotNull(longType);
        assertEquals(LongWritable.class, longType.getSerializationClass());
        
        PType<Float> floatType = Writables.getPrimitiveType(Float.class);
        assertNotNull(floatType);
        assertEquals(FloatWritable.class, floatType.getSerializationClass());
        
        PType<Double> doubleType = Writables.getPrimitiveType(Double.class);
        assertNotNull(doubleType);
        assertEquals(DoubleWritable.class, doubleType.getSerializationClass());
        
        PType<Boolean> booleanType = Writables.getPrimitiveType(Boolean.class);
        assertNotNull(booleanType);
        assertEquals(BooleanWritable.class, booleanType.getSerializationClass());
        
        PType<ByteBuffer> byteBufferType = Writables.getPrimitiveType(ByteBuffer.class);
        assertNotNull(byteBufferType);
        assertEquals(BytesWritable.class, byteBufferType.getSerializationClass());
    }

    @Test
    public void testRegister() {
        // Test registering a custom type
        WritableType<TestRecord, TestWritable> customType = new WritableType<>(TestRecord.class, TestWritable.class,
                IdentityFn.getInstance(), IdentityFn.getInstance());
        Writables.register(TestRecord.class, customType);
        
        // Verify that the type was registered
        assertEquals(customType, Writables.records(TestRecord.class));
    }

    @Test
    public void testNulls() {
        // Test nulls() method
        WritableType<Void, NullWritable> nullType = Writables.nulls();
        assertNotNull(nullType);
        assertEquals(Void.class, nullType.getTypeClass());
        assertEquals(NullWritable.class, nullType.getSerializationClass());
    }

    @Test
    public void testStrings() {
        // Test strings() method
        WritableType<String, Text> stringType = Writables.strings();
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getTypeClass());
        assertEquals(Text.class, stringType.getSerializationClass());
    }

    @Test
    public void testLongs() {
        // Test longs() method
        WritableType<Long, LongWritable> longType = Writables.longs();
        assertNotNull(longType);
        assertEquals(Long.class, longType.getTypeClass());
        assertEquals(LongWritable.class, longType.getSerializationClass());
    }

    @Test
    public void testInts() {
        // Test ints() method
        WritableType<Integer, IntWritable> intType = Writables.ints();
        assertNotNull(intType);
        assertEquals(Integer.class, intType.getTypeClass());
        assertEquals(IntWritable.class, intType.getSerializationClass());
    }

    @Test
    public void testFloats() {
        // Test floats() method
        WritableType<Float, FloatWritable> floatType = Writables.floats();
        assertNotNull(floatType);
        assertEquals(Float.class, floatType.getTypeClass());
        assertEquals(FloatWritable.class, floatType.getSerializationClass());
    }

    @Test
    public void testDoubles() {
        // Test doubles() method
        WritableType<Double, DoubleWritable> doubleType = Writables.doubles();
        assertNotNull(doubleType);
        assertEquals(Double.class, doubleType.getTypeClass());
        assertEquals(DoubleWritable.class, doubleType.getSerializationClass());
    }

    @Test
    public void testBooleans() {
        // Test booleans() method
        WritableType<Boolean, BooleanWritable> booleanType = Writables.booleans();
        assertNotNull(booleanType);
        assertEquals(Boolean.class, booleanType.getTypeClass());
        assertEquals(BooleanWritable.class, booleanType.getSerializationClass());
    }

    @Test
    public void testBytes() {
        // Test bytes() method
        WritableType<ByteBuffer, BytesWritable> byteType = Writables.bytes();
        assertNotNull(byteType);
        assertEquals(ByteBuffer.class, byteType.getTypeClass());
        assertEquals(BytesWritable.class, byteType.getSerializationClass());
    }

    @Test
    public void testRecordsWithWritableClass() {
        // Test records() with a Writable subclass
        WritableType<TestWritable, TestWritable> writableType = Writables.records(TestWritable.class);
        assertNotNull(writableType);
        assertEquals(TestWritable.class, writableType.getTypeClass());
        assertEquals(TestWritable.class, writableType.getSerializationClass());
    }

    @Test
    public void testRecordsWithNonWritableClass() {
        // Test records() with a non-Writable class should throw IllegalArgumentException
        try {
            Writables.records(String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testWritables() {
        // Test writables() method
        WritableType<TestWritable, TestWritable> writableType = Writables.writables(TestWritable.class);
        assertNotNull(writableType);
        assertEquals(TestWritable.class, writableType.getTypeClass());
        assertEquals(TestWritable.class, writableType.getSerializationClass());
    }

    @Test
    public void testPairs() {
        // Test pairs() method
        PType<String> stringType = Writables.strings();
        PType<Integer> intType = Writables.ints();
        WritableType<org.apache.crunch.Pair<String, Integer>, TupleWritable> pairType = Writables.pairs(stringType, intType);
        assertNotNull(pairType);
        assertEquals(org.apache.crunch.Pair.class, pairType.getTypeClass());
        assertEquals(TupleWritable.class, pairType.getSerializationClass());
    }

    @Test
    public void testTriples() {
        // Test triples() method
        PType<String> stringType = Writables.strings();
        PType<Integer> intType = Writables.ints();
        PType<Long> longType = Writables.longs();
        WritableType<Tuple3<String, Integer, Long>, TupleWritable> tripleType = Writables.triples(stringType, intType, longType);
        assertNotNull(tripleType);
        assertEquals(Tuple3.class, tripleType.getTypeClass());
        assertEquals(TupleWritable.class, tripleType.getSerializationClass());
    }

    @Test
    public void testQuads() {
        // Test quads() method
        PType<String> stringType = Writables.strings();
        PType<Integer> intType = Writables.ints();
        PType<Long> longType = Writables.longs();
        PType<Float> floatType = Writables.floats();
        WritableType<Tuple4<String, Integer, Long, Float>, TupleWritable> quadType = Writables.quads(stringType, intType, longType, floatType);
        assertNotNull(quadType);
        assertEquals(Tuple4.class, quadType.getTypeClass());
        assertEquals(TupleWritable.class, quadType.getSerializationClass());
    }

    @Test
    public void testTuples() {
        // Test tuples() method with PType array
        PType<String> stringType = Writables.strings();
        PType<Integer> intType = Writables.ints();
        PType<Long> longType = Writables.longs();
        WritableType<TupleN, TupleWritable> tupleType = Writables.tuples(stringType, intType, longType);
        assertNotNull(tupleType);
        assertEquals(TupleN.class, tupleType.getTypeClass());
        assertEquals(TupleWritable.class, tupleType.getSerializationClass());
    }

    @Test
    public void testUnionOf() {
        // Test unionOf() method
        PType<String> stringType = Writables.strings();
        PType<Integer> intType = Writables.ints();
        PType<Union> unionType = Writables.unionOf(stringType, intType);
        assertNotNull(unionType);
        assertEquals(Union.class, unionType.getTypeClass());
        assertEquals(UnionWritable.class, unionType.getSerializationClass());
    }

    @Test
    public void testCollections() {
        // Test collections() method
        PType<String> stringType = Writables.strings();
        WritableType<Collection<String>, GenericArrayWritable> collectionType = Writables.collections(stringType);
        assertNotNull(collectionType);
        assertEquals(Collection.class, collectionType.getTypeClass());
        assertEquals(GenericArrayWritable.class, collectionType.getSerializationClass());
    }

    @Test
    public void testMaps() {
        // Test maps() method
        PType<String> stringType = Writables.strings();
        WritableType<Map<String, String>, TextMapWritable> mapType = Writables.maps(stringType);
        assertNotNull(mapType);
        assertEquals(Map.class, mapType.getTypeClass());
        assertEquals(TextMapWritable.class, mapType.getSerializationClass());
    }

    @Test
    public void testJsons() {
        // Test jsons() method
        PType<TestRecord> jsonType = Writables.jsons(TestRecord.class);
        assertNotNull(jsonType);
        assertEquals(TestRecord.class, jsonType.getTypeClass());
    }

    // Helper classes for testing
    private static class TestWritable implements Writable {
        @Override
        public void write(java.io.DataOutput out) throws IOException {
        }

        @Override
        public void readFields(java.io.DataInput in) throws IOException {
        }
    }

    private static class TestWritable2 implements Writable {
        @Override
        public void write(java.io.DataOutput out) throws IOException {
        }

        @Override
        public void readFields(java.io.DataInput in) throws IOException {
        }
    }

    private static class TestRecord {
        private String name;
        private int value;

        public TestRecord() {}

        public TestRecord(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
}
