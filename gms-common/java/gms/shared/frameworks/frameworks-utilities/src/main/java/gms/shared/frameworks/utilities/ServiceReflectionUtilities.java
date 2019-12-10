package gms.shared.frameworks.utilities;

import gms.shared.frameworks.common.ContentType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods extracting class and method information useful for automatically
 * generating service wrappers and their corresponding client proxies.
 */
public class ServiceReflectionUtilities {

  private ServiceReflectionUtilities() {
  }

  private static final Logger logger = LoggerFactory.getLogger(ServiceReflectionUtilities.class);

  /**
   * Obtains {@link PathMethod}s for each of the {@link Path} annotated operations in the
   * provided class. Validates the PathMethods have the correct signature (public, one input
   * parameter, return a value, also annotated with {@link POST}).  Validates the PathMethods all
   * have unique paths.  {@link PathMethod#getRelativePath()} on the returned PathMethods includes
   * the base path from the class-level {@link Path} annotation.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Set of {@link PathMethod} containing the {@link Path} annotated handler
   * operations of the class, not null
   * @throws IllegalArgumentException if any of the {@link Path} operations have incorrect
   * signatures; if any of the {@link Path} operations have replicated path strings; if the
   * class-level {@link Path} annotation has an incorrect path
   * @throws NullPointerException if class is null
   */
  public static Set<PathMethod> findPathAnnotatedMethods(Class cls) {

    Objects.requireNonNull(cls, "findPathAnnotatedMethods requires non-null class");

    // Find the methods annotated with @Path
    final Set<PathMethod> pathMethods = findPathMethods(cls);

    // Verify the @Path methods have valid relative paths
    verifyRelativePaths(pathMethods);

    // Verify each path is unique
    verifyUniqueRelativePaths(pathMethods);

    // Verify the @Path methods have the correct signatures
    verifyMethodSignatures(pathMethods);

    return pathMethods;
  }

  /**
   * Obtains {@link PathMethod}s for the {@link Path} annotated operations in the
   * provided class, but throws an exception if any of the methods in the class
   * is not a PathMethod (doesn't have annotations) or isn't abstract.
   * Validates the PathMethods have the correct signature (public, one input
   * parameter, return a value, also annotated with {@link POST}).  Validates the PathMethods all
   * have unique paths.  {@link PathMethod#getRelativePath()} on the returned PathMethods includes
   * the base path from the class-level {@link Path} annotation.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Set of {@link PathMethod} containing the {@link Path} annotated handler
   * operations of the class, not null
   * @throws IllegalArgumentException if there are any operations in the class
   * that are not abstract and/or do not have proper annotations;
   * if any of the {@link Path} operations have incorrect
   * signatures; if any of the {@link Path} operations have replicated path strings; if the
   * class-level {@link Path} annotation has an incorrect path
   * @throws NullPointerException if class is null
   */
  public static Set<PathMethod> findPathAnnotatedMethodsOnlyOrThrow(Class cls) {
    final Set<PathMethod> pathMethods = findPathAnnotatedMethods(cls);
    final Set<Method> declaredMethods = getDeclaredMethods(cls);
    throwIfNotSameMethods(declaredMethods, onlyAbstract(declaredMethods),
        "These methods need to be abstract (implies not default)");
    throwIfNotSameMethods(declaredMethods, methods(pathMethods),
        "These methods are not PathMethod's (needs annotations and proper signature)");
    return pathMethods;
  }

  /**
   * Returns on the methods that are abstract from the provided methods.
   * @param methods the methods to filter
   * @return the subset of the given methods that are abstract
   */
  private static Set<Method> onlyAbstract(Set<Method> methods) {
    return methods.stream().filter(m -> Modifier.isAbstract(m.getModifiers()))
        .collect(Collectors.toSet());
  }

  /**
   * Retrieves all Method's from the given PathMethod's
   * @param pathMethods the PathMethod's
   * @return all of the Method's from the PathMethod's
   */
  private static Set<Method> methods(Set<PathMethod> pathMethods) {
    return pathMethods.stream().map(PathMethod::getMethod).collect(Collectors.toSet());
  }

  /**
   * Throws an exception if the two sets of Method's are not the same,
   * formatting the exceptions nicely.  The set difference is part
   * of the exception message.
   * @param s1 the first set of methods
   * @param s2 the second set of methods
   * @param msg a message to put before the set differences
   * @throws IllegalArgumentException if the two sets are not equal
   */
  private static void throwIfNotSameMethods(Set<Method> s1, Set<Method> s2, String msg) {
    if (!s1.equals(s2)) {
      final Set<Method> diff = difference(s1, s2);
      final Set<String> methodStrs = diff.stream()
          .map(Method::toGenericString).collect(Collectors.toSet());
      throw new IllegalArgumentException(msg + " : " + methodStrs);
    }
  }

