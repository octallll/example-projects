package academy.writers;

import academy.stats.AnalysisResult;
import academy.stats.DateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import java.io.BufferedWriter;
import java.io.IOException;

public class MarkdownWriter extends AbstractWriter {
    public MarkdownWriter(BufferedWriter writer) {
        super(writer);
    }

    @Override
    public void write(AnalysisResult result) throws IOException {
        writer.write(String.format("#### Общая информация%n%n"));
        writer.write(String.format("|        Метрика        |     Значение |%n"));
        writer.write(String.format("|:---------------------:|-------------:|%n"));

        writer.write(String.format("|       Файл(-ы)        | `%s` |%n", String.join(", ", result.files())));
        writer.write(String.format("|  Количество запросов  |       %,d |%n", result.totalRequestsCount()));
        writer.write(String.format(
                "| Средний размер ответа |         %.2fb |%n",
                result.responseSizeInBytes().average()));
        writer.write(String.format(
                "|  95p размера ответа   |         %.2fb |%n",
                result.responseSizeInBytes().p95()));
        writer.write(String.format(
                "|  Макс. размер ответа  |         %.2fb |%n",
                result.responseSizeInBytes().max()));

        writer.write("%n#### Запрашиваемые ресурсы%n%n");
        writer.write("|     Ресурс      | Количество |%n");
        writer.write("|:---------------:|-----------:|%n");

        for (ResourceStat resource : result.resources()) {
            writer.write(String.format(
                    "|  `%s`  |      %,d |%n",
                    resource.resource().length() > 30
                            ? resource.resource().substring(0, 27) + "..."
                            : resource.resource(),
                    resource.totalRequestsCount()));
        }

        writer.write(String.format("%n#### Коды ответа%n%n"));
        writer.write(String.format("| Код |          Имя          | Количество |%n"));
        writer.write(String.format("|:---:|:---------------------:|-----------:|%n"));

        for (ResponseCodeStat codeStat : result.responseCodes()) {
            String name = HttpStatus.getDescriptionOrDefault(codeStat.code(), "Unknown");
            writer.write(String.format("| %d | %s | %,d |%n", codeStat.code(), name, codeStat.totalResponsesCount()));
        }

        if (!result.requestsPerDate().isEmpty()) {
            writer.write("%n#### Распределение запросов по датам%n%n");
            writer.write("|     Дата      | День недели | Количество | Процент |%n");
            writer.write("|:-------------:|:-----------:|-----------:|--------:|%n");

            for (DateStat dateStat : result.requestsPerDate()) {
                writer.write(String.format(
                        "| %s | %s | %,d | %.2f%% |%n",
                        dateStat.date(),
                        dateStat.weekday(),
                        dateStat.totalRequestsCount(),
                        dateStat.totalRequestsPercentage()));
            }
        }

        if (!result.uniqueProtocols().isEmpty()) {
            writer.write("%n#### Уникальные протоколы%n%n");
            writer.write("| Протокол |%n");
            writer.write("|:--------:|%n");

            for (String protocol : result.uniqueProtocols()) {
                writer.write(String.format("| %s |%n", protocol));
            }
        }

        writer.flush();
        writer.close();
    }
}
