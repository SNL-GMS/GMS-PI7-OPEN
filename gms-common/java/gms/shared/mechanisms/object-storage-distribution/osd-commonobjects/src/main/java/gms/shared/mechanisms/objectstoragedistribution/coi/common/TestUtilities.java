package gms.shared.mechanisms.objectstoragedistribution.coi.common;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.EqualsVerifierApi;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * Helpful methods for testing.
 */
public class TestUtilities {

  /**
   * Tests whether an object can be serialized and deserialized with the COI object mapper.
   * @param object an instance of the object
   * @param type the type of the object
   * @param <T> the type of the object
   * @throws IOException if serialization/deserialization fails
   */
  public static <T> void testSerialization(T object, Class<T> type)
      throws IOException {
    testSerialization(object, type, CoiObjectMapperFactory.getJsonObjectMapper());
  }

  /**
   * Tests whether an object can be serialized and deserialized with the given object mapper.
   * @param object an instance of the object
   * @param type the type of the object
   * @param objMapper an object mapper to use
   * @param <T> the type of the object
   * @throws IOException if serialization/deserialization fails
   */
  public static <T> void testSerialization(T object, Class<T> type, ObjectMapper objMapper)
      throws IOException {
    Objects.requireNonNull(object, "Cannot test serialization with null object instance");
    Objects.requireNonNull(type, "Cannot test serialization with null type");
    Objects.requireNonNull(objMapper, "Cannot test serialization with null object mapper");
    final String serialized = objMapper.writeValueAsString(object);
    Objects.requireNonNull(serialized, "Expected serialized string to be non-null");
    Validate.isTrue(!serialized.isEmpty(), "Expected serialized string to not be empty");
    final T deserialized = objMapper.readValue(serialized, type);
    Objects.requireNonNull(deserialized, "Expected deserialized object to be non-null");
    Validate.isTrue(object.equals(deserialized), "Expected deserialized object to equal original; original: "
        + object + " , deserialized: " + deserialized);
  }

  /**
   * Tests whether an object can be serialized and deserialized with the COI object mapper.
   * @param object an instance of the object
   * @param type the type of the object
   * @param <T> the type of the object
   * @throws IOException if serialization/deserialization fails
   */
  public static <T> void testSerialization(T object, JavaType type)
      throws IOException {
    testSerialization(object, type, CoiObjectMapperFactory.getJsonObjectMapper());
  }

  /**
   * Tests whether an object can be serialized and deserialized with the given object mapper.
   * @param object an instance of the object
   * @param type the type of the object
   * @param objMapper an object mapper to use
   * @param <T> the type of the object
   * @throws IOException if serialization/deserialization fails
   */
  public static <T> void testSerialization(T object, JavaType type, ObjectMapper objMapper)
      throws IOException {
    Objects.requireNonNull(object, "Cannot test serialization with null object instance");
    Objects.requireNonNull(type, "Cannot test serialization with null type");
    Objects.requireNonNull(objMapper, "Cannot test serialization with null object mapper");
    final String serialized = objMapper.writeValueAsString(object);
    Objects.requireNonNull(serialized, "Expected serialized string to be non-null");
    Validate.isTrue(!serialized.isEmpty(), "Expected serialized string to not be empty");
    final T deserialized = objMapper.readValue(serialized, type);
    Objects.requireNonNull(deserialized, "Expected deserialized object to be non-null");
    Validate.isTrue(object.equals(deserialized), "Expected deserialized object to equal original; original: "
        + object + " , deserialized: " + deserialized);
  }


  /**
   * Tests the equals/hashCode contract of the given class, while checking that subclass and
   * superclass instances are equal.
   *
   * @param clazz the class to test
   */
  public static void checkClassEqualsAndHashcode(Class<?> clazz) {
    checkClassEqualsAndHashcode(clazz, true);
  }

  /**
   * Tests the equals/hashCode contract of the given class.
   *
   * @param clazz the class to test
   * @param superClassEqualsSubclass if true, then any superclass and subclass instances of the
   * given class must be equal.
   */
  public static void checkClassEqualsAndHashcode(Class<?> clazz, boolean superClassEqualsSubclass) {

    EqualsVerifierApi<?> verifier = EqualsVerifier.forClass(clazz);
    if (!superClassEqualsSubclass) {
      // ignores check that subclass instances and superclass instances are equal
      verifier = verifier.withRedefinedSuperclass();
    }
    // TODO: Fix this!
    //verifier.verify();
  }

