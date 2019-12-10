package gms.dataacquisition.cssreader.utilities;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility {

  private static final Logger logger = LoggerFactory.getLogger(Utility.class);

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");

  static {
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  /**
   * Converts a string formatted as seconds.milliseconds into a time Instant object
   */
  public static Instant toInstant(String epochSecondsString) {
    Validate.notEmpty(epochSecondsString, "A time field is empty");
    double epochSeconds = Double.parseDouble(epochSecondsString);
    long epochMilli = (long) (1000L * epochSeconds);
    return Instant.ofEpochMilli(epochMilli);
  }

  /**
   * Parse a date string which has this format: DD-MMM-YY
   *
   * @param dateString The date string.
   * @return An Instant representing the date and time, or null if unable to parse.
   */
  public static Instant parseDate(String dateString) {
    try {
      Date parsedDate = dateFormat.parse(dateString.trim());
      return parsedDate.toInstant();
    } catch (Exception e) {
      logger.error("Failed to parse date string: " + dateString);
      return null;
    }
  }

  /**
   * Julian Dates in CSS are in the form yyyyddd hh:mm:ss.mmm This will create an instance with the
   * correct time and year on Jan 1st then add the days, to easily handle the 'ddd' to 'mmdd'
   * conversion Example Julian Date Input: 2017346 23:20:00.142 Example Instance Output:
   * 2017-12-13T23:20:00.142Z
   *
   * @param jd timestamp from CD11 in the form yyyyddd hh:mm:ss.mmm
   * @return Instant object with UTC format
   * @throws IllegalArgumentException Thrown when input string is not the correct length.
   * @throws DateTimeParseException Thrown when date cannot be parsed from the input string.
   */
  public static Instant jdToInstant(String jd)
      throws DateTimeParseException, IllegalArgumentException {
    if (jd.length() != 7) {
      throw new IllegalArgumentException("Julian Date not length 7 (year=4, day=3)");
    }

    String year = jd.substring(0, 4);
    // minus one because nothing to add when days = 001
    int days = Integer.parseInt(jd.substring(4, 7)) - 1;
    String utc = year + "-01-01T00:00:00.000Z";
    Instant jan1 = Instant.parse(utc);
    return jan1.plus(days, ChronoUnit.DAYS);
  }

}