  /**
   * Computes set difference.
   * @param s1 the first set
   * @param s2 the second set
   * @param <T> the type of elements in the set
   * @return a set that contains the elements that are in s1 but not in s2
   */
  private static <T> Set<T> difference(Set<T> s1, Set<T> s2) {
    final Set<T> diff = new HashSet<>(s1);
    diff.removeAll(s2);
    return diff;
  }

  /**
   * Finds the methods in cls which have an {@link Path} annotation and constructs a
   * {@link PathMethod} for each of those methods.  The PathMethods include the base path from
   * the class-level {@link Path} annotation. Only base path validation occurs in this
   * operation.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Set of {@link PathMethod} containing the classes {@link Path} annotated handler
   * operations, not null
   */
  private static Set<PathMethod> findPathMethods(Class cls) {
    return findRelativePathsByMethod(cls).entrySet().stream()
        .map(e -> createPathMethod(e.getValue(), e.getKey()))
        .collect(Collectors.toSet());
  }

  /**
   * Finds the methods in cls which have an {@link Path} annotation and constructs a
   * {@link Method} to String map for each of those methods.  The strings are the relative path for
   * the method created by combining the class-level {@link Path} annotation value
   * with the Path annotation value from each Method.
   *
   * @param cls class containing {@link Path} annotated handler operations, not null
   * @return Map of Method to path String for each {@link Path} annotated operation, not null
   */
  private static Map<Method, String> findRelativePathsByMethod(Class cls) {

    // Find the base path used by all of the @Path methods
    final String basePath = getBasePath(cls);

    // Find Methods annotated with @Path and collect them in a (Method -> relativePath) String map
    final Map<Method, String> potentialRouteHandlerMethods = getDeclaredMethods(cls)
        .stream().collect(Collectors.toMap(Function.identity(),
                m -> AnnotationUtils.findMethodAnnotation(m, Path.class)))
            .entrySet().stream()
            .filter(e -> e.getValue().isPresent())
            .collect(Collectors.toMap(
                Entry::getKey, e -> createOperationPath(basePath, e.getValue().get().value())));

    logger.trace("Found {} potential route handler methods", potentialRouteHandlerMethods.size());
    potentialRouteHandlerMethods.keySet()
        .forEach(m -> logger.trace("Potential route handler: {}", m));

    return potentialRouteHandlerMethods;
  }

  private static Set<Method> getDeclaredMethods(Class cls) {
    return new HashSet<>(Arrays.asList(cls.getDeclaredMethods()));
  }

  /**
   * Obtains the base path from the class-level ({@link Path} annotation.  The returned base
   * does not start with / but does have a trailing /
   *
   * @param cls get the value form the Path annotation on this class, not null
   * @return String containing the base url route, not null.
   * @throws IllegalArgumentException if the Path annotation is missing; if the Path annotation's
   * value is not a valid path string
   */
  private static String getBasePath(Class cls) {
    final String message = "Classes with @Path annotated operations must also have an @Path "
        + "annotation on the class definition with value providing the base path for all routes "
        + "exposed by that class.";

    final String basePath = correctPathOnClassFormatting(
        AnnotationUtils.findClassAnnotation(cls, Path.class)
            .map(Path::value)
            .orElseThrow(() -> new IllegalArgumentException(message)));

    if (!isValidPath(basePath)) {
      throw new IllegalArgumentException(
          "Service's @Path defines an invalid base path of '" + basePath + "'");
    }

    return basePath;
  }

  /**
   * Obtain the full path to an operation by combing the basePath with the operation's relative
   * path.  Assumes the provided basePath but not the relative path has the standard path formatting
   * (see {@link ServiceReflectionUtilities#correctPathOnMethodFormatting(String)}.
   *
   * @param basePath base path to the operation, not null
   * @param relativePath operation's path relative to the base path, not null
   * @return String containing a correctly formatted path to the operation, not null
   */
  private static String createOperationPath(String basePath, String relativePath) {
    return basePath + correctPathOnMethodFormatting(relativePath);
  }

  /**
   *
   * Corrects the provided path from @Path on a method: trim whitespace,
   * remove leading and trailing '/' if present.
   *
   * @param path String containing a relative path, not null
   * @return String containing the corrected path
   */
  private static String correctPathOnMethodFormatting(String path) {
    return removeLeadingSlashesIfPresent
        .andThen(removeTrailingSlashesIfPresent).apply(path);
  }

  /**
   *
   * Corrects the provided path from @Path on a class: trim whitespace,
   * remove leading '/' if present, add tailing '/' if not present.
   *
   * @param path String containing a relative path, not null
   * @return String containing the corrected path
   */
  private static String correctPathOnClassFormatting(String path) {
    return removeLeadingSlashesIfPresent.andThen(addTrailSlashIfNotPresent).apply(path);
  }

  private static final UnaryOperator<String> addTrailSlashIfNotPresent
      = s -> StringUtils.appendIfMissing(s, "/");

  private static final UnaryOperator<String> removeTrailingSlashesIfPresent
      = s -> StringUtils.stripEnd(s, "/");

  private static final UnaryOperator<String> removeLeadingSlashesIfPresent
      = s -> StringUtils.stripStart(s, "/");

