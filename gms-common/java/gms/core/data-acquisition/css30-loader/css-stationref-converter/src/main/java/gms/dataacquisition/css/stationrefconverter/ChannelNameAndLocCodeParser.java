package gms.dataacquisition.css.stationrefconverter;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

public class ChannelNameAndLocCodeParser {

  /**
   * Parses a channel string into a pair of channel and location
   * code, in that order.  If the location code isn't present,
   * it is returned as an empty string.
   * @param chan the channel string.  The first 3 characters
   * are the name of the channel.  The remainder, if any,
   * are the location code.  If the string doesn't have at least 3 characters,
   * the return value is a pair of the input string and an empty string.
   * @return a pair of the channel name and the location code,
   * in that order.  The location code might be the empty string.
   */
  public static Pair<String, String> parse(String chan) {
    Validate.notBlank(chan, "Cannot parse null chan string");
    if (chan.length() > 3) {
      return Pair.of(chan.substring(0, 3), chan.substring(3));
    } else {  // no location code, return chan as channel and location code as empty string.
      return Pair.of(chan, "");
    }
  }

}
