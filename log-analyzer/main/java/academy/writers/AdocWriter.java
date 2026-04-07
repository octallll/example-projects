package academy.writers;

import academy.stats.AnalysisResult;
import academy.stats.DateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import java.io.BufferedWriter;
import java.io.IOException;

public class AdocWriter extends AbstractWriter {
    public AdocWriter(BufferedWriter writer) {
        super(writer);
    }

    @Override
    public void write(AnalysisResult result) throws IOException {
        writer.write("=== Общая информация%n%n");
        writer.write("[options=\"header\"]%n");
        writer.write("|=================%n");
        writer.write("| Метрика | Значение %n");

        writer.write(String.format("| Файл(-ы) | `%s` %n", String.join(", ", result.files())));
        writer.write(String.format("| Количество запросов | %,d %n", result.totalRequestsCount()));
        writer.write(String.format(
                "| Средний размер ответа | %.2fb %n",
                result.responseSizeInBytes().average()));
        writer.write(String.format(
                "| 95-й перцентиль размера ответа | %.2fb %n",
                result.responseSizeInBytes().p95()));
        writer.write(String.format(
                "| Максимальный размер ответа | %.2fb %n",
                result.responseSizeInBytes().max()));

        writer.write("|=================%n%n");

        if (!result.resources().isEmpty()) {
            writer.write("=== Запрашиваемые ресурсы%n%n");
            writer.write("[options=\"header\"]%n");
            writer.write("|=================%n");
            writer.write("| Ресурс | Количество запросов %n");

            for (ResourceStat resource : result.resources()) {
                String resourceName = resource.resource().length() > 50
                        ? resource.resource().substring(0, 47) + "..."
                        : resource.resource();
                writer.write(String.format("| `%s` | %,d %n", resourceName, resource.totalRequestsCount()));
            }

            writer.write("|=================%n%n");
        }

        if (!result.responseCodes().isEmpty()) {
            writer.write("=== Коды ответа%n%n");
            writer.write("[options=\"header\"]%n");
            writer.write("|=================%n");
            writer.write("| Код | Описание | Количество %n");

            for (ResponseCodeStat codeStat : result.responseCodes()) {
                String description = HttpStatus.getDescriptionOrDefault(codeStat.code(), "Unknown");
                writer.write(String.format(
                        "| %d | %s | %,d %n", codeStat.code(), description, codeStat.totalResponsesCount()));
            }

            writer.write("|=================%n%n");
        }

        if (!result.requestsPerDate().isEmpty()) {
            writer.write("=== Распределение запросов по датам%n%n");
            writer.write("[options=\"header\"]%n");
            writer.write("|====================================%n");
            writer.write("| Дата | День недели | Количество | Процент %n");

            for (DateStat dateStat : result.requestsPerDate()) {
                writer.write(String.format(
                        "| %s | %s | %,d | %.2f%% %n",
                        dateStat.date(),
                        dateStat.weekday(),
                        dateStat.totalRequestsCount(),
                        dateStat.totalRequestsPercentage()));
            }

            writer.write("|====================================%n%n");
        }

        if (!result.uniqueProtocols().isEmpty()) {
            writer.write("=== Уникальные протоколы%n%n");
            writer.write("[options=\"header\"]%n");
            writer.write("|=================%n");
            writer.write("| Протокол %n");

            for (String protocol : result.uniqueProtocols()) {
                writer.write(String.format("| %s %n", protocol));
            }

            writer.write("|=================%n%n");
        }

        writer.flush();
        writer.close();
    }
}
