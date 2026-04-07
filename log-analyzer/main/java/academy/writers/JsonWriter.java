package academy.writers;

import academy.stats.AnalysisResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;

public class JsonWriter extends AbstractWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonWriter(BufferedWriter writer) {
        super(writer);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void write(AnalysisResult result) throws IOException {
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
