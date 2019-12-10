package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.FilterDefinitionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.FilterDefinitionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.FilterDefinitionDaoConverter;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link FilterDefinitionRepository} using JPA.
 */
public class FilterDefinitionRepositoryJpa implements FilterDefinitionRepository {

  private static final Logger logger = LoggerFactory.getLogger(FilterDefinitionRepositoryJpa.class);

  private EntityManagerFactory entityManagerFactory;

  private FilterDefinitionRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Obtain a new {@link FilterDefinitionRepositoryJpa} which uses the provided {@link
   * EntityManagerFactory}
   *
   * @param entityManagerFactory EntityManagerFactory for the filter definition entity classes, not
   * null
   * @return FilterDefinitionRepositoryJpa, not null
   * @throws NullPointerException if entityManagerFactory is null
   */
  public static FilterDefinitionRepositoryJpa create(EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
        "Cannot create FilterDefinitionRepositoryJpa with a null EntityManagerFactory");

    return new FilterDefinitionRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void store(FilterDefinition filterDefinition) {
    Objects.requireNonNull(filterDefinition, "Cannot store a null FilterDefinition");

    if(!retrieveAll().contains(filterDefinition)) {
      storeInTransaction(FilterDefinitionDaoConverter.toDao(filterDefinition));
    }
  }

  /**
   * Utility operation to store the provided entity in a transaction
   * @param entity entity to store, not null
   * @param <T> entity type
   */
  private <T> void storeInTransaction(T entity) {
    Consumer<EntityManager> storeEntityClosure = em -> {
      try {
        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
      } catch (IllegalArgumentException | PersistenceException e) {
        logger.error("Error storing entity {}: {}", entity, e);
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      }
    };

    acceptInEntitySession(storeEntityClosure);
  }

  @Override
  public Collection<FilterDefinition> retrieveAll() {

    final Function<EntityManager, Collection<FilterDefinitionDao>> findAllFilterDefinitions =
        em -> em.createQuery("SELECT f FROM FilterDefinitionDao f", FilterDefinitionDao.class)
            .getResultList();

    return applyInEntitySession(findAllFilterDefinitions)
        .stream()
        .map(FilterDefinitionDaoConverter::fromDao)
        .collect(Collectors.toList());
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
}
