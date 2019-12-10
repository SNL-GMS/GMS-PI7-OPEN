package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

public interface Updateable<T> {

  boolean update(T updatedValue);

}
