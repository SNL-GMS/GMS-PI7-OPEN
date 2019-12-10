package gms.shared.frameworks.client.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import gms.shared.frameworks.common.annotations.Control;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.client.ServiceClient;
import gms.shared.frameworks.client.ServiceRequest;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.common.ContentType;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientGeneratorTests {

  private static final String NO_CONSUMES_OR_PRODUCES = "no-consumes-or-produces";
  private static final String CONSUMES_MSGPACK = "consumes-msgpack";
  private static final String PRODUCES_MSGPACK = "produces-msgpack";
  private static final String CONTROL_NAME = "the-control";
  private static final String BASE_PATH = "example";
  private final String HOSTNAME = "some-host";
  private final int PORT = 5555;
  private final Duration TIMEOUT = Duration.ofMillis(123);
  private final String s = "foo";
  private final JavaType typeOfS = new ObjectMapper().constructType(s.getClass());
  private final String baseUrl = String.format("http://%s:%d/", HOSTNAME, PORT);
  private ExampleApi api;
  @Mock
  private ServiceClient mockClient;
  @Mock
  private SystemConfig sysConfig;

  @BeforeEach
  void setup() throws Exception {
    doReturn(new URL(baseUrl)).when(sysConfig).getUrl();
    doReturn(TIMEOUT).when(sysConfig).getValueAsDuration(SystemConfig.CLIENT_TIMEOUT);
    api = ClientGenerator.createClient(ExampleApi.class, mockClient, sysConfig);
    assertNotNull(api);
  }

  @Control(CONTROL_NAME)
  @Path(BASE_PATH)
  interface ExampleApi {

    @Path(NO_CONSUMES_OR_PRODUCES)
    @POST
    String noConsumesOrProduces(String s);

    @Path(CONSUMES_MSGPACK)
    @POST
    @Consumes({ContentType.MSGPACK_NAME})
    String consumesMsgpack(String s);

    @Path(PRODUCES_MSGPACK)
    @POST
    @Produces({ContentType.MSGPACK_NAME})
    String producesMsgpack(String s);
  }

  @Test
  void testCall_noConsumesOrProduces() throws Exception {
    api.noConsumesOrProduces(s);
    verifyClientCall(NO_CONSUMES_OR_PRODUCES,
        ContentType.defaultContentType(), ContentType.defaultContentType());
  }

  @Test
  void testCall_consumesMsgpack() throws Exception {
    api.consumesMsgpack(s);
    verifyClientCall(CONSUMES_MSGPACK, ContentType.MSGPACK, ContentType.defaultContentType());
  }

  @Test
  void testCall_producesMsgpack() throws Exception {
    api.producesMsgpack(s);
    verifyClientCall(PRODUCES_MSGPACK, ContentType.defaultContentType(), ContentType.MSGPACK);
  }

  private void verifyClientCall(String path, ContentType consumes, ContentType produces)
      throws Exception {
    verify(mockClient).send(ServiceRequest.from(
        constructUrl(path), s, TIMEOUT, typeOfS, consumes, produces));
  }

  private URL constructUrl(String path) throws MalformedURLException {
    return new URL(baseUrl + BASE_PATH + "/" + path);
  }

  @Path(BASE_PATH)
  interface InterfaceWithoutControlAnnotation {

    @Path(NO_CONSUMES_OR_PRODUCES)
    @POST
    String noConsumesOrProduces(String s);

    @Path(CONSUMES_MSGPACK)
    @POST
    String consumesMsgpack(String s);

    @Path(PRODUCES_MSGPACK)
    @POST
    String producesMsgpack(String s);
  }

  @Test
  void testCallWithoutControlAnnotationThrows() {
    assertEquals("Client interface must have @Control",
        assertThrows(IllegalArgumentException.class,
            () -> ClientGenerator.createClient(InterfaceWithoutControlAnnotation.class))
            .getMessage());
  }
}