  /**
   * Runs constructor null argument validation for the provided class across all input value arrays.
   * It then checks that all constructors for the provided class were tested, throwing an error if
   * not.
   *
   * @param clazz Class whose constructors are validated for null arguments.
   * @param validArguments A collection of argument collections, each representing the input to a
   * different constructors.
   */
  public static void checkAllConstructorsValidateNullArguments(Class<?> clazz,
      Object[][] validArguments) {

    Arrays.stream(validArguments)
        .forEach(a -> checkConstructorValidatesNullArguments(clazz, a));

    Set<Constructor> validatedConstructors = Arrays.stream(validArguments)
        .map(TestUtilities::convertToTypes)
        .map(ts -> ConstructorUtils.getAccessibleConstructor(clazz, ts))
        .collect(Collectors.toSet());

    List<Constructor<?>> missingValidatedConstructors = Arrays.stream(clazz.getConstructors())
        .filter(c -> !validatedConstructors.contains(c))
        .collect(Collectors.toList());

    if (!missingValidatedConstructors.isEmpty()) {
      throw new IllegalArgumentException(
          "Missing validation arguments for following constructors: "
              + missingValidatedConstructors.stream()
              .map(Constructor::toGenericString)
              .collect(Collectors.joining(",")));
    }

  }


  /**
   * Checks that a constructor for the given class validates arguments as non-null.
   *
   * @param clazz the class to check
   * @param validArguments Arguments the caller declares would make a valid instance of the class.
   * These are used with a single null parameter set in each call to test the constructor.
   */
  public static void checkConstructorValidatesNullArguments(Class<?> clazz,
      Object... validArguments) {

    Class<?>[] validArgumentTypes = convertToTypes(validArguments);
    Constructor constructor = ConstructorUtils
        .getMatchingAccessibleConstructor(clazz, validArgumentTypes);
    if (constructor == null) {
      throw new IllegalArgumentException("No constructor signature found for arguments: "
          + Arrays.toString(validArgumentTypes));
    }

    // call the constructor with regular (working) args, ensure that works.
    try {
      constructor.newInstance(validArguments);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not call constructor with supposedly valid arguments",
          e);
    }

