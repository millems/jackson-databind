package tools.jackson.databind.ser.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for checking that overridden settings for
 * <code>JsonInclude</code> annotation property work
 * as expected.
 */
public class JsonIncludeOverrideTest
    extends DatabindTestUtil
{
    @JsonPropertyOrder({"list", "map"})
    static class EmptyListMapBean
    {
        public List<String> list = Collections.emptyList();

        public Map<String,String> map = Collections.emptyMap();
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonPropertyOrder({"num", "annotated", "plain"})
    static class MixedTypeAlwaysBean
    {
        @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
        public Integer num = null;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String annotated = null;

        public String plain = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"num", "annotated", "plain"})
    static class MixedTypeNonNullBean
    {
        @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
        public Integer num = null;

        @JsonInclude(JsonInclude.Include.ALWAYS)
        public String annotated = null;

        public String plain = null;
    }

    @Test
    public void testPropConfigOverridesForInclude() throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        // First, with defaults, both included:
        JsonIncludeOverrideTest.EmptyListMapBean empty = new JsonIncludeOverrideTest.EmptyListMapBean();
        assertEquals(a2q("{'list':[],'map':{}}"),
                mapper.writeValueAsString(empty));

        // and then change inclusion criteria for either
        mapper = jsonMapperBuilder()
                .withConfigOverride(Map.class,
                        o -> o.setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null)))
                .build();
        assertEquals(a2q("{'list':[]}"),
                mapper.writeValueAsString(empty));

        mapper = jsonMapperBuilder()
                .withConfigOverride(List.class,
                        o -> o.setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, null)))
                .build();
        assertEquals(a2q("{'map':{}}"),
                mapper.writeValueAsString(empty));
    }

    @Test
    public void testOverrideForIncludeAsPropertyNonNull() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // First, with defaults, all but NON_NULL annotated included
        JsonIncludeOverrideTest.MixedTypeAlwaysBean nullValues = new JsonIncludeOverrideTest.MixedTypeAlwaysBean();
        assertEquals(a2q("{'num':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));

        // and then change inclusion as property criteria for either
        mapper = jsonMapperBuilder()
                .withConfigOverride(String.class,
                        o -> o.setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null)))
                .build();
        assertEquals("{\"num\":null}",
                mapper.writeValueAsString(nullValues));

        mapper = jsonMapperBuilder()
                .withConfigOverride(Integer.class,
                        o -> o.setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null)))
                .build();
        assertEquals("{\"plain\":null}",
                mapper.writeValueAsString(nullValues));
    }

    @Test
    public void testOverrideForIncludeAsPropertyAlways() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // First, with defaults, only ALWAYS annotated included
        JsonIncludeOverrideTest.MixedTypeNonNullBean nullValues = new JsonIncludeOverrideTest.MixedTypeNonNullBean();
        assertEquals("{\"annotated\":null}",
                mapper.writeValueAsString(nullValues));

        // and then change inclusion as property criteria for either
        mapper = jsonMapperBuilder()
                .withConfigOverride(String.class,
                        o -> o.setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null)))
                .build();
        assertEquals(a2q("{'annotated':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));

        mapper = jsonMapperBuilder()
                .withConfigOverride(Integer.class,
                        o -> o.setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null)))
                .build();
        assertEquals(a2q("{'num':null,'annotated':null}"),
                mapper.writeValueAsString(nullValues));
    }

    @Test
    public void testOverridesForIncludeAndIncludeAsPropertyNonNull() throws Exception
    {
        // First, with ALWAYS override on containing bean, all included
        JsonIncludeOverrideTest.MixedTypeNonNullBean nullValues = new JsonIncludeOverrideTest.MixedTypeNonNullBean();
        ObjectMapper mapper = jsonMapperBuilder()
                .withConfigOverride(JsonIncludeOverrideTest.MixedTypeNonNullBean.class,
                        o -> o.setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null)))
                .build();
        assertEquals(a2q("{'num':null,'annotated':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));

        // and then change inclusion as property criteria for either
        mapper = jsonMapperBuilder()
                .withConfigOverride(JsonIncludeOverrideTest.MixedTypeNonNullBean.class,
                        o -> o.setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.ALWAYS, null)))
                .withConfigOverride(String.class,
                    o -> o.setIncludeAsProperty(JsonInclude.Value
                            .construct(JsonInclude.Include.NON_NULL, null)))
                .build();
        assertEquals(a2q("{'num':null,'annotated':null}"),
                mapper.writeValueAsString(nullValues));

        mapper = jsonMapperBuilder()
                .withConfigOverride(JsonIncludeOverrideTest.MixedTypeNonNullBean.class,
                        o -> o.setInclude(JsonInclude.Value
                                .construct(JsonInclude.Include.ALWAYS, null)))
                .withConfigOverride(Integer.class,
                    o -> o.setIncludeAsProperty(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null)))
            .build();
        assertEquals(a2q("{'annotated':null,'plain':null}"),
                mapper.writeValueAsString(nullValues));
    }

    @Test
    public void testOverridesForIncludeAndIncludeAsPropertyAlways() throws Exception
    {
        // First, with NON_NULL override on containing bean, empty
        JsonIncludeOverrideTest.MixedTypeAlwaysBean nullValues = new JsonIncludeOverrideTest.MixedTypeAlwaysBean();
        ObjectMapper mapper = jsonMapperBuilder()
                .withConfigOverride(JsonIncludeOverrideTest.MixedTypeAlwaysBean.class,
                        o -> o.setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null)))
                .build();
        assertEquals("{}",
                mapper.writeValueAsString(nullValues));

        // and then change inclusion as property criteria for either
        mapper = jsonMapperBuilder()
                .withConfigOverride(JsonIncludeOverrideTest.MixedTypeAlwaysBean.class,
                        o -> o.setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null)))
                .withConfigOverride(String.class,
                        o -> o.setIncludeAsProperty(JsonInclude.Value
                                .construct(JsonInclude.Include.ALWAYS, null)))
                .build();
        assertEquals("{\"plain\":null}",
                mapper.writeValueAsString(nullValues));

        mapper = jsonMapperBuilder()
                .withConfigOverride(JsonIncludeOverrideTest.MixedTypeAlwaysBean.class,
                        o -> o.setInclude(JsonInclude.Value
                        .construct(JsonInclude.Include.NON_NULL, null)))
                .withConfigOverride(Integer.class,
                        o -> o.setIncludeAsProperty(JsonInclude.Value
                                .construct(JsonInclude.Include.ALWAYS, null)))
                .build();
        assertEquals("{\"num\":null}",
                mapper.writeValueAsString(nullValues));
    }
}
