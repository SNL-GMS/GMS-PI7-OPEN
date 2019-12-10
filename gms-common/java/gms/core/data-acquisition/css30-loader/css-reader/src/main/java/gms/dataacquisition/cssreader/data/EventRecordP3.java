package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.Utility;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PositionalRecord
public class EventRecordP3 extends EventRecord {
  private static final Logger logger = LoggerFactory.getLogger(EventRecordP3.class);

  private static final int recordLength = 96;


  public static int getRecordLength() {
    return recordLength;
  }

  @PositionalField(initialPosition = 1, finalPosition = 9)
  public int getEventId() {
    return eventId;
  }

  public void setEventId(String id) {
    this.eventId = Integer.valueOf(id);
  }


  @PositionalField(initialPosition = 11, finalPosition = 42)
  public String getName() {
    return eventName;
  }

  public void setName(String name) {
    this.eventName = name;
  }

  @PositionalField(initialPosition = 44, finalPosition = 52)
  public int getOriginId() {
    return originId;
  }

  public void setOriginId(String id) {
    this.originId = Integer.valueOf(id);
  }



  @PositionalField(initialPosition = 54, finalPosition = 68)
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String name) {
    this.author = name;
  }


  @PositionalField(initialPosition = 70, finalPosition = 78)
  public int getCommentId() {
    return commentId;
  }

  public void setCommentId(String id) {
    this.commentId = Integer.valueOf(id);
  }



  @PositionalField(initialPosition = 80, finalPosition = 96)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = Utility.parseDate(lddate);
  }

}
