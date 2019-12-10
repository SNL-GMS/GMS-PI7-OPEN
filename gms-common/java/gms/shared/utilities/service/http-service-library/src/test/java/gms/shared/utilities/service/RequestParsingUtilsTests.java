package gms.shared.utilities.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gms.shared.utilities.service.RequestParsingUtils.DeserializationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.jackson.dataformat.MessagePackFactory;

public class RequestParsingUtilsTests {

  private static final ObjectMapper jsonMapper = new ObjectMapper();
  private static final ObjectMapper msgpackMapper = new ObjectMapper(new MessagePackFactory());
  private ObjectNode basicNode;

  @Before
  public void init() {
    basicNode = jsonMapper.createObjectNode().put("name", "fred");
  }

  @Test
  public void extractRequestJsonTest() throws Exception {
    final Request request = mockJsonRequest(basicNode);
    final JsonNode jsonNode = RequestParsingUtils.extractRequest(request, jsonMapper);
    assertEquals(basicNode, jsonNode);
  }

  @Test
  public void extractRequestJsonTestMsgpack() throws Exception {
    final Request request = mockMsgpackRequest(basicNode);
    final JsonNode jsonNode = RequestParsingUtils.extractRequest(request, msgpackMapper);
    assertEquals(basicNode, jsonNode);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestJsonTestNullbody() throws Exception {
    RequestParsingUtils.extractRequest(mockJsonRequest(null), jsonMapper);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestJsonTestNullbodyMsgpack() throws Exception {
    RequestParsingUtils.extractRequest(mockMsgpackRequest(null), msgpackMapper);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestJsonTestMsgpackRequestButJsonMapper() throws Exception {
    final Request request = mockMsgpackRequest(basicNode);
    RequestParsingUtils.extractRequest(request, jsonMapper);
  }

  @Test
  public void extractRequestTest() throws Exception {
    final Request request = mockJsonRequest(basicNode);
    final ObjectNode objectNode2 = RequestParsingUtils.extractRequest(
        request, jsonMapper, ObjectNode.class);
    assertNotSame(basicNode, objectNode2);
    assertEquals(basicNode, objectNode2);
  }

  @Test
  public void extractRequestTestMsgpack() throws Exception {
    final Request request = mockMsgpackRequest(basicNode);
    final ObjectNode objectNode2 = RequestParsingUtils.extractRequest(
        request, msgpackMapper, ObjectNode.class);
    assertNotSame(basicNode, objectNode2);
    assertEquals(basicNode, objectNode2);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestAsWrongTypeTest() throws Exception {
    RequestParsingUtils.extractRequest(mockJsonRequest("foo"),
        jsonMapper, Integer.class);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestNullBodyTest() throws Exception {
    RequestParsingUtils.extractRequest(mockJsonRequest(null),
        jsonMapper, String.class);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestAsWrongTypeMsgpackTest() throws Exception {
    RequestParsingUtils.extractRequest(mockMsgpackRequest("foo"),
        msgpackMapper, Integer.class);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestNullBodyMsgpackTest() throws Exception {
    RequestParsingUtils.extractRequest(mockMsgpackRequest(null),
        msgpackMapper, String.class);
  }

  @Test
  public void extractRequestListTest() throws Exception {
    final List<UUID> body = List.of(UUID.randomUUID(), UUID.randomUUID());
    final Request request = mockJsonRequest(body);
    final List<UUID> deserialized = RequestParsingUtils.extractRequestList(
        request, jsonMapper, UUID.class);
    assertNotSame(body, deserialized);
    assertEquals(body, deserialized);
  }

  @Test
  public void extractRequestListMsgpackTest() throws Exception {
    final List<UUID> body = List.of(UUID.randomUUID(), UUID.randomUUID());
    final Request request = mockMsgpackRequest(body);
    final List<UUID> deserialized = RequestParsingUtils.extractRequestList(
        request, msgpackMapper, UUID.class);
    assertNotSame(body, deserialized);
    assertEquals(body, deserialized);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestListAsWrongTypeTest() throws Exception {
    RequestParsingUtils.extractRequestList(mockJsonRequest(
        List.of("foo")), jsonMapper, Integer.class);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestListNullBodyTest() throws Exception {
    RequestParsingUtils.extractRequestList(
        mockJsonRequest(null), jsonMapper, String.class);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestListAsWrongTypeMsgpackTest() throws Exception {
    RequestParsingUtils.extractRequestList(mockMsgpackRequest(
        List.of("foo")), msgpackMapper, Integer.class);
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestListNullBodyMsgpackTest() throws Exception {
    RequestParsingUtils.extractRequestList(
        mockMsgpackRequest(null), msgpackMapper, String.class);
  }

  @Test
  public void extractRequestElementFromJsonNodeTest() throws Exception {
    final String stringValueKey = "stringVal";
    final String stringValue = "i am a simple string";
    final ObjectNode requestNode = jsonMapper.createObjectNode().put(stringValueKey, stringValue);
    final String subObjectKey = "objectVal";
    final ObjectNode subObject = jsonMapper.readValue("{ \"name\": \"Roy\", \"age\": 35 }",
        ObjectNode.class);
    requestNode.set(subObjectKey, subObject);
    final String subArrayKey = "arrayVal";
    final ArrayNode subArray = jsonMapper.readValue("[ \"hello\", \"gms\" ]",
        ArrayNode.class);
    requestNode.set(subArrayKey, subArray);

    assertEquals(Optional.of(stringValue), RequestParsingUtils.extractRequestElement(requestNode,
        jsonMapper, stringValueKey, String.class));
    assertEquals(Optional.of(subObject), RequestParsingUtils.extractRequestElement(requestNode,
        jsonMapper, subObjectKey, ObjectNode.class));
    assertEquals(Optional.of(subArray), RequestParsingUtils.extractRequestElement(requestNode,
        jsonMapper, subArrayKey, ArrayNode.class));

    // try extracting non-existent key, get empty back.
    assertFalse(RequestParsingUtils.extractRequestElement(
        requestNode, jsonMapper, "noSuchKey", String.class).isPresent());
  }

  @Test
  public void extractRequiredRequestElementFromJsonNodeTest() throws Exception {
    final String stringValueKey = "stringVal";
    final String stringValue = "i am a simple string";
    final ObjectNode requestNode = jsonMapper.createObjectNode().put(stringValueKey, stringValue);
    final String subObjectKey = "objectVal";
    final ObjectNode subObject = jsonMapper.readValue("{ \"name\": \"Roy\", \"age\": 35 }",
        ObjectNode.class);
    requestNode.set(subObjectKey, subObject);
    final String subArrayKey = "arrayVal";
    final ArrayNode subArray = jsonMapper.readValue("[ \"hello\", \"gms\" ]",
        ArrayNode.class);
    requestNode.set(subArrayKey, subArray);

    assertEquals(stringValue, RequestParsingUtils.extractRequiredRequestElement(requestNode,
        jsonMapper, stringValueKey, String.class));
    assertEquals(subObject, RequestParsingUtils.extractRequiredRequestElement(requestNode,
        jsonMapper, subObjectKey, ObjectNode.class));
    assertEquals(subArray, RequestParsingUtils.extractRequiredRequestElement(requestNode,
        jsonMapper, subArrayKey, ArrayNode.class));

    try {
      RequestParsingUtils.extractRequiredRequestElement(
          requestNode, jsonMapper, "noSuchKey", String.class);
      fail("Expected DeserializationException caused by noSuchKey");
    } catch (DeserializationException de) {
      // Expected -- go ahead and make a new test if you must
    }
  }

  @Test
  public void extractRequiredRequestElementFromRequestTest() throws Exception {
    final String stringValueKey = "stringVal";
    final String stringValue = "i am a simple string";
    final ObjectNode requestNode = jsonMapper.createObjectNode().put(stringValueKey, stringValue);
    final String subObjectKey = "objectVal";
    final ObjectNode subObject = jsonMapper.readValue("{ \"name\": \"Roy\", \"age\": 35 }",
        ObjectNode.class);
    requestNode.set(subObjectKey, subObject);
    final String subArrayKey = "arrayVal";
    final ArrayNode subArray = jsonMapper.readValue("[ \"hello\", \"gms\" ]",
        ArrayNode.class);
    requestNode.set(subArrayKey, subArray);

    Request request = mockJsonRequest(requestNode);

    assertEquals(stringValue, RequestParsingUtils.extractRequiredRequestElement(request,
        jsonMapper, stringValueKey, String.class));
    assertEquals(subObject, RequestParsingUtils.extractRequiredRequestElement(request,
        jsonMapper, subObjectKey, ObjectNode.class));
    assertEquals(subArray, RequestParsingUtils.extractRequiredRequestElement(request,
        jsonMapper, subArrayKey, ArrayNode.class));

    try {
      RequestParsingUtils.extractRequiredRequestElement(
          request, jsonMapper, "noSuchKey", String.class);
      fail("Expected DeserializationException caused by noSuchKey");
    } catch (DeserializationException de) {
      // Expected -- go ahead and make a new test if you must
    }
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestElementFromJsonNodeAsWrongTypeTest() throws Exception {
    RequestParsingUtils.extractRequestElement(basicNode, jsonMapper,
        "name", Integer.class);
  }

  @Test
  public void extractRequestElementFromRequestTest() throws Exception {
    extractRequestElementFromRequestTestHelper(false);
  }

  @Test
  public void extractRequestElementFromRequestMsgpackTest() throws Exception {
    extractRequestElementFromRequestTestHelper(true);
  }

  private static void extractRequestElementFromRequestTestHelper(boolean msgpack) throws Exception {
    final ObjectMapper mapper = msgpack ? msgpackMapper : jsonMapper;
    final String stringValueKey = "stringVal";
    final String stringValue = "i am a simple string";
    final ObjectNode requestNode = mapper.createObjectNode().put(stringValueKey, stringValue);
    final String subObjectKey = "objectVal";
    final ObjectNode subObject = mapper.readValue("{ \"name\": \"Roy\", \"age\": 35 }",
        ObjectNode.class);
    requestNode.set(subObjectKey, subObject);
    final String subArrayKey = "arrayVal";
    final ArrayNode subArray = mapper.readValue("[ \"hello\", \"gms\" ]",
        ArrayNode.class);
    requestNode.set(subArrayKey, subArray);

    final Request request = msgpack ? mockMsgpackRequest(requestNode) : mockJsonRequest(requestNode);

    assertEquals(Optional.of(stringValue), RequestParsingUtils.extractRequestElement(request,
        mapper, stringValueKey, String.class));
    assertEquals(Optional.of(subObject), RequestParsingUtils.extractRequestElement(request,
        mapper, subObjectKey, ObjectNode.class));
    assertEquals(Optional.of(subArray), RequestParsingUtils.extractRequestElement(request,
        mapper, subArrayKey, ArrayNode.class));

    // try extracting non-existent key, get empty back.
    assertFalse(RequestParsingUtils.extractRequestElement(
        request, mapper, "noSuchKey", String.class).isPresent());
  }

  @Test(expected = DeserializationException.class)
  public void extractRequestElementFromRequestAsWrongTypeTest() throws Exception {
    RequestParsingUtils.extractRequestElement(mockJsonRequest(basicNode),
        jsonMapper, "name", Integer.class);
  }

  @Test
  public void extractRequestElementListFromJsonNodeTest() throws Exception {
    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(Map.of(key, elementList)));
    final List<Integer> retrievedElementList = RequestParsingUtils.extractRequestElementList(
        node, jsonMapper, key, Integer.class)
        .orElseThrow(() -> new RuntimeException("Failed to find element by key " + key));
    assertNotSame(elementList, retrievedElementList);
    assertEquals(elementList, retrievedElementList);
  }

  @Test
  public void extractRequiredRequestElementListFromJsonNodeTest() throws Exception {

    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(Map.of(key, elementList)));
    final List<Integer> retrievedElementList = RequestParsingUtils.extractRequiredRequestElementList(
        node, jsonMapper, key, Integer.class);

    assertNotSame(elementList, retrievedElementList);
    assertEquals(elementList, retrievedElementList);
  }

  @Test
  public void extractRequiredRequestElementListFromRequestTest() throws Exception {

    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(Map.of(key, elementList)));

    final Request request = mockJsonRequest(node);

    final List<Integer> retrievedElementList = RequestParsingUtils.extractRequiredRequestElementList(
        request, jsonMapper, key, Integer.class);

    assertNotSame(elementList, retrievedElementList);
    assertEquals(elementList, retrievedElementList);

    // Perform a quick check to ensure it throws an exception when asked for an element
    // not present.
    try {
      RequestParsingUtils.extractRequiredRequestElementList(request, jsonMapper, "notThere",
          Double.class);
      fail("Expected an exception cause by missing key");
    } catch (DeserializationException de) {
      // Expected
    }
  }

  @Test
  public void testExtractRequestElementListFromJsonNodeWrongKey() throws Exception {
    assertFalse(RequestParsingUtils.extractRequestElementList(
        basicNode, jsonMapper, "noSuchKey", String.class).isPresent());
  }

  @Test(expected = DeserializationException.class)
  public void testExtractRequestElementListFromJsonNodeWrongType() throws Exception {
    final String key = "key";
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(
        Map.of(key, List.of(1, 2, 3))));
    RequestParsingUtils.extractRequestElementList(
        node, jsonMapper, key, Boolean.class);
  }

  @Test
  public void extractRequestElementListFromRequestTest() throws Exception {
    extractRequestElementListFromRequestTestHelper(false);
  }

  @Test
  public void extractRequestElementListFromRequestMsgpackTest() throws Exception {
    extractRequestElementListFromRequestTestHelper(true);
  }

  private static void extractRequestElementListFromRequestTestHelper(boolean msgpack) throws Exception {
    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final Map<String, List<Integer>> body = Map.of(key, elementList);
    final Request request = msgpack ? mockMsgpackRequest(body) : mockJsonRequest(body);
    final ObjectMapper mapper = msgpack ? msgpackMapper : jsonMapper;
    final List<Integer> retrievedElementList = RequestParsingUtils.extractRequestElementList(
        request, mapper, key, Integer.class)
        .orElseThrow(() -> new RuntimeException("Failed to find element by key " + key));
    assertNotSame(elementList, retrievedElementList);
    assertEquals(elementList, retrievedElementList);
  }

  @Test
  public void testExtractRequestElementListFromRequestWrongKey() throws Exception {
    assertFalse(RequestParsingUtils.extractRequestElementList(
        mockJsonRequest(basicNode), jsonMapper, "noSuchKey", String.class).isPresent());
  }

  @Test(expected = DeserializationException.class)
  public void testExtractRequestElementListFromRequestWrongType() throws Exception {
    final String key = "key";
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(
        Map.of(key, List.of(1, 2, 3))));
    RequestParsingUtils.extractRequestElementList(
        mockJsonRequest(node), jsonMapper, key, Boolean.class);
  }

  private static Request mockJsonRequest(Object content) throws IOException {
    final Request r = mock(Request.class);
    given(r.getContentType())
        .willReturn(Optional.of(ContentType.APPLICATION_JSON));
    given(r.getBody())
        .willReturn(jsonMapper.writeValueAsString(content));
    given(r.clientSentMsgpack()).willReturn(false);
    return r;
  }

  private static Request mockMsgpackRequest(Object content) throws IOException {
    final Request r = mock(Request.class);
    given(r.getContentType())
        .willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));
    given(r.getRawBody())
        .willReturn(msgpackMapper.writeValueAsBytes(content));
    given(r.clientSentMsgpack()).willReturn(true);
    return r;
  }
}
