package tools.jackson.databind.jsontype.ext;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.*;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.testutil.DatabindTestUtil;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalTypeIdWithCreatorTest extends DatabindTestUtil
{
    // [databind#999]

    public static interface Payload999 { }

    @JsonTypeName("foo")
    public static class FooPayload999 implements Payload999 { }

    @JsonTypeName("bar")
    public static class BarPayload999 implements Payload999 { }

    public static class Message<P extends Payload999>
    {
        final String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
                visible = true,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
                @JsonSubTypes.Type(FooPayload999.class),
                @JsonSubTypes.Type(BarPayload999.class) })
        final P payload;

        @JsonCreator
        public Message(@JsonProperty("type") String type,
                @JsonProperty("payload") P payload)
        {
            this.type = type;
            this.payload = payload;
        }
    }

    // [databind#1198]

    public enum Attacks { KICK, PUNCH }

    static class Character {
        public String name;
        public Attacks preferredAttack;

        @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, defaultImpl=Kick.class,
                include=JsonTypeInfo.As.EXTERNAL_PROPERTY, property="preferredAttack")
        @JsonSubTypes({
            @JsonSubTypes.Type(value=Kick.class, name="KICK"),
            @JsonSubTypes.Type(value=Punch.class, name="PUNCH")
        })
        public Attack attack;
    }

    public static abstract class Attack {
        public String side;

        protected Attack(String side) {
            this.side = side;
        }
    }

    public static class Kick extends Attack {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Kick(String side) {
            super(side);
        }
    }

    public static class Punch extends Attack {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public Punch(String side) {
            super(side);
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    private final ObjectMapper MAPPER = newJsonMapper();

    // [databind#999]
    @Test
    public void testExternalTypeId() throws Exception
    {
        TypeReference<Message<FooPayload999>> type = new TypeReference<Message<FooPayload999>>() { };

        Message<?> msg = MAPPER.readValue(a2q("{ 'type':'foo', 'payload': {} }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);

        // and then with different order
        msg = MAPPER.readValue(a2q("{'payload': {}, 'type':'foo' }"), type);
        assertNotNull(msg);
        assertNotNull(msg.payload);
        assertEquals("foo", msg.type);
    }

    // [databind#1198]
    @Test
    public void test1198Fails() throws Exception {
        String json = "{ \"name\": \"foo\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }

    // [databind#1198]
    @Test
    public void test1198Works() throws Exception {
        String json = "{ \"name\": \"foo\", \"preferredAttack\": \"KICK\", \"attack\":\"right\" } }";

        Character character = MAPPER.readValue(json, Character.class);

        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }
}
