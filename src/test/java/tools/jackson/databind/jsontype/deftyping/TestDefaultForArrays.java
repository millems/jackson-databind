package tools.jackson.databind.jsontype.deftyping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.*;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.testutil.DatabindTestUtil;
import tools.jackson.databind.testutil.NoCheckSubTypeValidator;

import static org.junit.jupiter.api.Assertions.*;

public class TestDefaultForArrays extends DatabindTestUtil
{
    static class ArrayBean {
        public Object[] values;

        public ArrayBean() { this(null); }
        public ArrayBean(Object[] v) { values = v; }
    }

    static class PrimitiveArrayBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Object stuff;

        protected PrimitiveArrayBean() { }
        public PrimitiveArrayBean(Object value) { stuff = value; }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    /**
     * Simple unit test for verifying that we get String array
     * back, even though declared type is Object array
     */
    @Test
    public void testArrayTypingSimple() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.NON_CONCRETE_AND_ARRAYS)
                .build();
        ArrayBean bean = new ArrayBean(new String[0]);
        String json = mapper.writeValueAsString(bean);
        ArrayBean result = mapper.readValue(json, ArrayBean.class);
        assertNotNull(result.values);
        assertEquals(String[].class, result.values.getClass());
    }

    // And let's try it with deeper array as well
    @Test
    public void testArrayTypingNested() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.NON_CONCRETE_AND_ARRAYS)
                .build();
        ArrayBean bean = new ArrayBean(new String[0][0]);
        String json = mapper.writeValueAsString(bean);
        ArrayBean result = mapper.readValue(json, ArrayBean.class);
        assertNotNull(result.values);
        assertEquals(String[][].class, result.values.getClass());
    }

    @Test
    public void testNodeInArray() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.JAVA_LANG_OBJECT)
                .build();
        JsonNode node = mapper.readTree("{\"a\":3}");
        Object[] obs = new Object[] { node };
        String json = mapper.writeValueAsString(obs);
        Object[] result = mapper.readValue(json, Object[].class);
        assertEquals(1, result.length);
        Object ob = result[0];
        assertTrue(ob instanceof JsonNode);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testNodeInEmptyArray() throws Exception
    {
        Map<String, List<String>> outerMap = new HashMap<String, List<String>>();
        outerMap.put("inner", new ArrayList<String>());
        ObjectMapper vanillaMapper = jsonMapperBuilder()
                .disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
                .build();
        JsonNode tree = vanillaMapper.convertValue(outerMap, JsonNode.class);

        String json = vanillaMapper.writeValueAsString(tree);
        assertEquals("{}", json);

        JsonNode node = vanillaMapper.readTree("{\"a\":[]}");

        ObjectMapper mapper = vanillaMapper.rebuild()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.JAVA_LANG_OBJECT)
                .build();
        Object[] obs = new Object[] { node };
        json = mapper.writeValueAsString(obs);
        Object[] result = mapper.readValue(json, Object[].class);

        assertEquals(1, result.length);
        Object elem = result[0];
        assertTrue(elem instanceof ObjectNode);
        assertEquals(0, ((ObjectNode) elem).size());
    }

    @Test
    public void testArraysOfArrays() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .activateDefaultTyping(NoCheckSubTypeValidator.instance,
                        DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
                .build();

        Object value = new Object[][] { new Object[] {} };
        String json = mapper.writeValueAsString(value);

        // try with different (but valid) nominal types:
        _testArraysAs(mapper, json, Object[][].class);
        _testArraysAs(mapper, json, Object[].class);
        _testArraysAs(mapper, json, Object.class);
    }

    @Test
    public void testArrayTypingForPrimitiveArrays() throws Exception
    {
        final ObjectMapper mapper = jsonMapperBuilder()
                .polymorphicTypeValidator(new NoCheckSubTypeValidator())
                .build();
        _testArrayTypingForPrimitiveArrays(mapper, new int[] { 1, 2, 3 });
        _testArrayTypingForPrimitiveArrays(mapper, new long[] { 1, 2, 3 });
        _testArrayTypingForPrimitiveArrays(mapper, new short[] { 1, 2, 3 });
        _testArrayTypingForPrimitiveArrays(mapper, new double[] { 0.5, 5.5, -1.0 });
        _testArrayTypingForPrimitiveArrays(mapper, new float[] { 0.5f, 5.5f, -1.0f });
        _testArrayTypingForPrimitiveArrays(mapper, new boolean[] { true, false });
        _testArrayTypingForPrimitiveArrays(mapper, new byte[] { 1, 2, 3 });

        _testArrayTypingForPrimitiveArrays(mapper, new char[] { 'a', 'b' });
    }

    private void _testArrayTypingForPrimitiveArrays(ObjectMapper mapper, Object v) throws Exception {
        PrimitiveArrayBean input = new PrimitiveArrayBean(v);
        String json = mapper.writeValueAsString(input);
        PrimitiveArrayBean result = mapper.readValue(json, PrimitiveArrayBean.class);
        assertNotNull(result.stuff);
        assertSame(v.getClass(), result.stuff.getClass());
    }

    /*
    /**********************************************************
    /* Helper methods
    /**********************************************************
     */

    protected void _testArraysAs(ObjectMapper mapper, String json, Class<?> type)
        throws Exception
    {
        Object o = mapper.readValue(json, type);
        assertNotNull(o);
        assertTrue(o instanceof Object[]);
        Object[] main = (Object[]) o;
        assertEquals(1, main.length);
        Object element = main[0];
        assertNotNull(element);
        assertTrue(element instanceof Object[]);
        assertEquals(0, ((Object[]) element).length);
    }
}
