.. _using:

Using MQTT Kafka Bridge
===========================

This section will be about the requirements to running and interacting with the bridge.

* :ref:`Requirements <require>`
* :ref:`Interacting <interact>`

.. _require:

Requirements
----------------------------

The following are parameters that need to be mentionned and included in the environment variables of the container so the application can connect to the appropriate kafka and MQTT clusters.

+-----------------------------------------+-------------------------------+
| Parameters                              | Description                   |
+-----------------------------------------+-------------------------------+
| server.port                             | : Port the bridge will run on |
+-----------------------------------------+-------------------------------+
| spring.kafka.producer.bootstrap-servers | : Kafka connection url        |
+-----------------------------------------+-------------------------------+
| mqtt.servers                            | : MQTT broker connection url  |
+-----------------------------------------+-------------------------------+


.. _interact:

Interacting
----------------------------

The bridge can be controlled and checked using http endpoints.
They are mostly http ``GET`` so they can easily be run from a web browser.

They can be found below:

+---------------------------------+----------------------------------+
| Endpoint                        | Description                      |
+---------------------------------+----------------------------------+
| ``GET`` /bridge/health          | : Shows the health of the bridge |
+---------------------------------+----------------------------------+
| ``GET`` /bridge/reconnectToMqtt | : Reconnect to MQTT broker       |
+---------------------------------+----------------------------------+
