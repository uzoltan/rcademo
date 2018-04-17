package eu.mantis.rcademo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("rca")
public class RCAResource {

  static String timeStamp = "notnull";
  static String devicePayload = "default";

  @GET
  @Path("test")
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the MQTT_MIMOSA interface -> Box Resource.";
  }

  @GET
  public Response getTimeStampedAnswerFromDevice() {
    try {
      CloudToDevice.sendToDevice();
      String[] payloadFields = devicePayload.split(";");
      int i = 0;
      while (!payloadFields[0].equals(timeStamp)) {
        Thread.sleep(100);
        payloadFields = devicePayload.split(";");
        i++;
        if (i > 300) {
          break;
        }
      }
      if (devicePayload.equals("default")) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      } else {
        return Response.status(Status.OK).entity(devicePayload).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}
