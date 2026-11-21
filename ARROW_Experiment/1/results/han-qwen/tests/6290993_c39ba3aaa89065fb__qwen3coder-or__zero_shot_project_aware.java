package org.apache.crunch.types.writable;

import com.google.common.collect.ImmutableMap;
import org.apache.crunch.MapFn;
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
import org.apache.hadoop.io.WritableComparable;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;

public class Writables_ArrowTest {

    @Before
    public void setUp() {
        // Reset the WRITABLE_CODES to initial state before each test
        Writables.WRITABLE_CODES = com.google.common.collect.HashBiMap.create(
            com.google.common.collect.ImmutableBiMap.<Integer, Class<? extends Writable>>builder()
                .put(1, BytesWritable.class)
                .put(2, Text.class)
                .put(3, IntWritable.class)
                .put(4, LongWritable.class)
                .put(5, FloatWritable.class)
                .put(6, DoubleWritable.class)
                .put(7, BooleanWritable.class)
                .put(8, TupleWritable.class)
                .put(9, TextMapWritable.class)
                .put(10, UnionWritable.class)
                .build());
    }

    @Test
    public void testRegisterComparableWithUniqueCode() {
        Class<? extends WritableComparable> clazz = Text.class;
        int code = 11;
        Writables.registerComparable(clazz, code);
        assertEquals(clazz, Writables.WRITABLE_CODES.get(code));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterComparableWithDuplicateCodeDifferentClass() {
        Class<? extends WritableComparable> clazz1 = Text.class;
        Class<? extends WritableComparable> clazz2 = IntWritable.class;
        int code = 11;
        Writables.registerComparable(clazz1, code);
        Writables.registerComparable(clazz2, code);
    }

    @Test
    public void testRegisterComparableWithSameClassAndCode() {
        Class<? extends WritableComparable> clazz = Text.class;
        int code = 11;
        Writables.registerComparable(clazz, code);
        Writables.registerComparable(clazz, code); // Should not throw exception
        assertEquals(clazz, Writables.WRITABLE_CODES.get(code));
    }

    @Test
    public void testRegisterComparableAutoCodeGeneration() {
        Class<? extends WritableComparable> clazz = Text.class;
        Writables.registerComparable(clazz);
        int code = clazz.hashCode();
        if (code < 0) {
            code = -code;
        }
        if (code < Writables.WRITABLE_CODES.size()) {
            code += Writables.WRITABLE_CODES.size();
        }
        assertEquals(clazz, Writables.WRITABLE_CODES.get(code));
    }

    @Test
    public void testSerializeWritableComparableCodes() throws IOException {
        Configuration conf = new Configuration();
        Writables.serializeWritableComparableCodes(conf);
        assertNotNull(conf.get("crunch.writable.comparable.codes"));
    }

    @Test
    public void testReloadWritableComparableCodes() throws Exception {
        Configuration conf = new Configuration();
        Writables.serializeWritableComparableCodes(conf);
        Writables.reloadWritableComparableCodes(conf);
        assertNotNull(conf.get("crunch.writable.comparable.codes"));
    }

    @Test
    public void testGetPrimitiveType() {
        assertEquals(Writables.nulls(), Writables.getPrimitiveType(Void.class));
        assertEquals(Writables.strings(), Writables.getPrimitiveType(String.class));
        assertEquals(Writables.longs(), Writables.getPrimitiveType(Long.class));
        assertEquals(Writables.ints(), Writables.getPrimitiveType(Integer.class));
        assertEquals(Writables.floats(), Writables.getPrimitiveType(Float.class));
        assertEquals(Writables.doubles(), Writables.getPrimitiveType(Double.class));
        assertEquals(Writables.booleans(), Writables.getPrimitiveType(Boolean.class));
        assertEquals(Writables.bytes(), Writables.getPrimitiveType(ByteBuffer.class));
    }

    @Test
    public void testRegisterExtension() {
        WritableType<String, Text> customType = new WritableType<>(String.class, Text.class,
            new MapFn<Text, String>() {
                @Override
                public String map(Text input) {
                    return input.toString();
                }
            },
            new MapFn<String, Text>() {
                @Override
                public Text map(String input) {
                    return new Text(input);
                }
            });
        Writables.register(String.class, customType);
        assertEquals(customType, Writables.records(String.class));
    }

    @Test
    public void testRecordsWithWritableClass() {
        WritableType<BytesWritable, BytesWritable> result = Writables.records(BytesWritable.class);
        assertEquals(BytesWritable.class, result.getTypeClass());
        assertEquals(BytesWritable.class, result.getSerializationClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecordsWithNonWritableClass() {
        Writables.records(String.class);
    }

    @Test
    public void testWritables() {
        WritableType<Text, Text> result = Writables.writables(Text.class);
        assertEquals(Text.class, result.getTypeClass());
        assertEquals(Text.class, result.getSerializationClass());
        assertEquals(IdentityFn.getInstance(), result.getInputMapFn());
        assertEquals(IdentityFn.getInstance(), result.getOutputMapFn());
    }

    @Test
    public void testPairs() {
        PType<Integer> intType = Writables.ints();
        PType<String> stringType = Writables.strings();
        WritableType<org.apache.crunch.Pair<Integer, String>, TupleWritable> result = Writables.pairs(intType, stringType);
        assertEquals(org.apache.crunch.Pair.class, result.getTypeClass());
        assertEquals(TupleWritable.class, result.getSerializationClass());
    }

    @Test
    public void testTriples() {
        PType<Integer> intType = Writables.ints();
        PType<String> stringType = Writables.strings();
        PType<Double> doubleType = Writables.doubles();
        WritableType<Tuple3<Integer, String, Double>, TupleWritable> result = Writables.triples(intType, stringType, doubleType);
        assertEquals(Tuple3.class, result.getTypeClass());
        assertEquals(TupleWritable.class, result.getSerializationClass());
    }

    @Test
    public void testQuads() {
        PType<Integer> intType = Writables.ints();
        PType<String> stringType = Writables.strings();
        PType<Double> doubleType = Writables.doubles();
        PType<Boolean> booleanType = Writables.booleans();
        WritableType<Tuple4<Integer, String, Double, Boolean>, TupleWritable> result = Writables.quads(intType, stringType, doubleType, booleanType);
        assertEquals(Tuple4.class, result.getTypeClass());
        assertEquals(TupleWritable.class, result.getSerializationClass());
    }

    @Test
    public void testTuples() {
        PType<Integer> intType = Writables.ints();
        PType<String> stringType = Writables.strings();
        PType<Double> doubleType = Writables.doubles();
        WritableType<TupleN, TupleWritable> result = Writables.tuples(intType, stringType, doubleType);
        assertEquals(TupleN.class, result.getTypeClass());
        assertEquals(TupleWritable.class, result.getSerializationClass());
    }

    @Test
    public void testUnionOf() {
        PType<Integer> intType = Writables.ints();
        PType<String> stringType = Writables.strings();
        PType<Union> result = Writables.unionOf(intType, stringType);
        assertEquals(Union.class, result.getTypeClass());
        assertEquals(UnionWritable.class, ((WritableType)result).getSerializationClass());
    }

    @Test
    public void testDerived() {
        PType<Integer> intType = Writables.ints();
        MapFn<Integer, String> inputFn = new MapFn<Integer, String>() {
            @Override
            public String map(Integer input) {
                return input.toString();
            }
        };
        MapFn<String, Integer> outputFn = new MapFn<String, Integer>() {
            @Override
            public Integer map(String input) {
                return Integer.valueOf(input);
            }
        };
        PType<String> result = Writables.derived(String.class, inputFn, outputFn, intType);
        assertEquals(String.class, result.getTypeClass());
    }

    @Test
    public void testDerivedImmutable() {
        PType<Integer> intType = Writables.ints();
        MapFn<Integer, String> inputFn = new MapFn<Integer, String>() {
            @Override
            public String map(Integer input) {
                return input.toString();
            }
        };
        MapFn<String, Integer> outputFn = new MapFn<String, Integer>() {
            @Override
            public Integer map(String input) {
                return Integer.valueOf(input);
            }
        };
        PType<String> result = Writables.derivedImmutable(String.class, inputFn, outputFn, intType);
        assertEquals(String.class, result.getTypeClass());
    }

    @Test
    public void testCollections() {
        PType<Integer> intType = Writables.ints();
        WritableType<Collection<Integer>, GenericArrayWritable> result = Writables.collections(intType);
        assertEquals(Collection.class, result.getTypeClass());
        assertEquals(GenericArrayWritable.class, result.getSerializationClass());
    }

    @Test
    public void testMaps() {
        PType<Integer> intType = Writables.ints();
        WritableType<Map<String, Integer>, TextMapWritable> result = Writables.maps(intType);
        assertEquals(Map.class, result.getTypeClass());
        assertEquals(TextMapWritable.class, result.getSerializationClass());
    }

    @Test
    public void testJsons() {
        PType<String> result = Writables.jsons(String.class);
        assertEquals(String.class, result.getTypeClass());
    }

    @Test
    public void testAsBytesWritable() {
        Text text = new Text("test");
        BytesWritable result = Writables.asBytesWritable(text);
        assertEquals(text.getBytes(), result.getBytes());
        assertEquals(text.getLength(), result.getLength());
    }

    @Test
    public void testCreateSameClass() {
        Text text = new Text("test");
        Text result = Writables.create(Text.class, text);
        assertEquals(text, result);
    }

    @Test
    public void testCreateDifferentClass() throws IOException {
        Text text = new Text("test");
        BytesWritable bytes = new BytesWritable();
        bytes.set(text.getBytes(), 0, text.getLength());
        IntWritable result = Writables.create(IntWritable.class, bytes);
        assertEquals(0, result.get()); // Default value for IntWritable
    }

    @Test
    public void testTWTupleMapFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.TWTupleMapFn fn = new Writables.TWTupleMapFn(TupleFactory.PAIR, intType, intType);
        fn.initialize();
        assertNotNull(fn.values);
    }

    @Test
    public void testTupleTWMapFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.TupleTWMapFn fn = new Writables.TupleTWMapFn(intType, intType);
        fn.initialize();
        assertNotNull(fn.written);
        assertNotNull(fn.values);
    }

    @Test
    public void testUWInputFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.UWInputFn fn = new Writables.UWInputFn(intType);
        fn.initialize();
    }

    @Test
    public void testUWOutputFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.UWOutputFn fn = new Writables.UWOutputFn(intType);
        fn.initialize();
    }

    @Test
    public void testArrayCollectionMapFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.ArrayCollectionMapFn<Integer> fn = new Writables.ArrayCollectionMapFn<>(intType.getOutputMapFn());
        fn.initialize();
    }

    @Test
    public void testCollectionArrayMapFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.CollectionArrayMapFn<Integer> fn = new Writables.CollectionArrayMapFn<>(intType.getOutputMapFn());
        fn.initialize();
    }

    @Test
    public void testMapInputMapFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.MapInputMapFn<Integer> fn = new Writables.MapInputMapFn<>(IntWritable.class, intType.getInputMapFn());
        fn.initialize();
    }

    @Test
    public void testMapOutputMapFnInitialize() {
        WritableType<Integer, IntWritable> intType = Writables.ints();
        Writables.MapOutputMapFn<Integer> fn = new Writables.MapOutputMapFn<>(intType.getOutputMapFn());
        fn.initialize();
    }
}
