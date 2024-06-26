package tools.jackson.databind.deser.builder;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BuilderWithUnwrappedSingleArray2608Test
{
    // [databind#2608]
    @JsonDeserialize(builder = ExamplePOJO2608.ExamplePOJOBuilder.class)
    static class ExamplePOJO2608 {
      public int id;
      public POJOValue2608 value;

      public ExamplePOJO2608(int id, POJOValue2608 value) {
          this.id = id;
          this.value = value;
      }

      @JsonPOJOBuilder(withPrefix = "")
      static class ExamplePOJOBuilder {
          int id;
          POJOValue2608 value;

          public ExamplePOJOBuilder id(int i) {
              this.id = i;
              return this;
          }

          public ExamplePOJOBuilder value(POJOValue2608 v) {
              this.value = v;
              return this;
          }

          public ExamplePOJO2608 build() {
              return new ExamplePOJO2608(id, value);
          }
      }
    }

    // [databind#2608]
    @JsonDeserialize(builder = POJOValue2608.POJOValueBuilder.class)
    static class POJOValue2608 {
        public String subValue;

        public POJOValue2608(String s) {
            subValue = s;
        }

      @JsonPOJOBuilder(withPrefix = "")
      public static class POJOValueBuilder {
          String v;

          public POJOValueBuilder subValue(String s) {
              v = s;
              return this;
          }

          public POJOValue2608 build() {
              return new POJOValue2608(v);
          }
      }
    }

    // [databind#2608]
    @Test
    public void testDeserializationAndFail() throws Exception {
        final ObjectMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .build();

// Regular POJO would work:
//        final String serialized = "{\"value\": {\"subValue\": \"123\"}}";
        final String serialized = "{\"value\": [ {\"subValue\": \"123\"} ]}";
        final ExamplePOJO2608 result = mapper.readValue(serialized, ExamplePOJO2608.class);
        assertNotNull(result);
    }
}
