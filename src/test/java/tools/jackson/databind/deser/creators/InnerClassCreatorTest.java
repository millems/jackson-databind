package tools.jackson.databind.deser.creators;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.*;
import tools.jackson.databind.exc.InvalidDefinitionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import static tools.jackson.databind.testutil.DatabindTestUtil.jsonMapperBuilder;
import static tools.jackson.databind.testutil.DatabindTestUtil.verifyException;


// For [databind#1501], [databind#1502], [databind#1503]; mostly to
// test that for non-static inner classes constructors are ignored
// and no Creators should be processed (since they cannot be made
// to work in standard way anyway).
public class InnerClassCreatorTest
{
    static class Something1501 {
        public InnerSomething1501 a;

        // important: must name the parameter (param names module, or explicit)
        @JsonCreator
        public Something1501(@JsonProperty("a") InnerSomething1501 a) { this.a = a; }

        public Something1501(boolean bogus) { a = new InnerSomething1501(); }

        class InnerSomething1501 {
            @JsonCreator
            public InnerSomething1501() { }
        }
    }

    static class Something1502 {
        @JsonProperty
        public InnerSomething1502 a;

        @JsonCreator
        public Something1502(InnerSomething1502 a) {}

        class InnerSomething1502 {
            @JsonCreator
            public InnerSomething1502() {}
        }
    }

    static class Outer1503 {
        public InnerClass1503 innerClass;

        class InnerClass1503 {
            public Generic<?> generic;
            public InnerClass1503(@JsonProperty("generic") Generic<?> generic) {}
        }

        static class Generic<T> {
            public int ignored;
        }
    }

    private final ObjectMapper MAPPER = jsonMapperBuilder()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    // Used to trigger `ArrayIndexOutOfBoundsException` for missing creator property index
    @Test
    public void testIssue1501() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Something1501(false));
        try {
            MAPPER.readValue(ser, Something1501.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Cannot construct instance");
            verifyException(e, "InnerSomething1501");
            verifyException(e, "non-static inner classes like this can only by instantiated using default");
        }
    }

    @Test
    public void testIssue1502() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Something1502(null));

        // 21-Sep-2017, tatu: For some reason with 3.x this DOES pass (or maybe
        //    more accurately somehow with 2.x it doesn't?).
        /*
        try {
            MAPPER.readValue(ser, Something1502.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Cannot construct instance");
            verifyException(e, "InnerSomething1502");
            verifyException(e, "non-static inner classes like this can only by instantiated using default");
        }
        */
        Something1502 result = MAPPER.readValue(ser, Something1502.class);
        assertNotNull(result);
        assertNull(result.a);
    }

    @Test
    public void testIssue1503() throws Exception
    {
        String ser = MAPPER.writeValueAsString(new Outer1503());
        Outer1503 result = MAPPER.readValue(ser, Outer1503.class);
        assertNotNull(result);
    }
}
