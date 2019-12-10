package gms.dataacquisition.seedlink.clientlibrary.data.formats;

import gms.dataacquisition.seedlink.clientlibrary.data.Format.Compressed.Int;

public abstract class Steim extends Int {

  public static final int FRAME_SIZE = 64;
  public static final int WORD_SIZE = 4;
  public static final int C_H = 0;
  public static final int C_4S = 1;

  private final int code;

  protected Steim(int c) {
    this.code = c;
  }

  protected void checkConstants(Bits w, int frame)
      throws DecodeException {
    if (w.two(0) != C_H) {
      throw new CorruptFrameHeaderException(
          "c0 in frame " + frame + " must be " +
              C_H + " but was " + w.two(0));
    }

    if (frame == 0) {
      for (int i = 1; i < 3; i++) {
        if (w.two(i * 2) != C_H) {
          throw new CorruptFrameHeaderException("c" + i +
              " in frame " + frame + " must be " +
              C_H + " but was " + w.two(i * 2));
        }
      }
    }
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String name() {
    return getClass().getSimpleName().toUpperCase();
  }

  /**
   * Truncates a quantity of bytes to the nearest multiple of 64 (Steim frames are all 64 bytes
   * apiece).
   *
   * @return an even multiple of 64 bytes, less than or equal to the original number
   */
  public static int truncate(int origBytes) {
    return origBytes - origBytes % 64;
  }

  public static abstract class DecodeException extends Exception {

    private static final long serialVersionUID = 1L;

    public DecodeException(String msg, Throwable cause) {
      super(msg, cause);
    }

    public abstract String description();

    public DecodeException(String msg) {
      super(msg);
    }

    public DecodeException(Throwable cause) {
      super(cause);
    }

    public DecodeException() {
      super();
    }
  }

  public static class CorruptFrameHeaderException extends DecodeException {

    private static final long serialVersionUID = 1L;

    public CorruptFrameHeaderException(String msg) {
      super(msg);
    }

    @Override
    public String description() {
      return "corrupt headers (steim)";
    }
  }
}
