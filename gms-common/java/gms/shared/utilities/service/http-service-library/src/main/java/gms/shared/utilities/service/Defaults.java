package gms.shared.utilities.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * Default service properties, such as port number.
 */
public class Defaults {

  /**
   * The default JSON object mapper
   */
  public static final ObjectMapper JSON_MAPPER
      = configureObjectMapper(new ObjectMapper());

  /**
   * The default msgpack object mapper
   */
  public static final ObjectMapper MSGPACK_MAPPER
      = configureObjectMapper(new ObjectMapper(new MessagePackFactory()));

  /**
   * The default port number the server runs on
   */
  public static final int PORT = 8080;

  /**
   * The default minimum thread pool size
   */
  public static final int MIN_THREAD_POOL_SIZE = 10;

  /**
   * The default maximum thread pool size
   */
  public static final int MAX_THREAD_POOL_SIZE = 20;

  /**
   * The timeout in milliseconds for a thread to be idle until it is timed out
   */
  public static final int THREAD_IDLE_TIMEOUT_MILLIS = 10000;

  private static ObjectMapper configureObjectMapper(ObjectMapper m) {
    m.findAndRegisterModules();
    m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return m;
  }

}