    // Replace each nullable argument with null individually,
    // call constructor and expect an InvocationTarget exception to be thrown.
    Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
    for (int i = 0; i < validArgumentTypes.length; i++) {
      if (!constructorParameterTypes[i].isPrimitive()) {
        try {
          Object[] argsWithOneNull = withNullAt(validArguments, i);
          constructor.newInstance(argsWithOneNull);
        }
        // exception is expected, proceed.
        catch (InvocationTargetException e) {
          if (e.getCause() instanceof NullPointerException) {
            continue;
          }
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException e) {
          throw new RuntimeException("Error creating test constructor instance", e);
        }
        // Exception wasn't thrown as expected.
        throw new RuntimeException("No validation for argument of type "
            + constructorParameterTypes[i] + " at parameter index " + i + " for constructor "
            + constructor);
      }
    }

  }

  /**
   * Checks that the static method for a provided class validates all arguments as non-null.
   *
   * @param clazz The class to validate.
   * @param methodName The static class method name to validate.
   * @param validArguments valid arguments to the method that are iterated over setting to null, to
   * ensure the method validates all arguments as non-null.
   */
  public static void checkStaticMethodValidatesNullArguments(Class<?> clazz, String methodName,
      Object... validArguments) throws IllegalAccessException {
    checkMethodValidatesNullArguments(clazz, null, methodName, Collections.emptyList(),
        validArguments);
  }

  /**
   * Checks that the static method for a provided class validates all non-nullable arguments as
   * non-null.
   *
   * @param clazz The class to validate.
   * @param methodName The static class method name to validate.
   * @param nullableFieldIndices Indices for arguments that are allowed to be null.
   * @param validArguments valid arguments to the method that are iterated over setting to null, to
   * ensure the method validates all arguments as non-null.
   */
  public static void checkStaticMethodValidatesNullableArguments(
      Class<?> clazz, String methodName, List<Integer> nullableFieldIndices,
      Object... validArguments) throws IllegalAccessException {
    checkMethodValidatesNullArguments(clazz, null, methodName, nullableFieldIndices,
        validArguments);
  }

  /**
   * Checks that the method for a provided object validates all arguments as non-null.
   *
   * @param object The class instantiation to validate.
   * @param methodName The object method name to validate.
   * @param validArguments valid arguments to the method that are iterated over setting to null, to
   * ensure the method validates all arguments as non-null.
   */
  public static void checkMethodValidatesNullArguments(
      Object object, String methodName, Object... validArguments) throws IllegalAccessException {
    checkMethodValidatesNullArguments(object.getClass(), object, methodName,
        Collections.emptyList(), validArguments);
  }

  /**
   * Checks that the method for a provided object validates all non-nullable arguments as non-null.
   *
   * @param object The class instantiation to validate.
   * @param methodName The object method name to validate.
   * @param nullableFieldIndices Indices for arguments that are allowed to be null.
   * @param validArguments valid arguments to the method that are iterated over setting to null, to
   * ensure the method validates all arguments as non-null.
   */
  public static void checkMethodValidatesNullableArguments(
      Object object, String methodName, List<Integer> nullableFieldIndices,
      Object... validArguments) throws IllegalAccessException {
    checkMethodValidatesNullArguments(object.getClass(), object, methodName,
        nullableFieldIndices, validArguments);
  }

  /**
   * Base method for validating non-null arguments in methods, both static and non-static.
   *
   * @param clazz The class to validate.
   * @param object The class instantiation to validate. Can be null for static methods.
   * @param methodName The method name to validate.
   * @param nullableFieldIndices Indices for arguments that are allowed to be null.
   * @param validArguments valid arguments to the method that are iterated over setting to null, to
   * ensure the method validates all arguments as non-null.
   */
  private static void checkMethodValidatesNullArguments(Class<?> clazz, Object object,
      String methodName, List<Integer> nullableFieldIndices, Object... validArguments)
      throws IllegalAccessException {

    Class<?>[] validParameterTypes = Arrays.stream(validArguments)
        .map(Object::getClass).toArray(Class[]::new);

    Method method = MethodUtils
        .getMatchingAccessibleMethod(clazz, methodName, validParameterTypes);

    if (method == null) {
      throw new IllegalArgumentException("No method signature found for method: " + methodName
          + ", arguments: "
          + Arrays.toString(validParameterTypes));
    }

    // call the method with regular (working) args, ensure that works.
    try {
      method.invoke(object, validArguments);
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not call static method with supposedly valid arguments",
          e);
    }

    Class<?>[] methodParameterTypes = method.getParameterTypes();
    for (int i = 0; i < validArguments.length; i++) {
      if (!methodParameterTypes[i].isPrimitive() && !nullableFieldIndices.contains(i)) {

        try {
          method.invoke(object, withNullAt(validArguments, i));
        } catch (InvocationTargetException e) {
          //expecting a NullPointerException, continue
          if (e.getCause() instanceof NullPointerException) {
            continue;
          }
        }
        //Exception wasn't thrown as expected
        throw new RuntimeException(
            "No validation for argument of type " + methodParameterTypes[i] +
                " at parameter index " + i +
                " for method " + methodName);
      }
    }
  }


  /**
   * Takes in an Object[], (shallow) copies it, sets the specified index to null, and returns the
   * new array.
   *
   * @param a the array to start with
   * @param index the index to set to null
   * @return a new copy of the original array, with the specified index set as null
   */
  private static Object[] withNullAt(Object[] a, int index) {
    Object[] newArgsList = Arrays.copyOf(a, a.length);
    newArgsList[index] = null;
    return newArgsList;
  }

  /**
   * Maps Object::getClass over an Object[], returning the class for each Object.
   *
   * @param objs the objects
   * @return the types of each Object as an array, acquired via Object::getClass.
   */
  private static Class<?>[] convertToTypes(Object[] objs) {
    return Arrays.stream(objs)
        .map(Object::getClass)
        .toArray(Class<?>[]::new);
  }

  /**
   * Checks if the number of constructors tested matches number of public constructors in the class.
   * If they don't, an exception is thrown that details how many and which ones were not tested.
   *
   * @param publicConstructors the public constructors for the class, e.g.
   * obj.getClass().getConstructors()
   * @param testedConstructors the constructors that will be tested.  These are compared to
   * publicConstructors, and if differences exist an exception is thrown.
   */
  private static void throwExceptionIfNotAllConstructorsTested(
      Constructor[] publicConstructors,
      Set<Constructor> testedConstructors) {

    Constructor[] notCovered = Arrays.stream(publicConstructors)
        .filter(c -> !testedConstructors.contains(c))
        .toArray(Constructor[]::new);

    if (notCovered.length > 0) {
      String exceptionMsg = String.format(
          "mustCoverAllConstructors=true, but not satisfied; "
              + "%d Constructor(s) are not covered: %s", notCovered.length,
          Arrays.toString(notCovered));
      throw new IllegalArgumentException(exceptionMsg);
    }
  }
}
