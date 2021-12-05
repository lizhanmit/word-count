package com.zhandev;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class App {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");  // equivalent to 127.0.0.1:9092
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> wordCountInput = builder.stream("word-count-input");

        KTable<String, Long> wordCount = wordCountInput.mapValues(textLine -> textLine.toLowerCase())  // equivalent to .mapValues(String::toLowerCase)
            .flatMapValues(value -> Arrays.asList(value.split(" ")))
            .selectKey((key, value) -> value)
            .groupByKey()
            .count("Counts");

        wordCount.to(Serdes.String(), Serdes.Long(), "word-count-output");

        KafkaStreams streams = new KafkaStreams(builder, properties);
        streams.start();

        // print topology
        System.out.println(streams.toString());

        // add shutdown hook to correctly close the streams app
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
