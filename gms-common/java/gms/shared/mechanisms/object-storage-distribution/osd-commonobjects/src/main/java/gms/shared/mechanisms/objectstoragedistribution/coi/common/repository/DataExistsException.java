package gms.shared.mechanisms.objectstoragedistribution.coi.common.repository;

public class DataExistsException extends RepositoryException {

  public DataExistsException(Throwable cause) {
    super(cause);
  }

  public DataExistsException(String msg) {
    super(msg);
  }

  public DataExistsException() {
    super();
  }
}
