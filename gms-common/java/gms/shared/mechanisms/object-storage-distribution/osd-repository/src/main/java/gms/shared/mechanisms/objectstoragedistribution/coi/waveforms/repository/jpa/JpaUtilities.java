package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa;

import javax.persistence.EntityManager;
import org.apache.commons.lang3.Validate;

public class JpaUtilities {

  /**
   * Saves and commits an object to JPA.
   * @param entityManager the entity manager to use to save the object
   * @param obj the object to save
   * @throws Exception if entity is not known, null, other problems with JPA
   */
  public static void saveObjectAndCommit(EntityManager entityManager, Object obj) throws Exception {
    Validate.notNull(entityManager);
    try {
      Validate.notNull(obj);
      entityManager.getTransaction().begin();
      entityManager.persist(obj);
      entityManager.getTransaction().commit();
    }
    finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }

  }

}

