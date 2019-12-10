package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import java.util.Collections;
import java.util.Objects;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestUtilitiesTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  public static String testFactory1(String testString, Integer testInt) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    Objects.requireNonNull(testInt, "Testing for non null testInt");

    return testString + testInt;
  }

  public static String testFactory2(String testString, Integer testInt) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    return testString + testInt;
  }

  public static String testFactory3(String testString, Number testNum) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    Objects.requireNonNull(testNum, "Testing for non null testNum");
    return testString + testNum;
  }

  public static String testFactory4(String testString, int testInt) {
    Objects.requireNonNull(testString, "Testing for non null testString");
    return testString + testInt;
  }

  public static String testFactory5(String testString, int testInt) {
    throw new NullPointerException();
  }

  @Test
  public void testCheckConstructorValidatesNullArguments() {
    TestUtilities.checkConstructorValidatesNullArguments(Test1.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsExpectException() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("No validation for argument of type " + Integer.class
        + " at parameter index 0 for constructor"
        + " public gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilitiesTests$Test2(java.lang.Integer)");
    TestUtilities.checkConstructorValidatesNullArguments(Test2.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsSubclassArgument() {
    TestUtilities.checkConstructorValidatesNullArguments(Test3.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsPrimitiveArgument() {
    TestUtilities.checkConstructorValidatesNullArguments(Test4.class, 1);
  }

  @Test
  public void testCheckConstructorValidatesNullArgumentsNoConstructorFound() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("No constructor signature found for arguments: [" + String.class + "]");
    TestUtilities.checkConstructorValidatesNullArguments(Test1.class, "Test");
  }

  @Test
  public void testCheckAllConstructorsValidateNullArguments() {
    TestUtilities
        .checkAllConstructorsValidateNullArguments(Test5.class, new Object[][]{{"Test"}, {1}});
  }

  @Test
  public void testCheckAllConstructorsValidateNullArgumentsWhenValidArgsThrowsException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Could not call constructor with supposedly valid arguments");
    TestUtilities.checkConstructorValidatesNullArguments(Test6.class, 1);
  }

  @Test
  public void testCheckAllConstructorsValidateNullArgumentsMissingParameters() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Missing validation arguments for following constructors: "
        + "public gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilitiesTests"
        + "$Test5(java.lang.Integer)");
    TestUtilities.checkAllConstructorsValidateNullArguments(Test5.class, new Object[][]{{"Test"}});
  }


  @Test
  public void testCheckStaticMethodValidatesNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory1", "TestString", 1);
  }

  @Test
  public void testCheckStaticMethodValidateNullArgumentsWhenValidArgsThrowsException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Could not call static method with supposedly valid arguments");
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory5", "TestString", 1);
  }

  @Test
  public void testCheckStaticMethodValidatesNullArgumentsExpectException() throws Exception {
    exception.expect(RuntimeException.class);
    exception.expectMessage(
        "No validation for argument of type " + Integer.class
            + " at parameter index 1 for method testFactory2");
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory2", "TestString", 1);
  }

  @Test
  public void testCheckStaticMethodValidatesNullArgumentsNullAllowed() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullableArguments(TestUtilitiesTests.class,
        "testFactory2", Collections.singletonList(1), "TestString", 1);
  }

  @Test
  public void testCheckStaticMethodValidatesNullArgumentsExpectsSuperclassPassesSubclass()
      throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory3", "TestString", 1);
  }

  @Test
  public void testCheckStaticMethodValidatesNullArgumentsPrimitiveArgument() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(TestUtilitiesTests.class,
        "testFactory4", "TestString", 1);
  }

  public static class Test1 {

    public Test1(Integer testInt) {
      Objects.requireNonNull(testInt, "Testing for non null testInt");
    }
  }

  public static class Test2 {

    public Test2(Integer testInt) {

    }
  }

  public static class Test3 {

    public Test3(Number testNum) {
      Objects.requireNonNull(testNum, "Testing for non null testNum");
    }
  }

  public static class Test4 {

    public Test4(int testInt) {
    }
  }

  public static class Test5 {

    public Test5(String testString) {
      Objects.requireNonNull(testString, "Testing for non null testString");
    }

    public Test5(Integer testInt) {
      Objects.requireNonNull(testInt, "Testing for non null testInt");
    }
  }

  public static class Test6 {

    public Test6(Integer testInt) {
      throw new NullPointerException();
    }
  }
}
