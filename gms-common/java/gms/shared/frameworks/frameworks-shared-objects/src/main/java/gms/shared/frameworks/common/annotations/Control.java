package gms.shared.frameworks.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a Control class. The annotation accepts a single string parameter containing the
 * Control's name.
 *
 * Used by ControlFactory to automatically instantiate an instance of the Control class.
 * This requires the class to contain a factory method which must be public static, must accept a
 * single parameter of type ControlContext, and must return an instance of Control class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Control {

  /**
   * Obtain the Control's name
   *
   * @return String containing the Control's name, not null
   */
  String value();
}
