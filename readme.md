#PubNub RabbitMQ

##Overview
This is an adapter layer using java to connect a RabbitMQ deployment with the PubNub real-time network.  RabbitMQ provides message broker middleware commonly used for server to server communication.  PubNub provides a real-time network to connect all cloud devices worldwide.  Connecting RabbitMQ to PubNub will take the pub/sub model of RabbitMQ and instantly extend its reach out to the cloud.

The desire to extend a RabbitMQ to the cloud has usually been for publishing messages to web clients.  This demo provides an example of an adapter which can take messages off of the queue, inspect the message, and decide whether or not it should be published to web clients.  This could be a unicast model where it is intended for s certain user or it could be broadcasted out to a large group.  

The desire to take messages from the web and add them to RabbitMQ has not been as prevalent.  The assumption has been that this is not possible to do in an effective and reasonable way.  However, this demo now provides an easy and elegant way to instantly add a whiole new dimension and feature set to RabbitMQ.  With these 2 adapters, you can now add bi-directional communication between RabbitMQ and any client on the web.  The possibilities are endless, and no changes are required to RabbitMQ and no buildout requred whatsoever.

This demo provides an elegant, robust, and secure way to connect RabbitMQ with web users with minimal changes.  

*No changes to RabbitMQ - The adapters will take care of consuming messages from RabbitMQ to publish to PubNub, and subscribing to PubNub to produce messages to RabbitMQ.  As far as RabbitMQ is concerned, the adapters are just another producer and another consumer.
*No changes to Backend Servers - The backend servers will continue to communicate with RabbitMQ and it will be business as usual.  They just send and receive messags without having to know the end-point on the web. 
*No changes to Firewall - The adapters will pub/sub to PubNub over standard web ports (80 nand 443).  There are no arbitrary or special ports to manage. 
*Flexibility - The adapters can contain business logic to determine how to handle the messages to and from PubNub, and this logic will be completely offloaded to the adapters.  Some examples are
** Modifying the message paylod with additional/modified/calculated data
** Converting the payload to a different format
** Analyzing the payload for security threats or invalid/incomplete data
** Determining which channel to direct the message to
** Determining whether to move the message forward or to discard it

