package eu.mantis.rcademo;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import java.nio.charset.Charset;
import java.time.Instant;

class DeviceToCloud {

  //rcademo
  //Endpoint=sb://iothub-ns-rcademo-430862-3abba0a57f.servicebus.windows.net/;SharedAccessKeyName=iothubowner;
  // SharedAccessKey=zgw95E1XghmEVjDozfdyvMrJFAvoTF5zsNZJ6AYXcAU=

  private static String connStr =
      "Endpoint=sb://iothub-ns-rcademo-430862-3abba0a57f.servicebus.windows.net/;SharedAccessKeyName=iothubowner;"
          + "SharedAccessKey=zgw95E1XghmEVjDozfdyvMrJFAvoTF5zsNZJ6AYXcAU=;EntityPath=rcademo;"
          + "SharedAccessKeyName=iothubowner;SharedAccessKey=zgw95E1XghmEVjDozfdyvMrJFAvoTF5zsNZJ6AYXcAU=";

  // Create a receiver on a partition.
  static EventHubClient receiveMessages(final String partitionId) {
    EventHubClient client = null;
    try {
      client = EventHubClient.createFromConnectionStringSync(connStr);
    } catch (Exception e) {
      System.out.println("Failed to create client: " + e.getMessage());
      System.exit(1);
    }
    try {
      // Create a receiver using the
      // default Event Hubs consumer group
      // that listens for messages from now on.
      client.createReceiver(EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, partitionId, Instant.now()).thenAccept(receiver -> {
        System.out.println("** Created receiver on partition " + partitionId);
        try {
          while (true) {
            Iterable<EventData> receivedEvents = receiver.receive(300).get();
            int batchSize = 0;
            if (receivedEvents != null) {
              System.out.println("Got some events");
              for (EventData receivedEvent : receivedEvents) {
                System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", receivedEvent.getSystemProperties().getOffset(),
                                                 receivedEvent.getSystemProperties().getSequenceNumber(),
                                                 receivedEvent.getSystemProperties().getEnqueuedTime()));
                System.out.println(String.format("| Device ID: %s", receivedEvent.getSystemProperties().get("iothub-connection-device-id")));
                RCAResource.devicePayload = new String(receivedEvent.getBytes(), Charset.defaultCharset());
                System.out.println(String.format("| Message Payload: %s", RCAResource.devicePayload));
                batchSize++;
              }
            }
            System.out.println(String.format("Partition: %s, ReceivedBatch Size: %s", partitionId, batchSize));
          }
        } catch (Exception e) {
          System.out.println("Failed to receive messages: " + e.getMessage());
        }
      });
    } catch (Exception e) {
      System.out.println("Failed to create receiver: " + e.getMessage());
    }
    return client;
  }

}