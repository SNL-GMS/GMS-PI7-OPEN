package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.ProvenanceRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInformationDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.utility.CreationInformationDaoConverter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository class responsible for storage and retrieval operations on {@link CreationInformation}s
 * and their related classes via JPA and DAO objects.
 */
public class ProvenanceRepositoryJpa implements ProvenanceRepository {

  private static final Logger logger = LoggerFactory.getLogger(ProvenanceRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  /**
   * Obtain an instance of {@link ProvenanceRepositoryJpa} using a default {@link
   * EntityManagerFactory}
   */
  public ProvenanceRepositoryJpa() {
    this(CoiEntityManagerFactory.create());
  }

  /**
   * Default constructor, requiring a DI'd EntityManagerFactory for generating EntityManagers during
   * a repository call.
   *
   * @param entityManagerFactory Factory to create new {@link EntityManager}s
   */
  public ProvenanceRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Convenience method for handling exceptions and resource closing whenever any repository call is
   * made.
   *
   * @param consumer Any repository call returning void (e.g. store calls)
   */
  private void acceptInEntitySession(Consumer<EntityManager> consumer) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      consumer.accept(entityManager);
    } catch (PersistenceException e) {
      throw new RuntimeException(e);
    } finally {
      entityManager.close();
    }

  }

  /**
   * Convenience method for handling exceptions and resource closing whenever any repository call is
   * made.
   *
   * @param applier Any repository call returning not void (e.g. find calls)
   */
  private <T> T applyInEntitySession(Function<EntityManager, T> applier) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      return applier.apply(entityManager);
    } catch (PersistenceException e) {
      throw new RuntimeException(e);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public void store(CreationInformation creationInformation) {
    Objects.requireNonNull(creationInformation, "Cannot store a null CreationInformation");
    acceptInEntitySession(em -> storeInternal(em, creationInformation));
  }

  @Override
  public List<CreationInformation> findCreationInformationById(UUID id) {
    Objects.requireNonNull(id, "Cannot query with a null CreationInformation Id");
    return applyInEntitySession(em -> getCreationInformation(em, id));
  }

  /**
   * Internal method used to handle storing CreationInformation objects.
   *
   * @param entityManager EntityManager used to handle queries and storage.
   * @param creationInformation to store.
   */
  private static void storeInternal(EntityManager entityManager,
      CreationInformation creationInformation) {
    List<CreationInformationDao> creationInformationDaos = getCreationInformationDao(entityManager,
        creationInformation.getId());

    if (creationInformationDaos.isEmpty()) {
      try {
        entityManager.getTransaction().begin();
        entityManager.persist(CreationInformationDaoConverter.toDao(creationInformation));
        entityManager.getTransaction().commit();
      } catch (IllegalArgumentException | PersistenceException e) {
        logger.error("Error storing CreationInformation", e);
        if (entityManager.getTransaction().isActive()) {
          entityManager.getTransaction().rollback();
        }
        throw e;
      }
    }
  }

  /**
   * Queries the database for a {@link CreationInformationDao} with an id matching the input {@link
   * CreationInformation}. Returns the list of discovered CreationInformationDao objects (one or an
   * empty list)
   *
   * @param entityManager Provides connection to the database.
   * @param creationInformationId CreationInformation Id we are searching for in the database.
   * @return a list with one entry (previously persisted) or empty.
   */
  private static List<CreationInformationDao> getCreationInformationDao(EntityManager entityManager,
      UUID creationInformationId) {
    TypedQuery<CreationInformationDao> query = entityManager
        .createQuery("SELECT m FROM CreationInformationDao m WHERE m.id = :id",
            CreationInformationDao.class);

    query.setParameter("id", creationInformationId);

    return query.getResultList();
  }

  /**
   * Queries the database for a {@link CreationInformationDao} with an id matching the input {@link
   * CreationInformation}. If one is not found, creates a new CreationInformationDao from the input
   * CreationInformation.
   *
   * @param entityManager Provides connection to the database.
   * @param id CreationInformation Id we are searching for in the database.
   * @return Either a previously persisted or newly created CreationInformationDao.
   */
  private static List<CreationInformation> getCreationInformation(EntityManager entityManager,
      UUID id) {
    List<CreationInformationDao> creationInformationDaos = getCreationInformationDao(entityManager,
        id);
    return creationInformationDaos.isEmpty() ? Collections.emptyList() : Collections
        .singletonList(CreationInformationDaoConverter.fromDao(creationInformationDaos.get(0)));
  }
}
