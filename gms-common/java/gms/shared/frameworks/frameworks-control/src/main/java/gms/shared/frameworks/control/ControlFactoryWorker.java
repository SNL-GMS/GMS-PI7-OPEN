package gms.shared.frameworks.control;

import gms.shared.frameworks.common.annotations.Control;
import gms.shared.frameworks.service.HttpService;
import gms.shared.frameworks.service.ServiceDefinition;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.AnnotationUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for creating control instances and services from control classes.
 */
class ControlFactoryWorker {

  private static final Logger logger = LoggerFactory.getLogger(ControlFactoryWorker.class);

  /**
   * Runs a HTTP service based on the provided controlClass.  The controlClass
   * contains handler operations accessed by the HttpService's {@link
   * gms.shared.frameworks.service.Route}s.  The controlClass must be annotated with {@link
   * Control}.  The controlClass instance is created by invoking the public static factory operation
   * which accepts a {@link ControlContext} and returns an instance of the control class.  The
   * factory is invoked using a {@link ControlContext} constructed entirely from default framework
   * implementations.  The HttpService has a Route for each {@link Path} annotated operation in the
   * controlClass.  The {@link HttpService#stop()} operation will be invoked when the application's
   * JVM shuts down.
   *
   * @param controlClass type of control to instantiate and wrap in a service (using the {@link
   * Path} annotated operations), not null
   * @param <T> control class type
   * @throws IllegalArgumentException if the controlClass or HttpService instances can't be created
   * @throws IllegalArgumentException if the controlClass is not annotated with {@link Control}
   */
  <T> void runService(Class<T> controlClass) {
    createService(controlClass, createControlContext(controlClass)).start();
  }

  /**
   * Obtains a started {@link HttpService} based on the provided controlClass and controlContext.  The controlClass
   * contains handler operations accessed by the HttpService's {@link
   * gms.shared.frameworks.service.Route}s.  The controlClass must be annotated with {@link
   * Control}.  The controlClass instance is created by invoking the public static factory operation
   * which accepts a {@link ControlContext} and returns an instance of the control class.  The
   * factory is invoked using a {@link ControlContext} constructed entirely from default framework
   * implementations.  The HttpService has a Route for each {@link Path} annotated operation in the
   * controlClass.  The {@link HttpService#stop()} operation will be invoked when the application's
   * JVM shuts down.
   *
   * @param controlClass type of control to instantiate and wrap in a service (using the {@link
   * Path} annotated operations), not null
   * @param <T> control class type
   * @param controlContext specific controlContext to use for constructing the control class
   * @return an {@link HttpService} for the controlClass that is started (see {@link
   * HttpService#start()} and ready to receive service requests, not null.
   * @throws IllegalArgumentException if the controlClass or HttpService instances can't be created
   * @throws IllegalArgumentException if the controlClass is not annotated with {@link Control}
   */
  <T> HttpService createService(Class<T> controlClass, ControlContext controlContext) {
    // Instantiate the control using a discovered factoryOperation and provided ControlContext
    final T control = createControl(controlClass, controlContext);
    // Create an HttpService using Control's RouteHandlers
    logger.info("Creating HttpService for @Path operations from {}", controlClass);
    return new HttpService(createServiceDefinition(
        control, controlContext.getSystemConfig()));
  }

  /**
   * Obtains a new instance of the provided controlClass. The controlClass must be annotated with
   * {@link Control}.  The controlClass instance is created by invoking the public static factory
   * operation which accepts a {@link ControlContext} and returns an instance of the control class.
   * The factory is invoked using a {@link ControlContext} constructed entirely from default
   * framework implementations.
   *
   * @param controlClass type of control to instantiate using the Control annotation, not null
   * @param <T> control class type
   * @return instance of the control class, not null
   * @throws IllegalArgumentException if the controlClass instance can't be created using the class'
   * factory operation or the correct factory operation doesn't exist in the class
   * @throws IllegalArgumentException if the controlClass is not annotated with {@link Control}
   */
  <T> T createControl(Class<T> controlClass) {
    return createControl(controlClass, createControlContext(controlClass));
  }

