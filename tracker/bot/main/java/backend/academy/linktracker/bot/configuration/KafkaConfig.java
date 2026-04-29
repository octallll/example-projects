package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import java.io.IOException;
import java.lang.reflect.Type;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;

@Configuration
public class KafkaConfig {

    @Bean
    public RecordMessageConverter avroConverter() {
        return new StringJacksonJsonMessageConverter() {
            @Override
            public @NonNull Object extractAndConvertValue(@NonNull ConsumerRecord<?, ?> record, Type type) {
                byte[] data = (byte[]) record.value();

                try {
                    BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
                    SpecificDatumReader<LinkUpdateEvent> reader = new SpecificDatumReader<>(LinkUpdateEvent.class);
                    return reader.read(null, decoder);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
