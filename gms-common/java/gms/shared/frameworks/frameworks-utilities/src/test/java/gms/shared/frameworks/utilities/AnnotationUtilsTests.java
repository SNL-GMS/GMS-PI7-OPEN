package gms.shared.frameworks.utilities;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class AnnotationUtilsTests {

  private static final String classAnnotationValue = "classAnnotationValueConstant";
  private static final String methodAnnotationValue = "methodAnnotationValueConstant";

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.TYPE, ElementType.METHOD})
  public @interface TestAnnotation {

    String value();
  }

  @TestAnnotation(classAnnotationValue)
  private interface AnnotatedInterface {

    @TestAnnotation(methodAnnotationValue)
    default void bar() {
    }
  }

  // *** Verify class based annotation search

  @Test
  void testFindClassAnnotationNoAnnotation() {
    final Optional<TestAnnotation> annotation = AnnotationUtils
        .findClassAnnotation(Object.class, TestAnnotation.class);

    assertNotNull(annotation);
    assertFalse(annotation.isPresent());

  }

  @TestAnnotation(classAnnotationValue)
  public class PublicFoo {

  }

  @TestAnnotation(classAnnotationValue)
  private class PrivateFoo {

  }

  @Test
  void testFindClassAnnotationClassDirectlyAnnotated() {
    verifyClassAnnotatedWithTestAnnotation(PublicFoo.class);
    verifyClassAnnotatedWithTestAnnotation(PrivateFoo.class);
  }

  @Test
  void testFindClassAnnotationValidatesParameters() {
    assertAll(
        () -> verifyNullPointerExceptionWithMessage(
            () -> AnnotationUtils.findClassAnnotation(null, TestAnnotation.class),
            "Target class can't be null"),

        () -> verifyNullPointerExceptionWithMessage(
            () -> AnnotationUtils.findClassAnnotation(Object.class, null),
            "Annotation class can't be null")
    );
  }

  private static void verifyNullPointerExceptionWithMessage(Executable executable, String message) {
    assertTrue(
        assertThrows(NullPointerException.class, executable).getMessage().startsWith(message));

  }

  // *** Verify correct behavior when class is not directly annotated

  @Test
  void testFindClassAnnotationOnImplementedInterface() {
    class Foo implements AnnotatedInterface {

    }

    verifyClassAnnotatedWithTestAnnotation(Foo.class);
  }

  @Test
  void testFindClassAnnotationOnAbstractBase() {
    @TestAnnotation(classAnnotationValue)
    abstract class Bar {

    }

    class Foo extends Bar {

    }

    verifyClassAnnotatedWithTestAnnotation(Foo.class);
  }

  @Test
  void testFindClassAnnotationOnIndirectlyImplementedInterface() {
    class Bar implements AnnotatedInterface {

    }

    class Foo extends Bar {

    }

    verifyClassAnnotatedWithTestAnnotation(Foo.class);
  }

  @Test
  void testFindClassAnnotationReturnsFirstFoundAnnotation() {
    final String fooAnnotation = "fooAnnotationConstant";
    @TestAnnotation(fooAnnotation)
    class Foo implements AnnotatedInterface {

    }

    verifyClassAnnotatedWithTestAnnotation(Foo.class, fooAnnotation);
  }

  private static void verifyClassAnnotatedWithTestAnnotation(Class<?> target) {
    verifyClassAnnotatedWithTestAnnotation(target, classAnnotationValue);
  }

  private static void verifyClassAnnotatedWithTestAnnotation(Class<?> target,
      String expectedAnnotationValue) {

    final Optional<TestAnnotation> annotation = AnnotationUtils
        .findClassAnnotation(target, TestAnnotation.class);

    assertNotNull(annotation);
    assertAll(
        () -> assertTrue(annotation.isPresent()),
        () -> assertEquals(expectedAnnotationValue,
            annotation.map(TestAnnotation::value).orElse(null))
    );
  }

  // *** Verify method based annotation search
  @Test
  void testFindMethodAnnotationNoAnnotation() throws NoSuchMethodException {
    final Method method = getMethod(Object.class, "getClass");

    final Optional<TestAnnotation> annotation = AnnotationUtils
        .findMethodAnnotation(method, TestAnnotation.class);

    assertEquals(Optional.empty(), annotation);
  }

  @Test
  void testFindMethodAnnotationMethodDirectlyAnnotated() throws NoSuchMethodException {

    class Foo {

      @TestAnnotation(methodAnnotationValue)
      private void privateBar() {
      }

      @TestAnnotation(methodAnnotationValue)
      public void publicBar() {
      }
    }

    verifyMethodAnnotatedWithTestAnnotation(getMethod(Foo.class, "privateBar"));
    verifyMethodAnnotatedWithTestAnnotation(getMethod(Foo.class, "publicBar"));
  }

  @Test
  void testMethodClassAnnotationValidatesParameters() {
    assertAll(
        () -> verifyNullPointerExceptionWithMessage(
            () -> AnnotationUtils.findMethodAnnotation(null, TestAnnotation.class),
            "Target method can't be null"),

        () -> verifyNullPointerExceptionWithMessage(
            () -> AnnotationUtils.findMethodAnnotation(getMethod(Object.class, "getClass"), null),
            "Annotation class can't be null")
    );
  }

  // *** Verify correct behavior when method is not directly annotated

  @Test
  void testFindMethodAnnotationOnImplementedInterface() throws NoSuchMethodException {
    class Foo implements AnnotatedInterface {

    }

    verifyMethodAnnotatedWithTestAnnotation(getPublicOverrideableMethod(Foo.class, "bar"));
  }

  @Test
  void testFindMethodAnnotationOnAbstractBaseClass() throws NoSuchMethodException {
    abstract class Foo {

      @TestAnnotation(methodAnnotationValue)
      public abstract double foo();
    }

    class Bar extends Foo {

      @Override
      public double foo() {
        return 0;
      }
    }

    verifyMethodAnnotatedWithTestAnnotation(getPublicOverrideableMethod(Bar.class, "foo"));
  }

  @Test
  void testFindMethodAnnotationOnIndirectlyImplementedInterface() throws NoSuchMethodException {
    class Bar implements AnnotatedInterface {

    }

    class Foo extends Bar {

    }

    verifyMethodAnnotatedWithTestAnnotation(getPublicOverrideableMethod(Foo.class, "bar"));
  }

  @Test
  void testFindMethodAnnotationReturnsFirstFoundAnnotation() throws NoSuchMethodException {
    final String fooAnnotation = "fooAnnotationConstant";
    class Foo implements AnnotatedInterface {

      @Override
      @TestAnnotation(fooAnnotation)
      public void bar() {

      }
    }

    verifyMethodAnnotatedWithTestAnnotation(getPublicOverrideableMethod(Foo.class, "bar"),
        fooAnnotation);
  }

  private static Method getMethod(Class<?> targetClass, String methodName)
      throws NoSuchMethodException {

    return targetClass.getDeclaredMethod(methodName, (Class[]) null);
  }

  private static Method getPublicOverrideableMethod(Class<?> targetClass, String methodName)
      throws NoSuchMethodException {

    return targetClass.getMethod(methodName, (Class[]) null);
  }

  private static void verifyMethodAnnotatedWithTestAnnotation(Method target) {
    verifyMethodAnnotatedWithTestAnnotation(target, methodAnnotationValue);
  }

  private static void verifyMethodAnnotatedWithTestAnnotation(Method target,
      String expectedAnnotationValue) {

    final Optional<TestAnnotation> annotation = AnnotationUtils
        .findMethodAnnotation(target, TestAnnotation.class);

    assertNotNull(annotation);
    assertAll(
        () -> assertTrue(annotation.isPresent()),
        () -> assertEquals(expectedAnnotationValue,
            annotation.map(TestAnnotation::value).orElse(null))
    );
  }
}
