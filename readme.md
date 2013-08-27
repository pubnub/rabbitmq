{\rtf1\ansi\ansicpg1252\cocoartf1187\cocoasubrtf390
{\fonttbl\f0\fswiss\fcharset0 Helvetica;\f1\fswiss\fcharset0 ArialMT;}
{\colortbl;\red255\green255\blue255;\red38\green38\blue38;\red26\green26\blue26;}
\margl1440\margr1440\vieww10800\viewh8400\viewkind0
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural

\f0\fs24 \cf0 PubNub RabbitMQ\
==============\
This is an adapter layer using java to connect a RabbitMQ deployment with the PubNub real-time network.  RabbitMQ provides message broker middleware to add scaleability, availability, reliability, and failover features behind the firewall.  PubNub provides a real-time network with these same characteristics in the cloud.  Combining the two provides an end-to-end solution with resiliency and flexibility for pub/sub use-cases between backend servers and clients in the cloud.\
\
#Installation\
This demo assumes that you have a working RabbitMQ installation using java.  This is very straightforward to set up by following the [RabbitMQ tutorial one java]({\field{\*\fldinst{HYPERLINK "http://www.rabbitmq.com/tutorials/tutorial-one-java.html"}}{\fldrslt http://www.rabbitmq.com/tutorials/tutorial-one-java.html}}) and [RabbitMQ tutorial two java]({\field{\*\fldinst{HYPERLINK "http://www.rabbitmq.com/tutorials/tutorial-two-java.html"}}{\fldrslt http://www.rabbitmq.com/tutorials/tutorial-two-java.html}}).\
\
Once the two tutorials are working, you can plug in PubNub's adapter to connect your pub/sub model behind the firewall with PubNub's real-time network.  \
\
1. [PubNub libraries]({\field{\*\fldinst{HYPERLINK "https://github.com/pubnub/rabbitmq/tree/master/lib"}}{\fldrslt https://github.com/pubnub/rabbitmq/tree/master/lib}}) - drop the required PubNub java libraries into the directory you used for the RabbitMQ tutorials\
2. [Adapter jar file]({\field{\*\fldinst{HYPERLINK "https://github.com/pubnub/rabbitmq/blob/master/jar/PubNub-RabbitMQ.jar"}}{\fldrslt https://github.com/pubnub/rabbitmq/blob/master/jar/PubNub-RabbitMQ.jar}}) - drop the RabbitMQ adapter jar file into the directory you used for the RabbitMQ tutorials\
\
#Description\
This adapter demo takes the two class files from the second RabbitMQ java tutorial and changes the channel name so that the message producer and message consular use different channels.  That is really the only change required to the RabbitMQ classes.  As far as your backend servers are concerned, they are just producing and consuming messages to/from RabbitMQ as usual, with no notion of PubNub.\
\
The adapters connect the RabbitMQ queue/exchange with PubNub.  One adapter will take messages from the queue and publish them to PubNub.  The other adapter will take messages from PubNub and send them to RabbitMQ.\
\
This is a good time to define the various components:\
* RabbitMQ - The middleware layer incorporating the message exchange and queue to handle delivery of messages.  The message exchange/broker/queue are all referred to together as RabbitMQ for the purposes of this demo\
* [ProducerClient]({\field{\*\fldinst{HYPERLINK "https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ProducerClient.java"}}{\fldrslt https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ProducerClient.java}}) - The backend server which produces messages to RabbitMQ.  This modifies [NewTask.java[({\field{\*\fldinst{HYPERLINK "https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java"}}{\fldrslt https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java}}) from the RabbitMQ tutorial two java by changing the RabbitMQ channel name defined in the variable \cf2 TASK_QUEUE_NAME.\cf0  \
* [ConsumerClient]({\field{\*\fldinst{HYPERLINK "https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ConsumerClient.java"}}{\fldrslt https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ConsumerClient.java}}) - The backend server which consumes messages from RabbitMQ.  This modifies [Worker.java[({\field{\*\fldinst{HYPERLINK "https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java"}}{\fldrslt https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/Worker.java}}) from the RabbitMQ tutorial two java by changing the RabbitMQ channel name defined in the variable \cf2 TASK_QUEUE_NAME.\cf0  \
* [ProducerServer]({\field{\*\fldinst{HYPERLINK "https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ProducerServer.java"}}{\fldrslt https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ProducerServer.java}}) - The PubNub adapter which subscribes to messages from PubNub and produces messages to RabbitMQ.\
* [ConsumerServer]({\field{\*\fldinst{HYPERLINK "https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ProducerServer.java"}}{\fldrslt https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/ConsumerServer.java}}) - The PubNub adapter which consumes messages from RabbitMQ and then publishes messages to PubNub.\
\
You can see the following workflow in [this diagram](https://github.com/pubnub/rabbitmq/tree/master/docs/RabbitMQ-Adapter-Workflow.pdf) \
\pard\pardeftab720
\cf3 * Backend Server Publishes to PubNub through RabbitMQ:\
1) ProducerClient ------>Produces Message ------>\'a0RabbitMQ\
2) RabbitMQ\'a0------> Distributes Message ------> ConsumerServer\
3) ConsumerServer\'a0------>Publishes Message------>\'a0PubNub\
\
* Backend Server Subscribes to PubNub through RabbitMQ\
1) ProducerServer <------Subscribes to Message <------\'a0PubNub\
2) RabbitMQ <------Produces Message <-----\'a0ProducerServer\
3) ConsumerClient <------Distributes Message <------\'a0RabbitMQ\
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural
\cf0 \
#Getting Started\
* Initialize environment\
1) Start RabbitMQ Server\
	> rabbitmq-server\
2) Start PubNub-RabbitMQ adapters in their own terminals from the directory you used for the RabbitMQ tutorials \
	> \cf3 java -cp "./*" com.pubnub.examples.ProducerServer\
	> java -cp "./*" com.pubnub.examples.ConsumerServer\cf0 \
\
* Receive Message on Backend Server from Client in the Cloud\
1) Start the RabbitMQ message consumer\
	> \cf3 java -cp "./*" com.pubnub.examples.ConsumerClient\
2) Publish a message on the [PubNub Dev Console]({\field{\*\fldinst{HYPERLINK "http://www.pubnub.com/console"}}{\fldrslt http://www.pubnub.com/console}}) on channel 'rabbitWorker' using publish key 'demo'\
3) Go to the ProducerServer terminal and confirm that the adapter successfully subscribes to the message from PubNuib and then produces the message successfully to RabbitMQ\
4) Go to the ConsumerClient terminal and confirm that the backend server consumed the message successfully from RabbitMQ\
5) Rinse, Lather, Repeat.\
6) Optionally, try closing the RabbitMQ message consumer from Step 1 and performing steps 2 through 5.  Then at some point perform step 1 to see how messages can still be successfully delivered even in the case of availability issues with your back-end servers.\
7) Optionally, try opening up several RabbitMQ message consumers to see how RabbitMQ can distribute the workload across several backend servers\
\
\cf0 * Receive Message on Client in the Cloud from Backend Server\
1) \cf3 Open the [PubNub Dev Console]({\field{\*\fldinst{HYPERLINK "http://www.pubnub.com/console"}}{\fldrslt http://www.pubnub.com/console}}) and subscribe to channel 'rabbitWorker' using subscribe key 'demo'\cf0 \
2) Start the RabbitMQ message producer\
	> \cf3 java -cp "./*" com.pubnub.examples.
\f1\fs26 ProducerClient hello world from RabbitMQ!
\f0\fs24 \
3) Go to the ConsumerServer terminal and confirm that the adapter consumes the message successfully from RabbitMQ and publishes the message successfully to PubNub\
4) view the message on the [PubNub Dev Console]({\field{\*\fldinst{HYPERLINK "http://www.pubnub.com/console"}}{\fldrslt http://www.pubnub.com/console}}).\
\cf0 \
#License\
\pard\pardeftab720\sl500\sa300
\cf2 * PubNub\
	The MIT License (MIT)Copyright (c) 2013 PubNub\
	Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\
	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\
\pard\pardeftab720\sl500
\cf2 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\
\pard\tx720\tx1440\tx2160\tx2880\tx3600\tx4320\tx5040\tx5760\tx6480\tx7200\tx7920\tx8640\pardirnatural
\cf0 \
* RabbitMQ\
\
	[RabbitMQ - Mozilla Public License]({\field{\*\fldinst{HYPERLINK "http://www.rabbitmq.com/mpl.html"}}{\fldrslt http://www.rabbitmq.com/mpl.html}})}