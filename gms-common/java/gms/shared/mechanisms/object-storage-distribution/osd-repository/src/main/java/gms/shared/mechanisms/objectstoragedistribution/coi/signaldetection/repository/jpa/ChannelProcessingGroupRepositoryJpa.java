package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.RepositoryExceptionUtils;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.ChannelProcessingGroupDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.ChannelProcessingGroupDaoConverter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements {@link ChannelProcessingGroupRepository} using JPA.
 */
public class ChannelProcessingGroupRepositoryJpa implements ChannelProcessingGroupRepository {

  private static final Logger logger =
      LoggerFactory.getLogger(ChannelProcessingGroupRepositoryJpa.class);

  private EntityManagerFactory entityManagerFactory;

  private ChannelProcessingGroupRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Obtain a new {@link ChannelProcessingGroupRepositoryJpa} which uses the provided {@link
   * EntityManagerFactory}
   *
   * @param entityManagerFactory EntityManagerFactory for the filter definition entity classes, not
   * null
   * @return ChannelProcessingGroupRepositoryJpa, not null
   * @throws NullPointerException if entityManagerFactory is null
   */
  public static ChannelProcessingGroupRepositoryJpa create(
      EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
        "Cannot create ChannelProcessingGroupRepositoryJpa with a null EntityManagerFactory");

    return new ChannelProcessingGroupRepositoryJpa(entityManagerFactory);
  }

  @Override
  public void createChannelProcessingGroup(ChannelProcessingGroup channelProcessingGroup)
      throws Exception {
    Objects.requireNonNull(channelProcessingGroup,
        "Cannot store a null ChannelProcessingGroup");

    if (!channelProcessingGroupExists(channelProcessingGroup.getId())) {
      ChannelProcessingGroupDao dao =
          ChannelProcessingGroupDaoConverter.toDao(channelProcessingGroup);

      storeInTransaction(dao);
    }
  }

  @Override
  public boolean channelProcessingGroupExists(UUID channelProcessingGroupId) throws Exception {
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<ChannelProcessingGroupDao> query = entityManager
          .createQuery("SELECT f "
                  + "FROM " + ChannelProcessingGroupDao.class.getSimpleName() + " f "
                  + "WHERE f.id = :id ",
              ChannelProcessingGroupDao.class);

      List<ChannelProcessingGroupDao> queryResults = query
          .setParameter("id", channelProcessingGroupId)
          .getResultList();

      return (queryResults.size() > 0);

    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public Optional<ChannelProcessingGroup> retrieve(UUID channelProcessingGroupId) throws Exception {

    Validate.notNull(channelProcessingGroupId);
    EntityManager entityManager = null;

    try {
      entityManager = this.entityManagerFactory.createEntityManager();
      TypedQuery<ChannelProcessingGroupDao> query = entityManager
          .createQuery("SELECT f "
                  + "FROM " + ChannelProcessingGroupDao.class.getSimpleName() + " f "
                  + "WHERE f.id = :id "
                  + "ORDER BY actualChangeTime ASC ",
              ChannelProcessingGroupDao.class);

      ChannelProcessingGroupDao queryResults = query
          .setParameter("id", channelProcessingGroupId)
          .getSingleResult();

      return (queryResults == null) ?
          Optional.empty() :
          Optional.of(ChannelProcessingGroupDaoConverter.fromDao(queryResults));
    } catch (Exception ex) {
      throw RepositoryExceptionUtils.wrap(ex);
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  @Override
  public List<ChannelProcessingGroup> retrieveAll() {

    final Function<EntityManager, Collection<ChannelProcessingGroupDao>>
        findAllChannelProcessingGroups = em -> em
        .createQuery("FROM ChannelProcessingGroupDao", ChannelProcessingGroupDao.class)
        .getResultList();

    Collection<ChannelProcessingGroupDao> daos =
        applyInEntitySession(findAllChannelProcessingGroups);

    return daos
        .stream()
        .map(ChannelProcessingGroupDaoConverter::fromDao)
        .collect(Collectors.toList());
  }

  /**
   * Utility operation to store the provided entity in a transaction
   *
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
