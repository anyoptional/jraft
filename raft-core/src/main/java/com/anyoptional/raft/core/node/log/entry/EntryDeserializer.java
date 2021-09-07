package com.anyoptional.raft.core.node.log.entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

import static com.anyoptional.raft.core.node.log.entry.Entry.KIND_GENERAL;
import static com.anyoptional.raft.core.node.log.entry.Entry.KIND_NO_OP;

public class EntryDeserializer extends JsonDeserializer<Entry> {

    @Override
    public Entry deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        int kind = node.get("kind").asInt();
        int index = node.get("index").asInt();
        int term = node.get("term").asInt();
        switch (kind) {
            case KIND_NO_OP: {
                return new NoOpEntry(index, term);
            }
            case KIND_GENERAL: {
                byte[] commandBytes = node.get("commandBytes").binaryValue();
                return new GeneralEntry(index, term, commandBytes);
            }
            default:
                throw new IOException("unsupported entry kind");
        }
    }

}
