package gms.shared.frameworks.utilities;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gms.shared.frameworks.common.ContentType;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class PathMethodTests {
  @Test
  void testFrom() {
    final String pathString = "pathString";
    final Method method = PathMethodTests.class.getMethods()[0];
    final PathMethod pathMethod = PathMethod.from(pathString, method,
        ContentType.defaultContentType(), ContentType.MSGPACK);

    assertNotNull(pathMethod);
    assertAll(
        () -> assertEquals(pathString, pathMethod.getRelativePath()),
        () -> assertEquals(method, pathMethod.getMethod()),
        () -> assertEquals(pathMethod.getInputFormat(), ContentType.defaultContentType()),
        () -> assertEquals(pathMethod.getOutputFormat(), ContentType.MSGPACK)
    );
  }
}
