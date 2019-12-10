package gms.shared.utilities.geotess.util.interfaces;

/**
 * Represents a JUnit Test type. An Integration Test would be a test that runs
 * multiple IPF components and tests their interaction/results etc...
 * 
 * A UnitTest would be a much smaller test that eliminates external resources
 * such as databases, other IPF components etc...
 * 
 * This object is intended to help us label/identify the different types of
 * automated IPF tests.
 * 
 * 
 * See {@link UnitTest}
 */
public interface IntegrationTest {
}
