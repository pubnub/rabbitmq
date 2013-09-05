#PubNub RabbitMQ

##Overview
This is an adapter layer using java to connect a RabbitMQ deployment with web users through the PubNub real-time network.  RabbitMQ provides message broker middleware commonly used for server to server communication.  PubNub provides a real-time network to connect any cloud devices worldwide.  Combining RabbitMQ with PubNub takes RabbitMQ and instantly extend its reach out to the cloud.

The desire to extend a RabbitMQ to the cloud has typically been to publish messages to web clients.  One part of this demo provides an example of an adapter which takes messages off of a RabbitMQ queue, inspects the message, and decides whether or not it should be published to web clients.

The flipside would be to subscribe to messages from web clients and push them to servers behind RabbitMQ.  This would take the model one step further and allow for bi-directional communication in real-time.  The desire to take messages from the web and add them to RabbitMQ has not been as prevalent as there had been no secure, flexible, and effective way to accomplish this.  However, this demo will show how easy and elegant it can be to instantly add this new class of use cases with no disruption to clients or servers and no buildout required. 

##Description
This demo takes the two class files from the second RabbitMQ java tutorial and changes the channel name so that the message producer (formerly NewTask.java) and message consumer (formerly (Worker.java) use different channels.  That is really the only change required to the RabbitMQ classes.  As far as your backend servers are concerned, they are just producing and consuming messages to/from RabbitMQ as usual, with no notion of the perils of the internet. 

The adapters will connect the RabbitMQ queue/exchange with PubNub over standard http ports 80 and 443.  One adapter will take messages from the queue and publish them to PubNub.  The other adapter will take messages from PubNub and send them to RabbitMQ.  both of these adapters provide examples of business logic to inspect messages and decide whether they should be discarded or sent forward. 

##Component Definition
This is a good time to define the various components: 

* RabbitMQ - The middleware layer incorporating the message exchange and queue to handle delivery of messages.  The message exchange/broker/queue are all referred to together as RabbitMQ for the purposes of this demo 
* [Producer](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/Producer.java) - The backend server which produces messages to RabbitMQ.  This modifies [NewTask.java](https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java) from the [RabbitMQ tutorial two java](http://www.rabbitmq.com/tutorials/tutorial-two-java.html) by changing the RabbitMQ channel name defined in the variable TASK_QUEUE_NAME.  
* [Consumer](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/Consumer.java) - The backend server which consumes messages from RabbitMQ.  This modifies [Worker.java](https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java) from the [RabbitMQ tutorial two java](http://www.rabbitmq.com/tutorials/tutorial-two-java.html) by changing the RabbitMQ channel name defined in the variable TASK_QUEUE_NAME.  
* [SubscribeAdapter](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/SubscribeAdapter.java) - The PubNub adapter which subscribes to messages from PubNub and produces qualified messages to RabbitMQ. 
* [PublishAdapter](https://github.com/pubnub/rabbitmq/blob/master/src/com/pubnub/examples/SubscribeAdapter.java) - The PubNub adapter which consumes messages from RabbitMQ and then publishes qualified messages to PubNub. 
* Cloud Client - Any client communicating over tcp port 80 (or optionally 443 for ssl).  This will be the client out in the cloud that we have now extended RabbitMQ's reach to.  This client can possibly be a web page using PubNub's javascript SDK, a client app using one of PubNub's sdk's (ie java, javascript, iOS, android, etc), or an app issuing simple REST API calls.  Our example just uses PubNub's [Dev console](http://www.pubnub.com/console) to demonstrate inbound and outbound messages.

##Installation
This demo assumes that you have a working RabbitMQ installation using java.  For the purposes of this demo, you can simply follow the [RabbitMQ tutorial one java](http://www.rabbitmq.com/tutorials/tutorial-one-java.html) and [RabbitMQ tutorial two java](http://www.rabbitmq.com/tutorials/tutorial-two-java.html) to make sure you have a bare-bones environment. 

Once the two tutorials are working, you can plug in PubNub's adapter to connect your RabbitMQ deployment with PubNub's real-time network. 

1. [Adapter jar and libraries](https://github.com/pubnub/rabbitmq/tree/master/zip/pubnub-rabbitmq.zip) - download this zip file which contains the PubNub-RabbitMQ.jar file as well as required jar files for PubNub's java SDK and RabbitMQ's java SDK 
2. Unzip pubnub-rabbitmq.zip into your desired location 
3. Follow the steps in the Getting Started section below 


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
2. Publish a message on the [PubNub Dev Console](http://www.pubnub.com/console) on channel 'rabbitWorker' using publish key 'demo'.  For this demo, the message should be valid JSON containing an element named 'Amount' 
``` 
	{"Depositor":"Randy","Amount":123.00} 
```  
3. Switch to the SubscribeAdapter terminal and confirm that the adapter successfully subscribes to the message from PubNub and then produces the message successfully to RabbitMQ.  Note how the SubscribeAdapter appended an additional element (Verifier) to the original payload.  This demonstrates how we can handle cases where backend servers require additional data or formatting by using the adapter to massage the data into a consumable format. 
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
5. Rinse, Lather, Repeat.  We have now taken a message from the cloud, qualified it as worthy to send forward, reformatted it, and sent it to a backend server through RabbitMQ. (Scary or Exciting?) 
6. Optionally, publish a message with a missing, negative, or non-numeric Amount field.  Note how it is received on the SubscribeAdapter but our business logic will discard the invalid message rather than burden RabbitMQ. 
	for example, here is a negative Amount
``` 
	{"Depositor":"Randy","Amount":-123.00} 
``` 
	which triggers the following messages in the PublishAdapter terminal 
``` 
	[x] SubscribeAdapter SUBSCRIBE : RECEIVED on channel:rabbitWorker : class org.json.JSONObject : {"text":"hey","Amount":-123} 
	[*] SubscribeAdapter : PRODUCE : inspecting message : {"text":"hey","Amount":-123} 
	[-] SubscribeAdapter : PRODUCE : discarding due to invalid Amount in {"Amount":-123,"Depositor":"Randy"} 
``` 
7. Optionally, perform steps 2 through 5 after stopping the Consumer from Step 1 in order to verify that the resiliency of RabbitMQ remains intact even with backend servers going down. 
8. Optionally, open several RabbitMQ message consumers to see how RabbitMQ distributes the workload across several backend servers. 

###Receive Message in the Cloud from Backend Server 

1. Open the [PubNub Dev Console](http://www.pubnub.com/console) and subscribe to channel 'rabbitWorker' using subscribe key 'demo' 
2. Start the RabbitMQ message producer 
``` 
	\> java -cp "./*" com.pubnub.examples.Producer 

	[x] Producer : sent '{"Depositor":"Randy","Amount":"1000000.01"}' 
```
3. Go to the PublishAdapter terminal and confirm that the adapter consumes the message successfully from RabbitMQ, validates the message, and publishes successfully to PubNub 
``` 
	 [x] PublishAdapter : received '{"Depositor":"Randy","Amount":"1000000.01"}' 

	 [*] PublishAdapter : verified Amount (1000000.01) exceeds threshold (1000000.0) so let's publish to PubNub 

	 [+] PublishAdapter : published to PubNub [1,"Sent","13777307636386103"] 
``` 
4. View the message on the [PubNub Dev Console](http://www.pubnub.com/console).  Note how the PublishAdapter appended an additional element (Verifier) to the original payload.  This demonstrates how we can handle cases where cloud clients require additional data or formatting by using the adapter to massage the data into a consumable format. 
```  
	{"Amount":"1000000.01", "Verifier":["PublishAdapter"], "Threshold":1000000, "Depositor":"Randy"}
``` 
5. (Optionally) Produce a message with a missing, negative, non-numeric, or smaller-than-one-million Amount field.  Note how it is received on the SubscribeAdapter but our business logic will discard the message.  Our SubscribeAdapter's business logic only publishes messsages to the cloud if the Amount is greater than one million. 

	for example, here is an Amount less than one million with the message supplied as an argument in quotes and the quotes within the message escaped with a backslash.
``` 
	\> java -cp "./*" com.pubnub.examples.Producer "{\"Depositor\":\"Randy\",\"Amount\":\"123\"}" 
``` 	
	which generates the following output in the PublishAdapter terminal
``` 
	 [x] PublishAdapter : received '{"Depositor":"Randy","Amount":"123"}' 
	 [-] PublishAdapter : discarding, Amount (123.0) is below threshold (1000000.0) so let's not publish to PubNub 
``` 
6. (Optionally) Leave the SubscribeAdapter and Consumer classes running and switch to those terminals to confirm that the same message published to PubNub was also delivered all the way back down to the Consumer 
	For example, the SubscribeAdapter console 
``` 
	 [x] SubscribeAdapter : SUBSCRIBE : RECEIVED on channel:rabbitWorker : class org.json.JSONObject : {"Amount":"1000000.01","Verifier":["PublishAdapter"],"Threshold":1000000,"Depositor":"Randy"} 
	 [*] SubscribeAdapter : PRODUCE : inspecting message {"Amount":"1000000.01","Verifier":["PublishAdapter"],"Threshold":1000000,"Depositor":"Randy"} 
	 [*] SubscribeAdapter : PRODUCE : verified Amount: 1000000.01 
	 [+] SubscribeAdapter : PRODUCE : produced to task_queue_inbound_durable '{"Amount":"1000000.01","Verifier":["PublishAdapter","SubscribeAdapter"],"Threshold":1000000,"Depositor":"Randy"}' 
``` 
	For example, the Consumer console.  Note how the Verifier element in the message body now indicates that the PublishAdapter and the SubscribeAdapter had both processed the message. 
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
