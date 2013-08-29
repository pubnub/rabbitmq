package com.pubnub.examples;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

/*
 * Note this class is nearly identical to NewTask.java in tutorial 2 on the RabbitMQ site from
 * http://www.rabbitmq.com/tutorials/tutorial-two-java.html
 * You can download the source code directly from 
 * http://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/NewTask.java
 * The only real difference is that we use 2 different RabbitMQ channels to avoid collisions.
 * We use one channel to produce messages to and another to consume messages from
 * So make sure you set the constant TASK_QUEUE_NAME to something other than 'task_queue', 
 * such as 'task_queue_outbound_durable'
*/

public class Producer {

  private static final String TASK_QUEUE_NAME = "task_queue_outbound_durable";

  public static void main(String[] argv) throws Exception {

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

    String message = getMessage(argv);

    channel.basicPublish( "", TASK_QUEUE_NAME,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes());
    System.out.println(" [x] Producer sent '" + message + "'");

    channel.close();
    connection.close();
  }

  private static String getMessage(String[] strings){
    if (strings.length < 1)
      return "{\"Depositor\":\"Randy\",\"Amount\":\"1000000.01\"}";
    return joinStrings(strings, " ");
  }

  private static String joinStrings(String[] strings, String delimiter) {
    int length = strings.length;
    if (length == 0) return "";
    StringBuilder words = new StringBuilder(strings[0]);
    for (int i = 1; i < length; i++) {
      words.append(delimiter).append(strings[i]);
    }
    return words.toString();
  }
}
