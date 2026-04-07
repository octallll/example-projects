package academy.writers;

import academy.stats.AnalysisResult;
import java.io.IOException;

public interface Writer {
    void write(AnalysisResult result) throws IOException;
}
