package gms.shared.mechanisms.objectstoragedistribution.coi.common.repository;

public class StorageUnavailableException extends RepositoryException {

  public StorageUnavailableException(Throwable cause) {
    super(cause);
  }

  public StorageUnavailableException(String msg) {
    super(msg);
  }

  public StorageUnavailableException() {
    super();
  }
}
