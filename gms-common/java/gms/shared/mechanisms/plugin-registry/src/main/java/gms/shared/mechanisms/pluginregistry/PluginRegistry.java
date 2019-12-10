package gms.shared.mechanisms.pluginregistry;

import gms.shared.mechanisms.pluginregistry.exceptions.UnspecifiedNameOrVersionException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginRegistry {

  private static Logger logger = LoggerFactory.getLogger(PluginRegistry.class);

  // Uses initialization on demand holder idiom. The singleton instance of PluginRegistry
  // is lazily instantiation in a thread-safe fashion the first time getRegistry() is called.
  private static class InstanceHolder {
    private static final PluginRegistry instance = new PluginRegistry();
  }

  private Map<PluginInfo, Class<? extends Plugin>> registrationInfoMap;

  private Map<Class<? extends Plugin>, Exception> loadingExceptionMap = new HashMap<>();

  public static PluginRegistry getRegistry() {
    return InstanceHolder.instance;
  }

  private PluginRegistry() {
    registrationInfoMap = new HashMap<>();
  }

  /**
   * Look up a plugin based on base interface and name and version (plugin info)
   *
   * @param pluginInfo Plugin info (name and version)
   * @param typeMatcher Interface that the plugin implements, used only for matching T
   * @param <T> Implementation class
   * @return An instantiation of T
   */
  public <T> Optional<T> lookup(PluginInfo pluginInfo, Class<T> typeMatcher) {
    Class<? extends Plugin> pluginClass = registrationInfoMap.get(pluginInfo);
    if (pluginClass == null) {
      return Optional.empty();
    }

    try {
      Plugin plugin = pluginClass.getConstructor().newInstance();
      runInitialize(plugin);

      //TODO: Possibly allow option not to wrap - will not allow logging but will remove overhead
      return Optional.of(wrapMethodCalls(plugin));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    } catch (NoSuchMethodException e) {
      //NOTE: Unreachable as long as the ServiceLoader tries to find no-arg constructor.
      throw new IllegalStateException(
          "No no-arg constructor; The ServiceLoader failed to throw an exception for some reason.");
    }
  }

  public Map<Class<? extends Plugin>, Exception> getLoadingExceptions() {
    Map<Class<? extends Plugin>, Exception> copy = new HashMap<>();

    loadingExceptionMap.entrySet().forEach(entry -> copy.put(entry.getKey(), entry.getValue()));

    return copy;
  }

  /**
   * Loads and registers all instances of a plugin interface using the provided Plugin Provider
   */
  public void loadAndRegister() {
    if (registrationInfoMap.isEmpty()) {

      ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class);

      serviceLoader.stream().forEach(p -> {
        Class<? extends Plugin> pluginType = p.type();
        if (pluginType.isAnnotationPresent(Name.class)
            && pluginType.isAnnotationPresent(Version.class)) {
          registrationInfoMap.put(
              PluginInfo.from(
                  pluginType.getAnnotation(Name.class).value(),
                  pluginType.getAnnotation(Version.class).value()
              ),
              pluginType
          );
        } else {
          UnspecifiedNameOrVersionException exception = new UnspecifiedNameOrVersionException(pluginType);
          logger.error("Loading error", exception);
          loadingExceptionMap.put(pluginType, exception);
        }
      });
    }
  }

  /**
   * Get information for all plugins that have been registered
   *
   * @return Set of PluginInfos that together describe all registered plugins
   */
  public Set<PluginInfo> getAllPlugins() {
    return registrationInfoMap.keySet();
  }

  /**
   * Get information for all plugins that have been registered and implement the proved interface
   *
   * @param iface Class object that describes the interface to filter by
   * @param <T> Interface type
   * @return Set of PluginInfos that together describe the requested registered plugins
   */
  public <T> Set<PluginInfo> getPluginsByInterface(Class<T> iface) {
    return registrationInfoMap.entrySet().stream()
        .filter(entry -> iface.isAssignableFrom(entry.getValue()))
        .map(Entry::getKey).collect(Collectors.toSet());
  }

  private static void runInitialize(Plugin plugin) {
    List<Method> initializeAnnotatedMethods = Arrays.stream(plugin.getClass().getDeclaredMethods())
        .filter(method -> method.isAnnotationPresent(Initialize.class))
        .collect(Collectors.toList());

    if (initializeAnnotatedMethods.size() == 1) {
      try {
        initializeAnnotatedMethods.get(0).invoke(plugin);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  //TODO: Test this method
  public <T> Set<PluginInfo> getPluginsByParameterizedType(Class<T> iface,
      ParameterizedType parameterizedType) {
    return registrationInfoMap.entrySet().stream()
        .filter(entry -> iface.isAssignableFrom(entry.getValue().getClass()))
        .filter(entry ->
            Arrays.asList(entry.getValue().getClass().getGenericInterfaces())
                .contains(parameterizedType))
        .map(Entry::getKey).collect(Collectors.toSet());

  }

  private static <T> T wrapMethodCalls(Object instance) {
    T wrappedInstance = (T) Proxy.newProxyInstance(
        instance.getClass().getClassLoader(),
        //instance.getClass(). .getInterfaces(),
        classListToArray(ClassUtils.getAllInterfaces(instance.getClass())),
        new LogProxy(instance)
    );

    return wrappedInstance;
  }

  private static Class<?>[] classListToArray(List<Class<?>> classList) {
    Class<?> array[] = new Class<?>[classList.size()];

    for (int i = 0; i < classList.size(); i++) {
      array[i] = classList.get(i);
    }
    return array;
  }

  private static class LogProxy implements InvocationHandler {

    Object instance;

    LogProxy(Object instance) {
      this.instance = instance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

      Object returnValue;

      try {
        returnValue = method.invoke(instance, args);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }

      Optional<Method> classMethod =
          Arrays.stream(instance.getClass().getDeclaredMethods())
              .filter(method1 -> method1.getName().equals(method.getName()))
              .filter(method1 -> Arrays
                  .deepEquals(method1.getParameterTypes(), method.getParameterTypes()))
              .findFirst();

      boolean doesClassAnnotationReferenceMethod =
          instance.getClass().isAnnotationPresent(LogCall.class)
              && Arrays.asList(instance.getClass().getAnnotation(LogCall.class).value())
              .contains(method.getName());

      boolean isMethodAnnotated =
          classMethod.isPresent() && classMethod.get().isAnnotationPresent(LogCall.class);

      if (doesClassAnnotationReferenceMethod || isMethodAnnotated) {
        String name = instance.getClass().getAnnotation(Name.class).value();
        String version = instance.getClass().getAnnotation(Version.class).value();
        logger.info("Method {} on {} version {} called.", method.getName(), name, version);

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
          logger.info("     Parameter {} with type {} set to {}", parameters[i].getName(), parameters[i].getType(), args[i]);
        }

        logger.info("      Returned {} (type: {})", returnValue, returnValue.getClass());
      }

      return returnValue;
    }
  }

}