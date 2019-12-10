package gms.shared.frameworks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.frameworks.service.RequestParsingUtils.DeserializationException;
import gms.shared.frameworks.common.ContentType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RequestParsingUtilsTests {

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper msgpackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();
  private ObjectNode basicNode;

  @BeforeEach
  void init() {
    basicNode = jsonMapper.createObjectNode().put("name", "fred");
  }

  @Test
  void extractRequestJsonTest() throws Exception {
    final Request request = mockJsonRequest(basicNode);
    final JsonNode jsonNode = RequestParsingUtils.extractRequest(request, jsonMapper);
    assertEquals(basicNode, jsonNode);
  }

  @Test
  void extractRequestJsonTestMsgpack() throws Exception {
    final Request request = mockMsgpackRequest(basicNode);
    final JsonNode jsonNode = RequestParsingUtils.extractRequest(request, msgpackMapper);
    assertEquals(basicNode, jsonNode);
  }

  @Test
  void extractRequestJsonTestNullbody() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(mockJsonRequest(null), jsonMapper));
  }

  @Test
  void extractRequestJsonTestNullbodyMsgpack() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(mockMsgpackRequest(null), msgpackMapper));
  }

  @Test
  void extractRequestJsonTestMsgpackRequestButJsonMapper() throws Exception {
    final Request request = mockMsgpackRequest(basicNode);
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(request, jsonMapper));
  }

  @Test
  void extractRequestTest() throws Exception {
    final Request request = mockJsonRequest(basicNode);
    final ObjectNode objectNode2 = RequestParsingUtils.extractRequest(
        request, jsonMapper, ObjectNode.class);
    assertNotSame(basicNode, objectNode2);
    assertEquals(basicNode, objectNode2);
  }

  @Test
  void extractRequestTestMsgpack() throws Exception {
    final Request request = mockMsgpackRequest(basicNode);
    final ObjectNode objectNode2 = RequestParsingUtils.extractRequest(
        request, msgpackMapper, ObjectNode.class);
    assertNotSame(basicNode, objectNode2);
    assertEquals(basicNode, objectNode2);
  }

  @Test
  void extractRequestAsWrongTypeTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(mockJsonRequest("foo"),
            jsonMapper, Integer.class));
  }

  @Test
  void extractRequestNullBodyTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(mockJsonRequest(null),
            jsonMapper, String.class));
  }

  @Test
  void extractRequestAsWrongTypeMsgpackTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(mockMsgpackRequest("foo"),
            msgpackMapper, Integer.class));
  }

  @Test
  void extractRequestNullBodyMsgpackTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequest(mockMsgpackRequest(null),
            msgpackMapper, String.class));
  }

  @Test
  void extractRequestJavaTypeTest() throws Exception {
    final List<Optional<Integer>> body = List.of(
        Optional.of(0), Optional.empty(), Optional.of(2));
    final Request request = mockJsonRequest(body);
    final TypeFactory f = jsonMapper.getTypeFactory();
    final JavaType optionalIntType = f.constructParametricType(
        Optional.class, Integer.class);
    final JavaType listOptionalIntType = f.constructCollectionType(
        Collection.class, optionalIntType);
    final List<Optional<Integer>> deserialized = RequestParsingUtils.extractRequest(
        request, jsonMapper, listOptionalIntType);
    assertNotSame(body, deserialized);
    assertEquals(body, deserialized);
  }

  @Test
  void extractRequestListTest() throws Exception {
    final List<UUID> body = List.of(UUID.randomUUID(), UUID.randomUUID());
    final Request request = mockJsonRequest(body);
    final List<UUID> deserialized = RequestParsingUtils.extractRequestList(
        request, jsonMapper, UUID.class);
    assertNotSame(body, deserialized);
    assertEquals(body, deserialized);
  }

  @Test
  void extractRequestListMsgpackTest() throws Exception {
    final List<UUID> body = List.of(UUID.randomUUID(), UUID.randomUUID());
    final Request request = mockMsgpackRequest(body);
    final List<UUID> deserialized = RequestParsingUtils.extractRequestList(
        request, msgpackMapper, UUID.class);
    assertNotSame(body, deserialized);
    assertEquals(body, deserialized);
  }

  @Test
  void extractRequestListAsWrongTypeTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestList(mockJsonRequest(
            List.of("foo")), jsonMapper, Integer.class));
  }

  @Test
  void extractRequestListNullBodyTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestList(
            mockJsonRequest(null), jsonMapper, String.class));
  }

  @Test
  void extractRequestListAsWrongTypeMsgpackTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestList(mockMsgpackRequest(
            List.of("foo")), msgpackMapper, Integer.class));
  }

  @Test
  void extractRequestListNullBodyMsgpackTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestList(
            mockMsgpackRequest(null), msgpackMapper, String.class));
  }

  @Test
  void extractRequestElementFromJsonNodeTest() throws Exception {
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
  void extractRequiredRequestElementFromJsonNodeTest() throws Exception {
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
  void extractRequiredRequestElementFromRequestTest() throws Exception {
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

  @Test
  void extractRequestElementFromJsonNodeAsWrongTypeTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestElement(basicNode, jsonMapper,
            "name", Integer.class));
  }

  @Test
  void extractRequestElementFromRequestTest() throws Exception {
    extractRequestElementFromRequestTestHelper(false);
  }

  @Test
  void extractRequestElementFromRequestMsgpackTest() throws Exception {
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

    final Request request =
        msgpack ? mockMsgpackRequest(requestNode) : mockJsonRequest(requestNode);

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

  @Test
  void extractRequestElementFromRequestAsWrongTypeTest() {
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestElement(mockJsonRequest(basicNode),
            jsonMapper, "name", Integer.class));
  }

  @Test
  void extractRequestElementListFromJsonNodeTest() throws Exception {
    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final JsonNode node = jsonMapper
        .readTree(jsonMapper.writeValueAsString(Map.of(key, elementList)));
    final List<Integer> retrievedElementList = RequestParsingUtils.extractRequestElementList(
        node, jsonMapper, key, Integer.class)
        .orElseThrow(() -> new RuntimeException("Failed to find element by key " + key));
    assertNotSame(elementList, retrievedElementList);
    assertEquals(elementList, retrievedElementList);
  }

  @Test
  void extractRequiredRequestElementListFromJsonNodeTest() throws Exception {

    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final JsonNode node = jsonMapper
        .readTree(jsonMapper.writeValueAsString(Map.of(key, elementList)));
    final List<Integer> retrievedElementList = RequestParsingUtils
        .extractRequiredRequestElementList(
            node, jsonMapper, key, Integer.class);

    assertNotSame(elementList, retrievedElementList);
    assertEquals(elementList, retrievedElementList);
  }

  @Test
  void extractRequiredRequestElementListFromRequestTest() throws Exception {

    final String key = "key";
    final List<Integer> elementList = List.of(1, 2, 3, 4, 5);
    final JsonNode node = jsonMapper
        .readTree(jsonMapper.writeValueAsString(Map.of(key, elementList)));

    final Request request = mockJsonRequest(node);

    final List<Integer> retrievedElementList = RequestParsingUtils
        .extractRequiredRequestElementList(
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
  void testExtractRequestElementListFromJsonNodeWrongKey() throws Exception {
    assertFalse(RequestParsingUtils.extractRequestElementList(
        basicNode, jsonMapper, "noSuchKey", String.class).isPresent());
  }

  @Test
  void testExtractRequestElementListFromJsonNodeWrongType() throws Exception {
    final String key = "key";
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(
        Map.of(key, List.of(1, 2, 3))));
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestElementList(
            node, jsonMapper, key, Boolean.class));
  }

  @Test
  void extractRequestElementListFromRequestTest() throws Exception {
    extractRequestElementListFromRequestTestHelper(false);
  }

  @Test
  void extractRequestElementListFromRequestMsgpackTest() throws Exception {
    extractRequestElementListFromRequestTestHelper(true);
  }

  private static void extractRequestElementListFromRequestTestHelper(boolean msgpack)
      throws Exception {
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
  void testExtractRequestElementListFromRequestWrongKey() throws Exception {
    assertFalse(RequestParsingUtils.extractRequestElementList(
        mockJsonRequest(basicNode), jsonMapper, "noSuchKey", String.class).isPresent());
  }

  @Test
  void testExtractRequestElementListFromRequestWrongType() throws Exception {
    final String key = "key";
    final JsonNode node = jsonMapper.readTree(jsonMapper.writeValueAsString(
        Map.of(key, List.of(1, 2, 3))));
    assertThrows(DeserializationException.class,
        () -> RequestParsingUtils.extractRequestElementList(
            mockJsonRequest(node), jsonMapper, key, Boolean.class));
  }

  private static Request mockJsonRequest(Object content) throws IOException {
    final Request r = mock(Request.class);
    given(r.getContentType())
        .willReturn(Optional.of(ContentType.JSON));
    given(r.getBody())
        .willReturn(jsonMapper.writeValueAsString(content));
    given(r.clientSentMsgpack()).willReturn(false);
    return r;
  }

  private static Request mockMsgpackRequest(Object content) throws IOException {
    final Request r = mock(Request.class);
    given(r.getContentType())
        .willReturn(Optional.of(ContentType.MSGPACK));
    given(r.getRawBody())
        .willReturn(msgpackMapper.writeValueAsBytes(content));
    given(r.clientSentMsgpack()).willReturn(true);
    return r;
  }
}
