package gms.shared.mechanisms.objectstoragedistribution.coi.common.repository;

public class RepositoryException extends RuntimeException {

  public RepositoryException(Throwable cause) {
    super(cause);
  }

  public RepositoryException(String msg) {
    super(msg);
  }

  public RepositoryException() {
    super();
  }
}
