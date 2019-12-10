package gms.shared.frameworks.utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Utilities to find annotations on classes and methods.
 */
public class AnnotationUtils {

  private AnnotationUtils() {
  }

  /**
   * Finds the first instance of annotationClass on the provided targetClass or any of its
   * superclasses (including interfaces).
   *
   * @param targetClass find an annotation on this {@link Class}, not null
   * @param annotationClass find an instance of this annotation, not null
   * @param <A> annotation type
   * @return Optional annotation of type annotationClass found on targetClass, not null
   * @throws NullPointerException if targetClass or annotationClass are null
   */
  public static <A extends Annotation> Optional<A> findClassAnnotation(Class<?> targetClass,
      Class<A> annotationClass) {

    Objects.requireNonNull(targetClass, "Target class can't be null");
    Objects.requireNonNull(annotationClass, "Annotation class can't be null");

    return StreamSupport
        .stream(ClassUtils.hierarchy(targetClass, Interfaces.INCLUDE).spliterator(), false)
        .map(c -> c.getAnnotation(annotationClass))
        .filter(Objects::nonNull)
        .findFirst();
  }

  /**
   * Finds the first instance of annotationClass on the provided targetMethod or any of its
   * superclass (including interface) implementations.
   *
   * @param targetMethod find an annotation on this {@link Method}, not null
   * @param annotationClass find an instance of this annotation, not null
   * @param <A> annotation type
   * @return Optional annotation of type annotationClass found on the targetMethod, not null
   * @throws NullPointerException if targetMethod or annotationClass are null
   */
  public static <A extends Annotation> Optional<A> findMethodAnnotation(Method targetMethod,
      Class<A> annotationClass) {

    Objects.requireNonNull(targetMethod, "Target method can't be null");
    Objects.requireNonNull(annotationClass, "Annotation class can't be null");

    return Optional.ofNullable(
        MethodUtils.getAnnotation(targetMethod, annotationClass, true, true));
  }
}
