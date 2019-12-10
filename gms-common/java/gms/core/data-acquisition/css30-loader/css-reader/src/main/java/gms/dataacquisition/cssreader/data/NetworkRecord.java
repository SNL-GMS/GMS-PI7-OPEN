package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetworkRecord {

  private static final Logger logger = LoggerFactory.getLogger(NetworkRecord.class);

  protected static int recordLength;

  protected String name;
  protected String desc;
  protected String type;
  protected String auth;
  protected int commentId;
  protected Instant lddate;

  public static int getRecordLength() {
    return recordLength;
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }

  public String getType() {
    return type;
  }

  public String getAuth() {
    return auth;
  }

  public int getCommentId() {
    return commentId;
  }

  public Instant getLddate() {
    return lddate;
  }

  @Override
  public String toString() {
    return "NetworkRecord{" +
        "name='" + name + '\'' +
        ", desc='" + desc + '\'' +
        ", type='" + type + '\'' +
        ", auth='" + auth + '\'' +
        ", commentId=" + commentId +
        ", lddate='" + lddate + '\'' +
        '}';
  }

  public void validate() {
    Validate.notEmpty(getName(), "Name is empty");
    Validate.notEmpty(getDesc(), "Description is empty");
    Validate.notEmpty(getType(), "Type is empty");
    Validate.notNull(getLddate(), "Load date is null");
  }
}
