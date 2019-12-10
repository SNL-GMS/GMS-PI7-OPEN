package gms.shared.frameworks.client.generation;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.client.ServiceClient;
import gms.shared.frameworks.client.ServiceClientJdkHttp;
import gms.shared.frameworks.client.ServiceRequest;
import gms.shared.frameworks.common.annotations.Control;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.AnnotationUtils;
import gms.shared.frameworks.utilities.PathMethod;
import gms.shared.frameworks.utilities.ServiceReflectionUtilities;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates client implementations that use HTTP given an interface.
 */
public class ClientGenerator {

  private ClientGenerator() {
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Creates a proxy instantiation of the given client interface that is
   * implemented using HTTP.
   * @param clientClass the class of the interface
   * @param <T> the type of the interface, same as the return type
   * @return an instance of the client interface
   * @throws NullPointerException if clientClass is null
   * @throws IllegalArgumentException if the clientClass doesn't have @Control
   */
  public static <T> T createClient(Class<T> clientClass) {
    return createClient(clientClass, ServiceClientJdkHttp.create(),
        SystemConfig.create(getControlName(clientClass)));
  }

  /**
   * Creates a proxy instantiation of the given client interface that is
   * implemented using the given HTTP client.
   * @param clientClass the class of the interface
   * @param httpClient the HTTP client to use
   * @param sysConfig the system configuration client, used to lookup
   * connection info (hostname, port, etc.) and configuration for HTTP client
   * @param <T> the type of the interface, same as the return type
   * @return an instance of the client interface
   * @throws NullPointerException if clientClass or httpClient is null
   * @throws IllegalArgumentException if the clientClass doesn't have @Control
   */
  static <T> T createClient(Class<T> clientClass, ServiceClient httpClient,
      SystemConfig sysConfig) {
    Objects.requireNonNull(clientClass, "Cannot create client from null class");
    Objects.requireNonNull(httpClient, "Cannot create client from null httpClient");

    final URL url = sysConfig.getUrl();
    final Duration timeout = sysConfig.getValueAsDuration(SystemConfig.CLIENT_TIMEOUT);
    final Map<Method, PathMethod> pathMethods = pathMethodsByMethod(clientClass);
    return clientClass.cast(Proxy.newProxyInstance(
        ClientGenerator.class.getClassLoader(),
        new Class[]{clientClass}, handler(httpClient, url, timeout, pathMethods)));
  }

  private static String getControlName(Class clientClass) {
    return AnnotationUtils.findClassAnnotation(clientClass, Control.class)
        .orElseThrow(() -> new IllegalArgumentException("Client interface must have @Control"))
        .value();
  }

  private static <T> Map<Method, PathMethod> pathMethodsByMethod(Class<T> clientClass) {
    return ServiceReflectionUtilities.findPathAnnotatedMethodsOnlyOrThrow(clientClass)
        .stream().collect(Collectors.toMap(PathMethod::getMethod, Function.identity()));
  }

  private static InvocationHandler handler(ServiceClient client,
      URL url, Duration timeout, Map<Method, PathMethod> pathMethods) {
    return (proxyObj, method, args) -> sendRequest(
        client, args[0], url, timeout, pathMethods.get(method));
  }

  private static Object sendRequest(ServiceClient httpClient,
      Object requestBody, URL url, Duration timeout, PathMethod method)
      throws MalformedURLException {
    return httpClient.send(ServiceRequest.from(
        appendToUrl(url, method.getRelativePath()), requestBody,
        timeout, getReturnType(method),
        method.getInputFormat(), method.getOutputFormat()));
  }

  private static JavaType getReturnType(PathMethod method) {
    return mapper.constructType(method.getMethod().getGenericReturnType());
  }

  private static URL appendToUrl(URL url, String path) throws MalformedURLException {
    return new URL(url.getProtocol(), url.getHost(), url.getPort(),
        url.getFile() + path, null);
  }
}
