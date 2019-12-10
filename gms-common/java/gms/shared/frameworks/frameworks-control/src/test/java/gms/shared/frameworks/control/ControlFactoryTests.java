package gms.shared.frameworks.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests on the ControlFactory facade layer.
 *
 * Tests do not use legitimate control classes because ControlFactoryWorker handles those details.
 * These tests only verify ControlFactory uses the correct ControlFactoryWorker operations.
 */
@ExtendWith(MockitoExtension.class)
public class ControlFactoryTests {

  @Mock
  private ControlFactoryWorker mockWorker;

  @BeforeEach
  void setup() {
    ControlFactory.setWorker(mockWorker);
  }

  @Test
  void testRunService() {
    ControlFactory.runService(Integer.class);
    verify(mockWorker).runService(Integer.class);
  }

  @Test
  void testCreateControl() {
    when(mockWorker.createControl(String.class)).thenReturn("foo");
    final String s = ControlFactory.createControl(String.class);
    assertEquals("foo", s);
    // try another class just to be sure :)
    when(mockWorker.createControl(Integer.class)).thenReturn(5);
    final int x = ControlFactory.createControl(Integer.class);
    assertEquals(5, x);
  }
}
