package gms.shared.mechanisms.objectstoragedistribution.coi;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

/**
 * Used to create EntityManagerFactory's for use in tests.
 */
public class CoiTestingEntityManagerFactory {

  private static final String UNIT_NAME = CoiEntityManagerFactory.UNIT_NAME;

  private static final Map<String, String> h2Properties = Map.of(
      "hibernate.connection.driver_class", "org.h2.Driver",
      "hibernate.connection.url", "jdbc:h2:mem:test",
      "hibernate.dialect", "org.hibernate.dialect.H2Dialect",
      "hibernate.hbm2ddl.auto", "create-drop",
      "hibernate.flushMode", "FLUSH_AUTO");

  /**
   * Creates an EntityManagerFactory for testing; connects to an in-memory database.
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory createTesting() {
    try {
      return Persistence.createEntityManagerFactory(UNIT_NAME, h2Properties);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + UNIT_NAME, e);
    }
  }
}
