package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframeHeader;
import java.nio.ByteBuffer;

public final class FakeDataFrame {

  private static final int DATA_FRAME_HEADER_SIZE = 44;
  private static final int NUM_CHANNELS = 1;
  private static final int FRAME_TIME_LENGTH = 10000;
  private static final String NOMINAL_TIME = "2017346 23:21:10.168";
  private static final int CHANNEL_STRING_COUNT = 10;
  private static final String CHANNEL_STRING = "STA12SHZ01__";

  private static final int DATA_SUBFRAME_SIZE = 100;
  private static final int CHANNEL_LENGTH = 96;
  private static final int AUTHENTICATION_OFFSET = DATA_FRAME_HEADER_SIZE + DATA_SUBFRAME_SIZE - 16;
  private static final byte CHANNEL_DESCRIPTION_AUTHENTICATION = 0;
  private static final byte CHANNEL_DESCRIPTION_TRANSFORMATION = 1;
  private static final byte CHANNEL_DESCRIPTION_SENSOR_TYPE = 0;
  private static final byte CHANNEL_DESCRIPTION_OPTION_FLAG = 0;
  private static final String CHANNEL_DESCRIPTION_SITE_NAME = "STA12";
  private static final String CHANNEL_DESCRIPTION_CHANNEL_NAME = "SHZ";
  private static final String CHANNEL_DESCRIPTION_LOCATION = "01";
  private static final String CHANNEL_DESCRIPTION_DATA_FORMAT = "s4";
  private static final int CHANNEL_DESCRIPTION_CALIB_FACTOR = 0;
  private static final int CHANNEL_DESCRIPTION_CALIB_PER = 0;
  private static final String TIME_STAMP = "2017346 23:21:10.168";
  private static final int SUBFRAME_TIME_LENGTH = 10000;
  private static final int SAMPLES = 8;
  private static final int CHANNEL_STATUS_SIZE = 4;
  private static final int CHANNEL_STATUS = 0;
  private static final int DATA_SIZE = 8;
  private static final byte[] CHANNEL_DATA = new byte[8];
  private static final int SUBFRAME_COUNT = 0;

  private static final int AUTH_KEY = 123;
  private static final int AUTH_SIZE = 8;
  private static final long AUTH_VALUE = 1512076158000l;

  public static Cd11ChannelSubframeHeader generateFakeChannelSubframeHeader() {
    return new Cd11ChannelSubframeHeader(initChannelSubframeHeaderBytes().rewind());
  }

  public static Cd11ChannelSubframe[] generateFakeChannelSubframes() {
    Cd11ChannelSubframe sf = new Cd11ChannelSubframe(initChannelSubframeBytes().rewind());
    return new Cd11ChannelSubframe[]{sf};
  }

  private static ByteBuffer initChannelSubframeHeaderBytes() {
    ByteBuffer sfh = ByteBuffer.allocate(DATA_FRAME_HEADER_SIZE);

    // Subframe header
    sfh.putInt(NUM_CHANNELS);
    sfh.putInt(FRAME_TIME_LENGTH);
    sfh.put(NOMINAL_TIME.getBytes());
    sfh.putInt(CHANNEL_STRING_COUNT);
    sfh.put(CHANNEL_STRING.getBytes());
    return sfh;
  }

  private static ByteBuffer initChannelSubframeBytes() {
    ByteBuffer sf = ByteBuffer.allocate(DATA_SUBFRAME_SIZE);

    for (int i = 0; i < CHANNEL_DATA.length; i++) {
      CHANNEL_DATA[i] = (byte) i;
    }

    // Subframe
    sf.putInt(CHANNEL_LENGTH);
    sf.putInt(AUTHENTICATION_OFFSET);
    sf.put(CHANNEL_DESCRIPTION_AUTHENTICATION);
    sf.put(CHANNEL_DESCRIPTION_TRANSFORMATION);
    sf.put(CHANNEL_DESCRIPTION_SENSOR_TYPE);
    sf.put(CHANNEL_DESCRIPTION_OPTION_FLAG);
    sf.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    sf.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    sf.put(CHANNEL_DESCRIPTION_LOCATION.getBytes());
    sf.put(CHANNEL_DESCRIPTION_DATA_FORMAT.getBytes());
    sf.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    sf.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    sf.put(TIME_STAMP.getBytes());
    sf.putInt(SUBFRAME_TIME_LENGTH);
    sf.putInt(SAMPLES);
    sf.putInt(CHANNEL_STATUS_SIZE);
    sf.putInt(CHANNEL_STATUS);
    sf.putInt(DATA_SIZE);
    sf.put(CHANNEL_DATA);
    sf.putInt(SUBFRAME_COUNT);
    sf.putInt(AUTH_KEY);
    sf.putInt(AUTH_SIZE);
    sf.putLong(AUTH_VALUE);

    return sf;
  }
}
