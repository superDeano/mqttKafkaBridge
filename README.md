# Mqtt Kafka Bridge

Bridge built on top of Spring to consume MQTT messages and republish them to Kafka.
The bridge periodically gets Kafka topics and subscribes to the same topics with the MQTT broker.

## Deployment
A docker image can be built to run in a container.
Just open a terminal in the root folder of the project and use the following commands.

To build the docker image

    $ docker build -t {image:tag} . 

To push the docker image to docker hub

    $ docker push {image:tag}

## Usage

The following environment variables need to be specified in order for the bridge to work:

    server.port                                 : Port the bridge will run on
    spring.kafka.producer.bootstrap-servers     : Kafka connection URL
    mqtt.servers                                : MQTT Broker connection URL


Because the bridge is a spring application, it can also be controlled using HTTP Requests. The following APIs are available:

    GET /bridge/health                          : Shows the health of the bridge
    GET /bridge/reconnectToMqtt                 : Reconnect to MQTT broker

The Health API returns an HTTP response of `200` if everything is fine and `500` or `Internal Server Error` which is useful for periodically checking the health of a pod like with GKE.


### Advanced Code
The bridge can be modified further to match a special use case regarding Kafka and MQTT Topics and the payloads.