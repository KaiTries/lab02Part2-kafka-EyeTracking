package com.examples;


import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerForGazeEventsForSingleEyeTrackerCustomOffset {
    public static void main(String[] args) throws IOException, ParseException {

        // initial settings
        KafkaConsumer<String, Object> consumer;
        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            consumer = new KafkaConsumer<>(properties);
        }

        // Read specific topic and partition
        TopicPartition topicPartition = new TopicPartition("gaze-events", 0);
        consumer.assign(Arrays.asList(topicPartition));

        // get consumer latest offset
        long latestoffset = consumer.position(topicPartition);

        System.out.println("latest offset: "+latestoffset);

        // seek to offsetToReadFrom
        int offsetToReadFrom = 10;
        // check that offsetToReadFrom<latestoffset
        if(offsetToReadFrom<latestoffset) {
            consumer.seek(topicPartition, offsetToReadFrom);
        }
       else {
            System.err.println("offsetToReadFrom ("+offsetToReadFrom+") not yet reached");
        }


        while (true) {

            // pool new data
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(8));

            // process consumer records depending on record.topic() and record.value()
            for (ConsumerRecord<String, Object> record : records) {
                // switch/case
                switch (record.topic()) {
                    //note: record.value() is a linkedHashMap (see utils.JavaDeserializer), use can use the following syntax to access specific attributes ((LinkedHashMap) record.value()).get("ATTRIBUTENAME").toString(); The object can be also reconstructed as Gaze object
                    case "gaze-events":
                        String value =   record.value().toString();
                        System.out.println("Received gaze-events - key: " + record.key() +"- value: " + value + "- partition: "+record.partition());
                        break;


                    default:
                        throw new IllegalStateException("Shouldn't be possible to get message on topic " + record.topic());
                }
            }


        }
    }

}