  /**
   * Obtains a new instance of the provided controlClass. The controlClass must be annotated with
   * {@link Control}.  The controlClass instance is created by invoking the public static factory
   * operation which accepts a {@link ControlContext} and returns an instance of the control class.
   * The factory is invoked using a {@link ControlContext} constructed entirely from default
   * framework implementations.
   *
   * @param controlClass type of control to instantiate using the Control annotation, not null
   * @param <T> control class type
   * @param controlContext control context to use.
   * @return instance of the control class, not null
   * @throws IllegalArgumentException if the controlClass instance can't be created using the class'
   * factory operation or the correct factory operation doesn't exist in the class
   * @throws IllegalArgumentException if the controlClass is not annotated with {@link Control}
   */
  <T> T createControl(Class<T> controlClass, ControlContext controlContext) {
    Objects.requireNonNull(controlClass, "controlClass must be non-null");
    Objects.requireNonNull(controlContext, "controlContext must be non-null");
    logger.info("Creating control class instance for: {}", controlClass);
    final Method creator = findControlCreatorMethod(controlClass);
    return instantiateControl(controlClass, creator, controlContext);
  }

  /**
   * Obtains a default {@link ControlContext} for the control with name provided by the controlClass
   * {@link Control} annotation value
   *
   * @param controlClass type of the control class, not null
   * @return {@link ControlContext}, not null
   * @throws IllegalArgumentException if the controlClass does not have an {@link Control}
   * annotation
   */
  ControlContext createControlContext(Class<?> controlClass) {
    final String controlName = AnnotationUtils.findClassAnnotation(controlClass, Control.class)
        .orElseThrow(() -> new IllegalArgumentException(
            "Can only instantiate classes annotated with @Control.")).value();
    return ControlContext.builder(controlName).build();
  }

  /**
   * Obtains a {@link ServiceDefinition} for the provided control's {@link Path} operations.
   *
   * @param control instance providing {@link Path} annotated handler operations
   * @return {@link ServiceDefinition}, not null
   */
  private static ServiceDefinition createServiceDefinition(Object control, SystemConfig sysConfig) {
    Objects.requireNonNull(control, "Cannot create service for null control instance");
    Objects.requireNonNull(sysConfig, "Cannot create service with null system config");
    try {
      return ServiceDefinition.builder(sysConfig.getServerConfig())
          .setRoutes(RouteGenerator.generate(control)).build();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Could not create a service definition for the provided control.", e);
    }
  }

  /**
   * Instantiate an instance of the controlClass by invoking the provided creator {@link Method}
   * with the provided {@link ControlContext}
   *
   * @param controlClass type of control to instantiate, not null
   * @param creator {@link Method} used to create the control instance, not null
   * @param controlContext {@link ControlContext} used to create the control instance, not null
   * @param <T> control class type
   * @return instance of the control class, not null
   * @throws IllegalArgumentException if the controlClass instance can't be created using the
   * creator method
   */
  private static <T> T instantiateControl(Class<T> controlClass, Method creator,
      ControlContext controlContext) {
    try {
      creator.setAccessible(true);
      return controlClass.cast(creator.invoke(null, controlContext));

    } catch (Exception e) {
      final String msg = "Could not instantiate the control class by providing a "
          + "ControlContext to the control class' public static factory operation.";
      logger.error(msg, e);
      throw new IllegalArgumentException(msg, e);
    }
  }

  /**
   * Searches controlClass to find a public static controlClass factory operation which accepts a
   * {@link ControlContext}. There must be a single such factory operation on the controlClass.
   *
   * @param controlClass type of Control to instantiate using the Control annotation, not null
   * @return {@link Method} used to create the Control, not null
   * @throws IllegalArgumentException if the controlClass does not have a single annotated {@link
   * Control} operation
   */
  private static Method findControlCreatorMethod(Class<?> controlClass) {
    // Find all operations in controlClass which have the correct signature
    final List<Method> factoryMethods = Arrays.stream(controlClass.getDeclaredMethods())
        .filter(m -> Modifier.isStatic(m.getModifiers()))
        .filter(m -> Modifier.isPublic(m.getModifiers()))
        .filter(m -> controlClass.equals(m.getReturnType()))
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> ControlContext.class.equals(m.getParameterTypes()[0]))
        .collect(Collectors.toList());

    if (factoryMethods.size() != 1) {
      throw new IllegalArgumentException(
          "The controlClass must have a single "
              + "factory operation which is public static, accepts a single ControlContext "
              + "parameter, and return an instance of the controlClass.");
    }
    return factoryMethods.get(0);
  }
}
