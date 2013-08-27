PubNub RabbitMQ
==============
This is an adapter layer using java to connect a RabbitMQ deployment with the PubNub real-time network.  RabbitMQ provides message broker middleware to add scaleability, availability, reliability, and failover features behind the firewall.  PubNub provides a real-time network with these same characteristics in the cloud.  Combining the two provides an end-to-end solution with resiliency and flexibility for pub/sub use-cases between backend servers and clients in the cloud.

#Installation
This demo assumes that you have a working RabbitMQ installation using java.  This is very straightforward to set up by following the [RabbitMQ tutorial one java](http://www.rabbitmq.com/tutorials/tutorial-one-java.html) and [RabbitMQ tutorial two java](http://www.rabbitmq.com/tutorials/tutorial-two-java.html).

Once the two tutorials are working, you can plug in PubNub's adapter to connect your pub/sub model behind the firewall with PubNub's real-time network.  

1. [PubNub libraries](https://github.com/pubnub/rabbitmq/tree/master/lib) - drop the required PubNub java libraries into the directory you used for the RabbitMQ tutorials
2. [Adapter jar file](https://github.com/pubnub/rabbitmq/blob/master/jar/PubNub-RabbitMQ.jar) - drop the RabbitMQ adapter jar file into the directory you used for the RabbitMQ tutorials

#Description
This adapter demo takes the two class files from the second RabbitMQ java tutorial and changes the channel name so that the message producer and message consular use different channels.  That is really the only change required to the RabbitMQ classes.  As far as your backend servers are concerned, they are just producing and consuming messages to/from RabbitMQ as usual, with no notion of PubNub.

The adapters connect the RabbitMQ queue/exchange with PubNub.  One adapter will take messages from the queue and publish them to PubNub.  The other adapter will take messages from PubNub and send them to RabbitMQ.

This is a good time to define the various components:
* RabbitMQ - The middleware layer incorporating the message exchange and queue to handle delivery of messages.  The message exchange/broker/queue are all referred to together as RabbitMQ for the purposes of this demo
* [Producer](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/Producer.java) - The backend server which produces messages to RabbitMQ.  This modifies [NewTask.java[(https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java) from the RabbitMQ tutorial two java by changing the RabbitMQ channel name defined in the variable TASK_QUEUE_NAME.  
* [Consumer](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/Consumer.java) - The backend server which consumes messages from RabbitMQ.  This modifies [Worker.java[(https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java) from the RabbitMQ tutorial two java by changing the RabbitMQ channel name defined in the variable TASK_QUEUE_NAME.  
* [SubscribeAdapter](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/SubscribeAdapter.java) - The PubNub adapter which subscribes to messages from PubNub and produces messages to RabbitMQ.
* [PublishAdapter](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/SubscribeAdapter.java) - The PubNub adapter which consumes messages from RabbitMQ and then publishes messages to PubNub.

You can see the following workflow in [this diagram](https://github.com/pubnub/rabbitmq/tree/master/docs/RabbitMQ-Adapter-Workflow.pdf) 

Backend Server Publishes to PubNub through RabbitMQ:

1. Producer ------> Produces Message ------> RabbitMQ
1. RabbitMQ ------> Distributes Message ------> PublishAdapter
1. PublishAdapter ------> Publishes Message------> PubNub

Backend Server Subscribes to PubNub through RabbitMQ:

1. SubscribeAdapter <------Subscribes to Message <------ PubNub
1. RabbitMQ <------Produces Message <----- SubscribeAdapter
1. Consumer <------Distributes Message <------ RabbitMQ

#Getting Started
Initialize environment

1. Start RabbitMQ Server
	\> rabbitmq-server
1. Start PubNub-RabbitMQ adapters in their own terminals from the directory you used for the RabbitMQ tutorials 
	\> java -cp "./*" com.pubnub.examples.SubscribeAdapter
	\> java -cp "./*" com.pubnub.examples.PublishAdapter 

Receive Message on Backend Server from Client in the Cloud

1. Start the RabbitMQ message consumer
	\> java -cp "./*" com.pubnub.examples.Consumer
1. Publish a message on the [PubNub Dev Console](http://www.pubnub.com/console) on channel 'rabbitWorker' using publish key 'demo'
1. Go to the SubscribeAdapter terminal and confirm that the adapter successfully subscribes to the message from PubNuib and then produces the message successfully to RabbitMQ
1. Go to the Consumer terminal and confirm that the backend server consumed the message successfully from RabbitMQ
1. Rinse, Lather, Repeat.
1. Optionally, try closing the RabbitMQ message consumer from Step 1 and performing steps 2 through 5.  Then at some point perform step 1 to see how messages can still be successfully delivered even in the case of availability issues with your back-end servers.
1. Optionally, try opening up several RabbitMQ message consumers to see how RabbitMQ can distribute the workload across several backend servers

Receive Message on Client in the Cloud from Backend Server

1. Open the [PubNub Dev Console](http://www.pubnub.com/console) and subscribe to channel 'rabbitWorker' using subscribe key 'demo' 
1. Start the RabbitMQ message producer
	\> java -cp "./*" com.pubnub.examples.Producer hello world from RabbitMQ!
1. Go to the PublishAdapter terminal and confirm that the adapter consumes the message successfully from RabbitMQ and publishes the message successfully to PubNub
1. view the message on the [PubNub Dev Console](http://www.pubnub.com/console)
1. (Optionally) Leave the SubscribeAdapter and Consumer classes running and go to those terminals to confirm that the same message sent to PubNub was also delivered all the way back fown to the Consumer

#License
PubNub

	The MIT License (MIT)Copyright (c) 2013 PubNub Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

* RabbitMQ

	[RabbitMQ - Mozilla Public License](http://www.rabbitmq.com/mpl.html)
