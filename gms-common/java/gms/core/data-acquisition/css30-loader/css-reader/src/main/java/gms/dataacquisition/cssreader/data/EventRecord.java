package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents the 'Event' table in CSS.
 * Uses the FFPojo library annotations to make this class parsable from a
 * flat file representation.
 */
public class EventRecord {
  private static final Logger logger = LoggerFactory.getLogger(EventRecord.class);

  protected static int recordLength;

  protected int eventId;
  protected String eventName;
  protected int originId;
  protected String author;
  protected int commentId;
  protected Instant lddate;

  public static int getRecordLength() {
    return recordLength;
  }

  public int getEventId() {
    return eventId;
  }

  public String getEventName() {
    return eventName;
  }

  public int getOriginId() {
    return originId;
  }

  public String getAuthor() {
    return author;
  }

  public int getCommentId() {
    return commentId;
  }

  public Instant getLddate() {
    return lddate;
  }

  @Override
  public String toString() {
    return "EventRecord{" +
        "eventId=" + eventId +
        ", eventName='" + eventName + '\'' +
        ", originId=" + originId +
        ", author='" + author + '\'' +
        ", commentId=" + commentId +
        ", lddate=" + lddate +
        '}';
  }

  public void validate() {
    Validate.notNaN(getEventId(), "Event ID is NaN");
    Validate.notEmpty(getEventName(), "Name is empty");
    Validate.notNaN(getOriginId(), "Origin ID is NaN");
    Validate.notEmpty(getAuthor(), "Author is empty");
    Validate.notNull(getLddate(), "Load date is null");

  }
}