  /**
   * Obtain a {@link PathMethod} containing information from the provided {@link Method} which has
   * the provided relativePath
   *
   * @param relativePath method's expose path (already includes the base path), not null
   * @param method {@link Method} which is exposed at relativePath
   * @return {@link PathMethod} corresponding to the provided method and relativePath, not null
   */
  private static PathMethod createPathMethod(String relativePath, Method method) {
    final ContentType consumes = getContentTypeFromAnnotation(method, Consumes.class, Consumes::value);
    final ContentType produces = getContentTypeFromAnnotation(method, Produces.class, Produces::value);
    return PathMethod.from(relativePath, method, consumes, produces);
  }

  private static <T extends Annotation> ContentType getContentTypeFromAnnotation(
      Method method,
      Class<T> annClass,
      Function<T, String[]> valuesExtractor) {

    final Optional<T> ann = AnnotationUtils.findMethodAnnotation(method, annClass);
    if (!ann.isPresent()) {
      return ContentType.defaultContentType();
    }
    final String[] annValues = valuesExtractor.apply(ann.get());
    Validate.isTrue(annValues.length == 1,
        "Exactly one content type allowed in annotation " + annClass.getSimpleName()
            + "; offending method: " + method.toGenericString());
    return ContentType.parse(annValues[0]);
  }

  /**
   * Verifies the {@link PathMethod}s have valid relative paths.  Raises an exception listing any
   * PathMethods with invalid {@link PathMethod#getRelativePath()}.  {@link
   * ServiceReflectionUtilities#isValidPath(String)} determines path validity.
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have invalid paths
   */
  private static void verifyRelativePaths(Collection<PathMethod> pathMethods) {
    final List<Method> methodsWithInvalidPaths = pathMethods.stream()
        .filter(e -> !isValidPath(e.getRelativePath()))
        .map(PathMethod::getMethod)
        .collect(Collectors.toList());

    if (!methodsWithInvalidPaths.isEmpty()) {
      final String description = "@Path operations must have values containing valid relative "
          + "paths. The following operations have invalid paths:\n";

      Validation.throwForMethods(description, methodsWithInvalidPaths);
    }
  }

  /**
   * Validates the provided path String is a valid path according to {@link URI#URI(String)}
   *
   * @param path String containing the path to validate, not null
   * @return whether the path is valid
   */
  private static boolean isValidPath(String path) {
    try {
      new URI(path);
      return !StringUtils.containsWhitespace(path);
    } catch (URISyntaxException e) {
      logger.debug("Invalid path: {}", e);
      return false;
    }
  }

  /**
   * Verifies each of the {@link PathMethod}s has a unique relative path and raises an exception
   * listing any repeated paths.
   *
   * @param pathMethods Collection of {@link PathMethod}s that must be unique, not null
   * @throws IllegalArgumentException if any entries in pathMethods have replicated {@link
   * PathMethod#getRelativePath()}
   */
  private static void verifyUniqueRelativePaths(Collection<PathMethod> pathMethods) {
    final String description = "A Service cannot have more than one endpoint operation with "
        + "the same relative path. These relative paths are repeated:";

    Validation.throwForNonUnique(pathMethods, PathMethod::getRelativePath, description);
  }

  /**
   * Verifies the {@link PathMethod}s have valid signatures.  Raises an exception listing any
   * PathMethods with invalid {@link PathMethod#getRelativePath()}.  A PathMethod has a valid
   * signature if it is:
   *
   * 1. Has public visibility
   *
   * 2. Returns a value (i.e. has a non-void return type)
   *
   * 3. Accepts a single input parameter
   *
   * 4. Is also annotated with {@link POST}
   *
   * @param pathMethods Collection of {@link PathMethod}s to verify, not null
   * @throws IllegalArgumentException if any of the pathMethods have invalid signatures
   */
  private static void verifyMethodSignatures(Collection<PathMethod> pathMethods) {
    final Predicate<Method> routeHandlerTest = method -> Optional.of(method)
        .filter(m -> Modifier.isPublic(m.getModifiers()))
        .filter(m -> m.getReturnType() != Void.TYPE)
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> AnnotationUtils.findMethodAnnotation(m, POST.class).isPresent())
        .isPresent();

    final List<Method> methodsWithInvalidSignatures = pathMethods.stream()
        .map(PathMethod::getMethod)
        .filter(m -> routeHandlerTest.negate().test(m))
        .collect(Collectors.toList());

    logger.trace("Found {} invalid route handler methods", methodsWithInvalidSignatures.size());
    methodsWithInvalidSignatures.forEach(m -> logger.trace("Invalid route handler: {}", m));

    // Raise an IllegalArgumentException if any @Path method has an incorrect signature
    if (!methodsWithInvalidSignatures.isEmpty()) {
      final String description = "@Path operations must be public, accept one input parameter, "
          + "return a result, and also have an @POST annotation. These @Path operations have "
          + "incorrect signatures:\n";

      Validation.throwForMethods(description, methodsWithInvalidSignatures);
    }
  }
}
