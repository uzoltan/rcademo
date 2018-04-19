package eu.mantis.rcademo;

import com.microsoft.azure.sdk.iot.service.DeliveryAcknowledgement;
import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.FeedbackRecord;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import java.time.LocalDateTime;
import java.util.List;

public class CloudToDevice {

  private static final String connectionString = "HostName=RCADemo.azure-devices.net;SharedAccessKeyName=iothubowner;"
      + "SharedAccessKey=zgw95E1XghmEVjDozfdyvMrJFAvoTF5zsNZJ6AYXcAU=";
  private static final String deviceId = "RCAAndroid";
  private static final IotHubServiceClientProtocol protocol = IotHubServiceClientProtocol.AMQPS;

  static void sendToDevice() throws Exception {
    ServiceClient serviceClient = ServiceClient.createFromConnectionString(connectionString, protocol);

    if (serviceClient != null) {
      serviceClient.open();
      FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver();
      feedbackReceiver.open();

      RCAResource.timeStamp = LocalDateTime.now().toString();
      Message messageToSend = new Message(RCAResource.timeStamp + ";Are you able to start the truck?");
      messageToSend.setDeliveryAcknowledgement(DeliveryAcknowledgement.Full);

      serviceClient.send(deviceId, messageToSend);
      System.out.println("Message sent to device");

      FeedbackBatch feedbackBatch = feedbackReceiver.receive(10000);
      if (feedbackBatch != null) {
        List<FeedbackRecord> test = feedbackBatch.getRecords();
        System.out.println("Message feedback received, feedback time: " + feedbackBatch.getEnqueuedTimeUtc().toString());
      }

      feedbackReceiver.close();
      serviceClient.close();
    }
  }

}
