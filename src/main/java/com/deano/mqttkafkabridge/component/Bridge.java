package com.deano.mqttkafkabridge.component;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.paho.client.mqttv3.*;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.Properties;
import java.util.stream.Collectors;

@Component
@EnableAsync
/*
 * Class that houses the business logic for the bridge to talk with Kafka and MQTT
 */
public class Bridge implements MqttCallback {
    private final String mqttConnection;
    private final String kafkaConnection;
    private final int oneMinute = 60000;
    private Logger logger;
    public static MqttAsyncClient mqtt;
    private static AdminClient kafkaAdmin;
    public static Producer<String, String> kafkaProducer;

    /**
     * Public Constructor for the class Bridge
     * Takes an Environment object to retrieve arguments from the environment variables
     * */
    public Bridge(Environment environment) {
        this.kafkaConnection = environment.getProperty("spring.kafka.producer.bootstrap-servers");
        this.mqttConnection = environment.getProperty("mqtt.servers");
        setUpLogger();
        connect();
        Thread periodicThread = new Thread(periodicallyGetKafkaTopics);
        periodicThread.start();
    }

    /**
     * Function that sets up the logger that outputs logs
     */
    private void setUpLogger() {
        this.logger = Logger.getLogger(this.getClass().getName());
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setTarget(ConsoleAppender.SYSTEM_OUT);
        consoleAppender.activateOptions();
        consoleAppender.setLayout(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
        this.logger.addAppender(consoleAppender);
    }

    Runnable periodicallyGetKafkaTopics = new Runnable() {

        @Override
        public void run() {
            logger.info("Started Thread to periodically check and subscribe to kafka topics");
            while (true) {
                try {
                    String[] topics = kafkaAdmin
                            .listTopics()
                            .names()
                            .get()
                            .toArray(String[]::new);

                    logger.log(Level.INFO, "Got topics");
                    if (topics != null && topics.length > 0) {
                        subcribeToTopics(topics);
                    }
                    logger.trace("Thread to check new Topics going to sleep");
                    Thread.sleep(oneMinute);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
    };


    public void connect() {
        try {
            mqtt = new MqttAsyncClient(mqttConnection, "MqttKafkaBridge");
            mqtt.setCallback(this);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            IMqttToken token = mqtt.connect(connOpts);
            Properties props = new Properties();
            props.put("bootstrap.servers", kafkaConnection);
            kafkaAdmin = KafkaAdminClient.create(props);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            kafkaProducer = new KafkaProducer<>(props);
            token.waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static void reconnect() throws MqttException {
        IMqttToken token = mqtt.connect();
        token.waitForCompletion();
    }


    @Async
    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("Lost connection to MQTT server", cause);
        while (true) {
            try {
                logger.info("Attempting to reconnect to MQTT server");
                reconnect();
                logger.info("Reconnected to MQTT server, resuming");
                return;
            } catch (MqttException e) {
                logger.warn("Reconnect failed, retrying in 10 seconds", e);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        byte[] payload = message.getPayload();
        logger.log(Level.INFO, "Message Arrived");

        kafkaProducer.send((new ProducerRecord<>(topic, new String(payload))));
    }


    private void subcribeToTopics(String[] topics) {
        int[] qos = new int[topics.length];
        try {
            IMqttToken token = mqtt.subscribe(topics, qos);
            token.waitForCompletion();
        } catch (MqttException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {
            logger.info("DELIVERY COMPLETE " + iMqttDeliveryToken.getMessage().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResponseEntity<String> testForHealth() {
        if (mqtt == null || kafkaProducer == null || kafkaAdmin == null)
            return new ResponseEntity<>("Kafka or Mqtt is null", HttpStatus.INTERNAL_SERVER_ERROR);
        try {
            mqtt.checkPing(null, null);
            kafkaProducer.metrics();
            kafkaAdmin.describeCluster();
        } catch (MqttException e) {
            return new ResponseEntity<>("Mqtt Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Kafka Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Everything seems fine!\nMqtt Connection: " + mqtt.isConnected(), HttpStatus.OK);
    }

    public static ResponseEntity<String> reconnectToMqttBroker() {
        try {
            reconnect();
        } catch (Exception e) {
            return new ResponseEntity<>("Could not reconnect\n" + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>("Successfully reconnected!\n", HttpStatus.OK);
    }
}
