author:	randeep@pubnub.com
revision: 1
last revised: 8/21/2013

This package is under construction.

Here's an excerpt from a note I sent to the rabbit-discuss@lists.rabbitmq.com:

I'm building an integration framework/scaffolding using Java between RabbitMQ broker and our real-time network.  We have been repeatedly asked if there are reference examples of integrations with PubNub, and since no one has built one yet, I figured I'd give it a shot.

PubNub is the leading real-time network that enables you to add real-time functionality to your web properties.  We allow for broadcast and unicast messaging as well as the core building blocks such as presence, history, auth, api's, etc.  We're providing a global network that is distributed, secure, fast, and reliable so folks can concentrate on their core competencies with their apps and leave the build-out and operationalization of a real-time delivery network to us.  Oh, and then you do not need to worry about the headache of supporting large numbers of disparate devices and corner cases such as mobile devices switching bands or catching up after lost connections.  That's where PubNub shines.

There is some conceptual overlap between RabbitMQ and PubNub since we both use the pub/sub model, but I'm not looking to displace RabbitMQ.  I am thinking of ways to hook RabbitMQ into PubNub in order to offer new capabilities that could not exist before.  I'd love to hear your feedback on some of my ideas and also hear other real-world scenarios to highlight in order to come up with a compelling and useful demo.

At a high-level, I am thinking of RabbitMQ behind the firewall, and PubNub handling delivery in the cloud.   So imagine if a device behind the corporate firewall produced a message to RabbitMQ.  My adapter would consume this message from RabbitMQ, execute some business logic (optionally), and then publish the message to PubNub's api.  At that point, any device around the world subscribing to the appropriate PubNub channel would receive the message within 250ms.  You would now have instant global reach.

On the flip-side, a device can publish a message to PubNub, my adapter behind the firewall would subscribe to this message, execute business logic (optionally), and then produce a message to RabbitMQ.  You can then have servers that consume these messages.  The beauty of this would be that the producers and consumers would just speak to RabbitMQ in a very traditional sense.

The kicker here is that you would gain the ability to not only reach a worldwide audience across multiple device types, but you would also gain the ability to communicate bi-directionally in real-time.  Enterprises using RabbitMQ expect a certain level of resiliency, and I want to extend that notion out into the cloud, where it was assumed to be either too costly, complex, or just plain impossible.  And as far as scaling, you only need to worry about your RabbitMQ deployment to handle message delivery behind your firewall; you do not need to worry about scaling to handle usage spikes or millions of connected devices. 

I will follow up with more detailed use cases as the ideas come together.  Eventually I'd like to publicize this project and obviously open it up to the world to use/copy/share/extend.  In the meantime, if you have real-world examples where you would have liked to extend RabbitMQ to reach a global audience with real-time communication, please let me know and i could try to highlight those scenarios.