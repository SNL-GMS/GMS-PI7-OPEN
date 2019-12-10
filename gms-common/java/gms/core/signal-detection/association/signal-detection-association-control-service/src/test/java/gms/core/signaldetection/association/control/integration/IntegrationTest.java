package gms.core.signaldetection.association.control.integration;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Target({TYPE, METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Test
@Tag("integration")
@interface IntegrationTest {

}
