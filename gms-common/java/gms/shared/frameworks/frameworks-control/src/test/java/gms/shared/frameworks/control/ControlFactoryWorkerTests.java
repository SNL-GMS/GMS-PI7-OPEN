package gms.shared.frameworks.control;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import gms.shared.frameworks.common.annotations.Control;
import gms.shared.frameworks.service.HttpService;
import gms.shared.frameworks.service.Route;
import gms.shared.frameworks.service.ServiceDefinition;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.ServerConfig;
import java.time.Duration;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlFactoryWorkerTests {

  private final ControlFactoryWorker worker = new ControlFactoryWorker();

  @Mock
  private ControlContext mockControlContext;

  @Mock
  private SystemConfig mockSysConfig;

  @Control("control-name")
  @Path("")
  private static class HttpControl {

    private static final String fooRoute = "foo";
    private static final String fullFooRoute = "/" + fooRoute;

    public static HttpControl create(ControlContext context) {
      return new HttpControl();
    }

    @Path(fooRoute)
    @POST
    public int foo(int input) {
      return input * 2;
    }
  }

  @Test
  void testRunService() {
    final Class<HttpControl> controlClass = HttpControl.class;
    final ControlFactoryWorker spy = spy(worker);
    final ControlContext mockContext = mock(ControlContext.class);
    doReturn(mockContext).when(spy).createControlContext(controlClass);
    final HttpService mockService = mock(HttpService.class);
    doReturn(mockService).when(spy).createService(controlClass, mockContext);
    spy.runService(controlClass);
    verify(mockService).start();
  }

  @Test
  void testCreateService() {
    doReturn(mockSysConfig).when(mockControlContext).getSystemConfig();
    final ServerConfig serverConfig = ServerConfig.from(
        8080, 1, 2, Duration.ofMillis(3));
    doReturn(serverConfig).when(mockSysConfig).getServerConfig();
    final HttpService service = worker.createService(HttpControl.class, mockControlContext);

    assertNotNull(service);
    assertFalse(service.isRunning(), "Expect service to be returned un-started");
    final ServiceDefinition serviceDefinition = service.getDefinition();
    Set<Route> routes = serviceDefinition.getRoutes();

    assertAll(
        () -> assertEquals(1, routes.size()),
        () -> assertTrue(routes.stream()
            .findFirst()
            .map(Route::getPath)
            .filter(HttpControl.fullFooRoute::equals)
            .isPresent()),
        () -> assertEquals(serverConfig, serviceDefinition.getServerConfig())
    );
  }

  @Control("control-name")
  private static class ControlRouteHandlerInvalidPath {

    public static ControlRouteHandlerInvalidPath create(ControlContext context) {
      return new ControlRouteHandlerInvalidPath();
    }

    @Path("/|`^")
    public Object handleInt(int foo) {
      return -1;
    }
  }

  @Test
  void testCreateServiceRouteGenerationFailsExpectIllegalArgumentException() {
    doReturn(mockSysConfig).when(mockControlContext).getSystemConfig();
    assertEquals("Could not create a service definition for the provided control.",
        assertThrows(IllegalArgumentException.class,
            () -> worker.createService(ControlRouteHandlerInvalidPath.class, mockControlContext))
            .getMessage());
  }

  // *** Verify correct behavior when instantiating a correctly annotated Control class

  /**
   * Test that createControl(class) calls createControl(class, context) properly
   */
  @Test
  void testCreateControlWithoutControlContext() {
    final Class<ControlClass> controlClass = ControlClass.class;
    final ControlFactoryWorker spy = spy(worker);
    final ControlContext mockContext = mock(ControlContext.class);
    doReturn(mockContext).when(spy).createControlContext(controlClass);
    final ControlClass control = spy.createControl(controlClass);
    assertNotNull(control);
    verify(spy).createControl(controlClass, mockContext);
  }

  @Test
  void testCreateControlWithControlContext() {
    assertNotNull(worker.createControl(ControlClass.class, mockControlContext));
  }

  @Test
  void testCreateControlNullControlClassExpectNullPointerException() {
    assertEquals("controlClass must be non-null",
        assertThrows(NullPointerException.class,
            () -> worker.createControl(null, mock(ControlContext.class)))
            .getMessage());
  }

  @Test
  void testCreateControlNullControlContextExpectNullPointerException() {
    assertEquals("controlContext must be non-null",
        assertThrows(NullPointerException.class,
            () -> worker.createControl(String.class, null))
            .getMessage());
  }

  @Control("control-name")
  private static class ControlClass {

    // Make sure can't directly instantiate a Control
    private ControlClass() {
    }

    public static ControlClass create(ControlContext context) {
      return new ControlClass();
    }
  }

  // *** Verify exceptions when creating Control classes with incorrect factory method signatures
  private void verifySingleValidCreatorOperation(Class<?> controlClass) {
    assertEquals(
        "The controlClass must have a "
            + "single factory operation which is public static, accepts a single ControlContext "
            + "parameter, and return an instance of the controlClass.",

        assertThrows(IllegalArgumentException.class,
            () -> worker.createControl(controlClass, mockControlContext)).getMessage());
  }

  // *** Attempt to instantiate control class with missing or duplicatr factory methods
  @Test
  void testCreateControlNoCreatorsExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlNoCreators.class);
  }

  @Test
  void testCreateControlTwoCreatorsExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlTwoCreators.class);
  }

  @Control("control-name")
  private static class ControlNoCreators {

  }

  @Control("control-name")
  private static class ControlTwoCreators {

    public static ControlTwoCreators createA(ControlContext context) {
      return new ControlTwoCreators();
    }

    public static ControlTwoCreators createB(ControlContext context) {
      return new ControlTwoCreators();
    }
  }

  // *** Attempt to instantiate Control classes with incorrect Control visibility

  @Test
  void testCreateControlProtectedCreatorExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlProtectedCreator.class);
  }

  @Test
  void testCreateControlPackagePrivateCreatorExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlPackagePrivateCreator.class);
  }

  @Test
  void testCreateControlPrivateCreatorExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlPrivateCreator.class);
  }

  @Control("control-name")
  private static class ControlProtectedCreator {


    protected static ControlProtectedCreator create(ControlContext context) {
      return new ControlProtectedCreator();
    }
  }

  @Control("control-name")
  private static class ControlPackagePrivateCreator {

    static ControlPackagePrivateCreator create(ControlContext context) {
      return new ControlPackagePrivateCreator();
    }
  }

  @Control("control-name")
  private static class ControlPrivateCreator {

    private static ControlPrivateCreator create(ControlContext context) {
      return new ControlPrivateCreator();
    }
  }

  // *** Attempt to instantiate Control classes with non-static Control

  @Test
  void testCreateControlNonStaticCreatorExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlNotStatic.class);
  }

  @Control("control-name")
  private static class ControlNotStatic {

    public ControlNotStatic create(ControlContext context) {
      return new ControlNotStatic();
    }
  }

  // *** Attempt to instantiate Control class with factory method that returns the incorrect type

  @Test
  void testCreateControlCreatorReturnsVoidExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlVoidReturn.class);
  }

  @Test
  void testCreateControlCreatorReturnsOtherTypeExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlIntegerReturn.class);
  }

  @Control("control-name")
  private static class ControlVoidReturn {

    public static void create(ControlContext context) {
    }
  }

  @Control("control-name")
  private static class ControlIntegerReturn {

    public static Integer create(ControlContext context) {
      return 10;
    }
  }

  // *** Attempt to instantiate Control class using factory method with incorrect parameters
  @Test
  void testCreateControlCreatorNoArgumentsExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlNoArguments.class);
  }

  @Test
  void testCreateControlCreatorTwoArgumentsExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlTwoArguments.class);
  }

  @Test
  void testCreateControlCreatorIntegerArgumentExpectIllegalArgumentException() {
    verifySingleValidCreatorOperation(ControlIntegerArgument.class);
  }

  @Control("control-name")
  private static class ControlNoArguments {

    public static ControlNoArguments create() {
      return new ControlNoArguments();
    }
  }

  @Control("control-name")
  private static class ControlTwoArguments {

    public static ControlTwoArguments create(ControlContext one, ControlContext two) {
      return new ControlTwoArguments();
    }
  }

  @Control("control-name")
  private static class ControlIntegerArgument {

    public static ControlIntegerArgument create(Integer integer) {
      return new ControlIntegerArgument();
    }
  }

  // *** Verify exceptions when fails to instantiate controlClass

  @Test
  void testCreateControlFailsExpectIllegalArgumentException() {
    verifyControlCreationThrowsExceptionOnError(worker::createControl);
  }

  private void verifyControlCreationThrowsExceptionOnError(
      BiFunction<Class<?>, ControlContext, ?> creatorMethod) {
    assertEquals(
        "Could not instantiate the control class by providing a "
            + "ControlContext to the control class' public static factory operation.",

        assertThrows(IllegalArgumentException.class,
            () -> creatorMethod.apply(ControlCreateThrowsException.class, mockControlContext))
            .getMessage());
  }

  @Control("control-name")
  private static class ControlCreateThrowsException {

    public static ControlCreateThrowsException create(ControlContext context) {
      throw new IllegalStateException("Can't instantiate ControlCreateThrowsException");
    }
  }

  // *** Verify exception when class does not have a Control annotation

  @Test
  void testCreateControlClassDoesNotHaveControlAnnotationExpectIllegalArgumentException() {
    verifyCreateThrowsExceptionWhenNoControlAnnotationOnClass(worker::createControl);
  }

  private static void verifyCreateThrowsExceptionWhenNoControlAnnotationOnClass(
      Function<Class<?>, ?> creator) {
    assertEquals("Can only instantiate classes annotated with @Control.",
        assertThrows(IllegalArgumentException.class, () -> creator.apply(Object.class))
            .getMessage());
  }
}
