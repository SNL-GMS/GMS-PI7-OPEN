package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.Utility;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PositionalRecord
public class NetworkRecordCss30 extends NetworkRecord {

  private static final Logger logger = LoggerFactory.getLogger(NetworkRecordCss30.class);


  private static final int recordLength = 137;


  public static int getRecordLength() {
    return recordLength;
  }

  @PositionalField(initialPosition = 1, finalPosition = 8)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name.trim();
  }

  @PositionalField(initialPosition = 10, finalPosition = 89)
  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc.trim();
  }

  @PositionalField(initialPosition = 91, finalPosition = 94)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type.trim();
  }

  @PositionalField(initialPosition = 96, finalPosition = 110)
  public String getAuth() {
    return auth;
  }

  public void setAuth(String auth) {
    this.auth = auth.trim();
  }

  @PositionalField(initialPosition = 112, finalPosition = 119)
  public int getCommentId() {
    return commentId;
  }

  public void setCommentId(String commentId) {
    this.commentId = Integer.valueOf(commentId);
  }

  @PositionalField(initialPosition = 121, finalPosition = 137)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = Utility.parseDate(lddate);
  }


}
