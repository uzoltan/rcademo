package eu.mantis.rcademo;

import com.microsoft.azure.eventhubs.EventHubClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RCAMain {

  private static HttpServer server;
  private static EventHubClient client0;
  private static EventHubClient client1;

  private static final String BASE_URI = "http://0.0.0.0:8350";

  public static void main(String[] args) throws Exception {
    server = startServer();
    // Create receivers for partitions 0 and 1.
    client0 = DeviceToCloud.receiveMessages("0");
    client1 = DeviceToCloud.receiveMessages("1");

    boolean daemon = false;
    for (String arg : args) {
      if (arg.equals("-d")) {
        daemon = true;
      }
    }

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Mimosa-MQTT interface...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }

  }


  private static HttpServer startServer() throws IOException {
    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(RCAResource.class);

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    System.out.println("Started insecure server at: " + BASE_URI);
    return server;
  }

  private static void shutdown() {
    if (server != null) {
      server.shutdownNow();
    }
    try {
      client0.closeSync();
      client1.closeSync();
      System.out.println("Server stopped!");
      System.exit(0);
    } catch (Exception e) {
      System.exit(1);
    }
  }

}