##Installation
This demo assumes that you have a working RabbitMQ installation using java.  This is very straightforward to set up by following the [RabbitMQ tutorial one java](http://www.rabbitmq.com/tutorials/tutorial-one-java.html) and [RabbitMQ tutorial two java](http://www.rabbitmq.com/tutorials/tutorial-two-java.html).

Once the two tutorials are working, you can plug in PubNub's adapter to connect your pub/sub model behind the firewall with PubNub's real-time network.  

1. [PubNub libraries](https://github.com/pubnub/rabbitmq/tree/master/lib) - drop the required PubNub java libraries into the directory you used for the RabbitMQ tutorials
2. [Adapter jar file](https://github.com/pubnub/rabbitmq/blob/master/jar/PubNub-RabbitMQ.jar) - drop the RabbitMQ adapter jar file into the directory you used for the RabbitMQ tutorials

##Description
This adapter demo takes the two class files from the second RabbitMQ java tutorial and changes the channel name so that the message producer and message consular use different channels.  That is really the only change required to the RabbitMQ classes.  As far as your backend servers are concerned, they are just producing and consuming messages to/from RabbitMQ as usual, with no notion of PubNub.

The adapters connect the RabbitMQ queue/exchange with PubNub.  One adapter will take messages from the queue and publish them to PubNub.  The other adapter will take messages from PubNub and send them to RabbitMQ.

###Component Definition
This is a good time to define the various components:
* RabbitMQ - The middleware layer incorporating the message exchange and queue to handle delivery of messages.  The message exchange/broker/queue are all referred to together as RabbitMQ for the purposes of this demo
* [Producer](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/Producer.java) - The backend server which produces messages to RabbitMQ.  This modifies [NewTask.java](https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java) from the RabbitMQ tutorial two java by changing the RabbitMQ channel name defined in the variable TASK_QUEUE_NAME.  
* [Consumer](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/Consumer.java) - The backend server which consumes messages from RabbitMQ.  This modifies [Worker.java](https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java) from the RabbitMQ tutorial two java by changing the RabbitMQ channel name defined in the variable TASK_QUEUE_NAME.  
* [SubscribeAdapter](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/SubscribeAdapter.java) - The PubNub adapter which subscribes to messages from PubNub and produces messages to RabbitMQ.
* [PublishAdapter](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/SubscribeAdapter.java) - The PubNub adapter which consumes messages from RabbitMQ and then publishes messages to PubNub.

You can see the following workflow in [this diagram](https://github.com/pubnub/rabbitmq/tree/master/docs/RabbitMQ-Adapter-Workflow.pdf) 

###Workflow

####Backend Server Publishes to PubNub through RabbitMQ:

1. Producer ------> Produces Message ------> RabbitMQ
2. RabbitMQ ------> Distributes Message ------> PublishAdapter
3. PublishAdapter <-----> Inspects Message <-----> Applies Business Logic
4. PublishAdapter ------> Publishes Message------> PubNub

####Backend Server Subscribes to PubNub through RabbitMQ:

1. SubscribeAdapter <------ Subscribes to Message <------ PubNub
2. Applies Business Logic <-----> Inspects Message <-----> SubscribeAdapter
3. RabbitMQ <------ Produces Message <----- SubscribeAdapter
4. Consumer <------ Distributes Message <------ RabbitMQ

##Getting Started

###Initialize environment

1. Start RabbitMQ Server
```
\> rabbitmq-server
```
2. Start PubNub-RabbitMQ adapters in their own terminals from the directory you used for the RabbitMQ tutorials 
```
	\> java -cp "./*" com.pubnub.examples.SubscribeAdapter
	\> java -cp "./*" com.pubnub.examples.PublishAdapter 
```

###Receive Message on Backend Server from the Cloud

1. Start the RabbitMQ message consumer
```
	\> java -cp "./*" com.pubnub.examples.Consumer
```
2. Publish a message on the [PubNub Dev Console](http://www.pubnub.com/console) on channel 'rabbitWorker' using publish key 'demo'.  For this demo, the message should be vslid JSON containing an element named 'Amount'
```
	{"Depositor":"Randy","Amount":123.00}
```
3. Go to the SubscribeAdapter terminal and confirm that the adapter successfully subscribes to the message from PubNub and then produces the message successfully to RabbitMQ.  Note how the SubscribeAdapter appended an additional element (Verifier) to the original payload.  In this demo, we can assume that the backend servers require additional data or formatting so the adapter is able to massage the data into a consumable format.
```
	 [x] SubscribeAdapter : SUBSCRIBE : RECEIVED on channel:rabbitWorker : class org.json.JSONObject : {"Amount":123,"Depositor":"Randy"}
	 [*] SubscribeAdapter : PRODUCE : inspecting message : {"Amount":123,"Depositor":"Randy"}
	 [*] SubscribeAdapter : PRODUCE : verified Amount: 123.0
	 [+] SubscribeAdapter : PRODUCE : produced to task_queue_inbound_durable '{"Amount":123,"Verifier":["SubscribeAdapter"],"Depositor":"Randy"}'
```
4. Go to the Consumer terminal and confirm that the backend server consumed the message successfully from RabbitMQ
```
	 [x] Consumer : received '{"Amount":123,"Verifier":["SubscribeAdapter"],"Depositor":"Randy"}'
```
5. Rinse, Lather, Repeat.
6. Optionally, publish a message with a missing, negative, or non-numeric Amount field.  Note how it is received on the SubscribeAdapter but our demo's business logic will decide to discard the message rather than producing a message to RabbitMQ.

	for example, here is an Amount element which is a negative number
```
	{"Depositor":"Randy","Amount":-123.00}
```	
	and here is the output from the PublishAdapter terminal
```
	[x] SubscribeAdapter SUBSCRIBE : RECEIVED on channel:rabbitWorker : class org.json.JSONObject : {"text":"hey","Amount":-123}
	[*] SubscribeAdapter : PRODUCE : inspecting message : {"text":"hey","Amount":-123}
	[-] SubscribeAdapter : PRODUCE : discarding due to invalid Amount in {"Amount":-123,"Depositor":"Randy"}
```
7. Optionally, try closing the RabbitMQ message consumer from Step 1 and performing steps 2 through 5.  Then at some point perform step 1 to see how messages can still be successfully delivered even in the case of availability issues with your back-end servers.
8. Optionally, try opening up several RabbitMQ message consumers to see how RabbitMQ can distribute the workload across several backend servers

###Receive Message in the Cloud from Backend Server

1. Open the [PubNub Dev Console](http://www.pubnub.com/console) and subscribe to channel 'rabbitWorker' using subscribe key 'demo' 
2. Start the RabbitMQ message producer
```
	\> java -cp "./*" com.pubnub.examples.Producer
	[x] Producer : sent '{"Depositor":"Randy","Amount":"1000000.01"}'
```
3. Go to the PublishAdapter terminal and confirm that the adapter consumes the message successfully from RabbitMQ and publishes the message successfully to PubNub
```
	 [x] PublishAdapter : received '{"Depositor":"Randy","Amount":"1000000.01"}'
	 [*] PublishAdapter : verified Amount (1000000.01) exceeds threshold (1000000.0) so let's publish to PubNub
	 [+] PublishAdapter : published to PubNub [1,"Sent","13777307636386103"]
```
4. view the message on the [PubNub Dev Console](http://www.pubnub.com/console).  Note how the PublishAdapter appended additional elements (Verifier and Threshold) to the original payload.  In this demo, we can assume that the cloud clients require additional data or formatting so the adapter is able to massage the data into a coinsumable format.
```
	{"Amount":"1000000.01", "Verifier":["PublishAdapter"], "Threshold":1000000, "Depositor":"Randy"}
```
5. (Optionally) produce a message with a missing, negative, non-numeric, or smaller-than-one-million Amount field.  Note how it is received on the SubscribeAdapter but our demo's business logic will decide to discard the message rather than publish to PubNub.  Our SubscribeAdapter's business logic decides that any messsage with an Amount greater than one million should be published to the cloud, and all other messages should be discarded.

	for example, here is an Amount element which is less than one million
```
	\> java -cp "./*" com.pubnub.examples.Producer "{\"Depositor\":\"Randy\",\"Amount\":\"123\"}"
```	
	and here is the output from the PublishAdapter terminal
```
	 [x] PublishAdapter : received '{"Depositor":"Randy","Amount":"123"}'
	 [-] PublishAdapter : discarding, Amount (123.0) is below threshold (1000000.0) so let's not publish to PubNub
```
6. (Optionally) Leave the SubscribeAdapter and Consumer classes running and go to those terminals to confirm that the same message sent to PubNub was also delivered all the way back down to the Consumer

	For example, the SubscribeAdapter console
```
	 [x] SubscribeAdapter : SUBSCRIBE : RECEIVED on channel:rabbitWorker : class org.json.JSONObject : {"Amount":"1000000.01","Verifier":["PublishAdapter"],"Threshold":1000000,"Depositor":"Randy"}	 
	 [*] SubscribeAdapter : PRODUCE : inspecting message {"Amount":"1000000.01","Verifier":["PublishAdapter"],"Threshold":1000000,"Depositor":"Randy"}
	 [*] SubscribeAdapter : PRODUCE : verified Amount: 1000000.01
	 [+] SubscribeAdapter : PRODUCE : produced to task_queue_inbound_durable '{"Amount":"1000000.01","Verifier":["PublishAdapter","SubscribeAdapter"],"Threshold":1000000,"Depositor":"Randy"}'
```	
	For example, the Consumer console.  Note how Verifier element indicates that the PublishAdapter and the SubscribeAdapter had processed the message.
```
	 [x] Consumer : received '{"Amount":"1000000.01","Verifier":["PublishAdapter","SubscribeAdapter"],"Threshold":1000000,"Depositor":"Randy"}'
```

##License
PubNub

	The MIT License (MIT)Copyright (c) 2013 PubNub Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

* RabbitMQ

	[RabbitMQ - Mozilla Public License](http://www.rabbitmq.com/mpl.html)
