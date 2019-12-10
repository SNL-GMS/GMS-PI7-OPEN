package gms.shared.frameworks.utilities;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.frameworks.common.ContentType;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.junit.jupiter.api.Test;

class ServiceReflectionUtilitiesTests {

  @Test
  void testFindPathAnnotatedMethodsConsumesMultipleThrows() {
    @Path("base")
    class Handler {

      @Consumes({ContentType.MSGPACK_NAME, "application/json"})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertExactlyOneContentTypeAllowedException(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsConsumesEmptyThrows() {
    @Path("base")
    class Handler {

      @Consumes({})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertExactlyOneContentTypeAllowedException(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsProducesMultipleThrows() {
    @Path("base")
    class Handler {

      @Produces({ContentType.MSGPACK_NAME, "application/json"})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertExactlyOneContentTypeAllowedException(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsProducesEmptyThrows() {
    @Path("base")
    class Handler {

      @Produces({})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertExactlyOneContentTypeAllowedException(Handler.class);
  }

  private static void assertExactlyOneContentTypeAllowedException(Class c) {
    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(c),
        "Exactly one content type allowed"
    );
  }

  @Test
  void testFindPathAnnotatedMethodsConsumesNoArgumentThrows() {
    @Path("")
    class Handler {

      @Consumes
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertUnknownContentTypeThrown(Handler.class, "*/*");
  }

  @Test
  void testFindPathAnnotatedMethodsProducesNoArgumentThrows() {
    @Path("")
    class Handler {

      @Produces
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertUnknownContentTypeThrown(Handler.class, "*/*");
  }

  @Test
  void testFindPathAnnotatedMethodsConsumesBadArgumentThrows() {
    final String badContentType = "foo";
    @Path("")
    class Handler {

      @Consumes({badContentType})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertUnknownContentTypeThrown(Handler.class, badContentType);
  }

  @Test
  void testFindPathAnnotatedMethodsProducesBadArgumentThrows() {
    final String badContentType = "foo";
    @Path("")
    class Handler {

      @Produces({badContentType})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }
    }
    assertUnknownContentTypeThrown(Handler.class, badContentType);
  }

  private static void assertUnknownContentTypeThrown(Class c, String contentType) {
    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(c),
        "Unknown content type: " + contentType
    );
  }

  @Test
  void testFindPathAnnotatedMethodsProvidesValidMappings() throws NoSuchMethodException {
    @Path("base")
    class Handler {

      @Consumes({ContentType.MSGPACK_NAME})
      @Path("intRoute")
      @POST
      public int foo(Integer input) {
        return input * 2;
      }

      @Produces({ContentType.MSGPACK_NAME})
      @Path("/doubleRoute")
      @POST
      public double bar(Double input) {
        return input * 2;
      }
    }

    final Set<PathMethod> foundPathMethods = ServiceReflectionUtilities
        .findPathAnnotatedMethods(Handler.class);
    assertNotNull(foundPathMethods);

    final Set<PathMethod> expectedMappings = Set.of(
        PathMethod.from("base/intRoute", Handler.class.getMethod("foo", Integer.class),
            ContentType.MSGPACK, ContentType.defaultContentType()),
        PathMethod.from("base/doubleRoute", Handler.class.getMethod("bar", Double.class),
            ContentType.defaultContentType(), ContentType.MSGPACK));

    assertEquals(expectedMappings, foundPathMethods);
  }

  @Test
  void testFindPathAnnotatedMethodsCorrectsSlashesInClassPathAnnotationsNoSlashesInBase() {
    @Path("base")
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    verifySingleFoundPathOperationHasPath(Handler.class, "base/intRoute");
  }

  @Test
  void testFindPathAnnotatedMethodsCorrectsSlashesInClassPathAnnotationsBaseBeginsWithSlash() {
    @Path("/base")
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    verifySingleFoundPathOperationHasPath(Handler.class, "base/intRoute");
  }

  @Test
  void testFindPathAnnotatedMethodsCorrectsSlashesInClassPathAnnotationsBaseEndsWithSlash() {
    @Path("base/")
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    verifySingleFoundPathOperationHasPath(Handler.class, "base/intRoute");
  }

  @Test
  void testFindPathAnnotatedMethodsCorrectsSlashesInClassPathAnnotationsBaseBeginsEndsWithSlash() {
    @Path("/base/")
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    verifySingleFoundPathOperationHasPath(Handler.class, "base/intRoute");
  }

  @Test
  void testFindPathAnnotatedMethodsLeadingWhitespaceInClassPathAnnotationThrows() {
    final String basePath = "   /base/";
    @Path(basePath)
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        "Service's @Path defines an invalid base path of '" + basePath + "'"
    );
  }

  @Test
  void testFindPathAnnotatedMethodsTrailingWhitespaceInClassPathAnnotationThrows() {
    @Path("/base    ")
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        "Service's @Path defines an invalid base path"
    );
  }

  @Test
  void testFindPathAnnotatedMethodsWhitespaceInClassPathAnnotationsThrows() {
    @Path("           /base/    ")
    class Handler {

      @Path("intRoute")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        "Service's @Path defines an invalid base path"
    );
  }

  @Test
  void testFindPathAnnotatedMethodsAllowsVariaticParameterRouteHandler()
      throws NoSuchMethodException {

    @Path("")
    class Handler {

      @Path("/foo")
      @POST
      public int foo(int... a) {
        return a[0];
      }
    }

    final Set<PathMethod> pathMethods = ServiceReflectionUtilities
        .findPathAnnotatedMethods(Handler.class);

    assertEquals(Set.of(PathMethod.from("/foo",
        Handler.class.getMethod("foo", int[].class),
        ContentType.defaultContentType(),
        ContentType.defaultContentType())),
        pathMethods);
  }

  void verifySingleFoundPathOperationHasPath(Class routeHandlerClass, String expectedPath) {
    final Set<PathMethod> foundPathMethods = ServiceReflectionUtilities
        .findPathAnnotatedMethods(routeHandlerClass);

    assertNotNull(foundPathMethods);
    assertAll(
        () -> assertEquals(1, foundPathMethods.size()),
        () -> assertEquals(expectedPath,
            foundPathMethods.stream().findFirst().map(PathMethod::getRelativePath).orElse(null))
    );
  }

  @Test
  void testFindPathAnnotatedMethodsCorrectsSlashesInOperationPathAnnotations() {

    @Path("base/")
    class Handler {

      @Path("intRoute1")
      @POST
      public int foo1(int input) {
        return input * 2;
      }

      @Path("/intRoute2")
      @POST
      public int foo2(int input) {
        return input * 2;
      }

      @Path("intRoute3/")
      @POST
      public int foo3(int input) {
        return input * 2;
      }

      @Path("/intRoute4/")
      @POST
      public int foo4(int input) {
        return input * 2;
      }
    }

    final Set<PathMethod> foundPathMethods = ServiceReflectionUtilities
        .findPathAnnotatedMethods(Handler.class);

    final String expectedPath = "base/intRoute";
    final int numExpectedRoutes = 4;

    final Predicate<String> isExpectedPath = p -> p.startsWith(expectedPath)
        && p.length() == expectedPath.length() + 1 // path ends with a number
        && Character.isDigit(p.charAt(p.length() - 1));

    assertNotNull(foundPathMethods);
    assertAll(
        () -> assertEquals(numExpectedRoutes, foundPathMethods.size()),
        () -> assertTrue(
            foundPathMethods.stream().map(PathMethod::getRelativePath).allMatch(isExpectedPath))
    );
  }

  @Test
  void testFindPathAnnotatedMethodsWhitespaceInOperationPathAnnotationsThrows() {
    @Path("base/")
    class Handler {

      @Path("       intRoute1")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        "@Path operations must have values containing valid relative paths"
    );
  }

  // *** Verify validation checks on class @Path annotation provided base path

  @Test
  void testFindPathAnnotatedMethodsBasePathAnnotationInvalidExpectIllegalArgumentException() {
    @Path("b\\a\\r/")
    class Handler {

      @Path("/foo")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    AssertionUtilities
        .verifyIllegalArgumentException(
            () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
            "Service's @Path defines an invalid base path"
        );
  }

  @Test
  void testFindPathAnnotatedMethodsAnnotatedOperationWithoutPathClassAnnotationExpectIllegalArgumentException() {
    class Handler {

      @Path("/foo")
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        "Classes with @Path annotated operations must also have an @Path "
            + "annotation on the class definition with value providing the base path for all "
            + "routes exposed by that class."
    );
  }

  // *** Verify interface can contain the @Path annotation

  private static final String handlerInterfaceBasePath = "base/";

  @Path(handlerInterfaceBasePath)
  interface HandlerInterface {

    String intRoute = "foo-int";

    @Path(intRoute)
    @POST
    int foo(int input);
  }

  @Test
  void testFindPathAnnotatedMethodsFromInterfacePathTypeAnnotationProvidesBasePath() {
    final Set<PathMethod> foundPathMethods = ServiceReflectionUtilities
        .findPathAnnotatedMethods(HandlerInterface.class);

    assertNotNull(foundPathMethods);
    assertAll(
        () -> assertEquals(1, foundPathMethods.size()),
        () -> assertEquals(handlerInterfaceBasePath + HandlerInterface.intRoute,
            foundPathMethods.stream().findFirst().map(PathMethod::getRelativePath).orElse(null))
    );
  }

  // *** Verify checks on @Path operation signatures

  private static void verifyAnnotatedOperationSignature(Class routeHandlerClass) {
    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(routeHandlerClass),
        "@Path operations must be public, accept one input parameter, return a result, and "
            + "also have an @POST annotation. These @Path operations have incorrect signatures:"
    );
  }

  @Test
  void testFindPathAnnotatedMethodsPrivateRouteHandlerExpectIllegalArgumentException() {
    @Path("")
    class PrivateHandler {

      @Path("/foo")
      @POST
      private int foo(int input) {
        return input * 2;
      }
    }

    verifyAnnotatedOperationSignature(PrivateHandler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsPackagePrivateRouteHandlerExpectIllegalArgumentException() {
    @Path("")
    class PackagePrivateHandler {

      @Path("/foo")
      @POST
      int foo(int input) {
        return input * 2;
      }
    }

    verifyAnnotatedOperationSignature(PackagePrivateHandler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsProtectedRouteHandlerExpectIllegalArgumentException() {
    @Path("")
    class ProtectedHandler {

      @Path("/foo")
      @POST
      protected int foo(int input) {
        return input * 2;
      }
    }

    verifyAnnotatedOperationSignature(ProtectedHandler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsVoidRouteHandlerExpectIllegalArgumentException() {
    @Path("")
    class Handler {

      @Path("/foo")
      @POST
      public void foo(int input) {
      }
    }

    verifyAnnotatedOperationSignature(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsParameterlessRouteHandlerExpectIllegalArgumentException() {
    @Path("")
    class Handler {

      @Path("/foo")
      @POST
      public int foo() {
        return 7;
      }
    }

    verifyAnnotatedOperationSignature(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsManyParametersRouteHandlerExpectIllegalArgumentException() {
    @Path("")
    class Handler {

      @Path("/foo")
      @POST
      public int foo(int a, int b, int c) {
        return 7;
      }
    }

    verifyAnnotatedOperationSignature(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsPathOperationWithoutPostExpectIllegalArgumentException() {
    @Path("/base")
    class Handler {

      @Path("/foo")
      public int foo(int input) {
        return input * 2;
      }
    }

    verifyAnnotatedOperationSignature(Handler.class);
  }

  @Test
  void testFindPathAnnotatedMethodsMultipleIncorrectOperationSignaturesExpectIllegalArgumentException() {
    @Path("")
    class Handler {

      @Path("/foo")
      @POST
      protected int foo(int input) {
        return input * 2;
      }

      @Path("/bar")
      @POST
      int bar(int input) {
        return input * input;
      }
    }

    AssertionUtilities.verifyThrowsWithAllInMessage(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        IllegalArgumentException.class,
        List.of("foo(", "bar(")
    );
  }

  @Test
  void testFindPathAnnotatedMethodsMultiplePathOperationWithoutPostExpectIllegalArgumentException() {
    @Path("")
    class Handler {

      @Path("/foo")
      protected int foo(int input) {
        return input * 2;
      }

      @Path("/bar")
      int bar(int input) {
        return input * input;
      }
    }

    AssertionUtilities.verifyThrowsWithAllInMessage(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        IllegalArgumentException.class,
        List.of("foo(", "bar(")
    );
  }

  @Test
  void testFindPathAnnotatedMethodsPathInvalidExpectIllegalArgumentException() {
    final String invalidPath = "b\\a\\r/";

    @Path("base/")
    class Handler {

      @Path(invalidPath)
      @POST
      public int foo(int input) {
        return input * 2;
      }
    }

    final List<String> messageComponents = List.of(
        "@Path operations must have values containing valid relative paths. The following operations have invalid paths:",
        "foo(");

    AssertionUtilities.verifyThrowsWithAllInMessage(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        IllegalArgumentException.class,
        messageComponents
    );
  }

  @Test
  void testFindPathAnnotatedMethodsMultipleErrorsCreatingRoutesExpectIllegalArgumentException() {
    @Path("")
    class Handler {

      @Path("f\\o\\o")
      @POST
      public int foo(int input) {
        return input * 2;
      }

      @Path("b\\a\\r")
      @POST
      public int bar(int input) {
        return input * input;
      }
    }

    AssertionUtilities.verifyThrowsWithAllInMessage(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        IllegalArgumentException.class,
        List.of("foo(", "bar(")
    );
  }

  @Test
  void testFindPathAnnotatedMethodsDuplicatePathsExpectIllegalArgumentException() {

    @Path("base")
    class Handler {

      @Path("/foo")
      @POST
      public int foo1(int input) {
        return input * 2;
      }

      @Path("/foo")
      @POST
      public int foo2(int input) {
        return input * input;
      }
    }

    AssertionUtilities.verifyIllegalArgumentException(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        "A Service cannot have more than one endpoint operation with the same relative path. "
            + "These relative paths are repeated:"
    );
  }

  @Test
  void testFindPathAnnotatedMethodsDuplicatePathsMultipleDuplicatesExpectIllegalArgumentException() {

    @Path("base")
    class Handler {

      @Path("/foo")
      @POST
      public int foo1(int input) {
        return input * 2;
      }

      @Path("/foo")
      @POST
      public int foo2(int input) {
        return input * input;
      }

      @Path("/bar")
      @POST
      public int bar1(int input) {
        return input * 2;
      }

      @Path("/bar")
      @POST
      public int bar2(int input) {
        return input * input;
      }
    }

    AssertionUtilities.verifyThrowsWithAllInMessage(
        () -> ServiceReflectionUtilities.findPathAnnotatedMethods(Handler.class),
        IllegalArgumentException.class,
        List.of("foo", "bar")
    );
  }

  @Test
  void testFindPathAnnotatedMethodsValidatesParameters() {
    AssertionUtilities
        .verifyNullPointerException(() -> ServiceReflectionUtilities.findPathAnnotatedMethods(null),
            "findPathAnnotatedMethods requires non-null class"
        );
  }

  @Path("")
  interface InterfaceHasMethodWithoutAnnotations {

    @Path("foo-int")
    @POST
    int foo(int input);

    String methodWithoutAnnotations(int x);
  }

  @Test
  void testFindPathAnnotatedMethodsOnlyOrThrowOnInterfaceWithOneNonAnnotatedMethod() {
    AssertionUtilities.verifyThrowsWithAllInMessage(() -> ServiceReflectionUtilities
            .findPathAnnotatedMethodsOnlyOrThrow(InterfaceHasMethodWithoutAnnotations.class),
        IllegalArgumentException.class,
        Set.of("methodWithoutAnnotations", "needs annotations and proper signature")
    );
  }

  @Path("")
  interface InterfaceHasNonAbstractMethod {

    @Path("foo-int")
    @POST
    int foo(int input);

    @Path("non-abstract-very-bad")
    @POST
    default String nonAbstractMethod(int x) {
      return "psyche!";
    }
  }

  @Test
  void testFindPathAnnotatedMethodsOnlyOrThrowOnInterfaceWithOneNonAbstractMethod() {
    AssertionUtilities.verifyThrowsWithAllInMessage(() -> ServiceReflectionUtilities
            .findPathAnnotatedMethodsOnlyOrThrow(InterfaceHasNonAbstractMethod.class),
        IllegalArgumentException.class,
        Set.of("nonAbstractMethod", "need to be abstract (implies not default)")
    );
  }
}
