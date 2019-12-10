package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import gms.dataacquisition.stationreceiver.cd11.common.CRC64;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * Represents the trailer of a CD-1.1 frame. The frame trailer provides three fields for frame
 * authentication. These fields are authentication key identifier, authentication size, and
 * authentication value. The three frame authentication fields are provided so that the receiver of
 * the frame has assurance the sender is correctly identified and the contents have not been
 * tampered with. The scope of the authentication field is the frame header and payload. The last
 * field of each frame trailer is the comm verification value used to verify the correct
 * transmission of the frame. The comm verification is a 64-bit CRC value calculated over the entire
 * frame (header, payload, and trailer).
 */
public class Cd11FrameTrailer {

  // See constructor javadoc for description of the fields.
  public final int authenticationKeyIdentifier;
  public final int authenticationSize;
  public final byte[] authenticationValue;
  public final long commVerification;

  /**
   * The minimum byte array length of a frame trailer. This value does not include the
   * authentication value which is dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = (Integer.BYTES * 2) + Long.BYTES;

  /**
   * Creates a trailer given an input stream.
   *
   * @param frameSegment1 first 8 bytes of the CD 1.1 trailer frame.
   * @param frameSegment2 remaining bytes of the CD 1.1 trailer frame.
   * @throws Exception if the input is null, cannot be read, doesn't have desired data, etc.
   */
  public Cd11FrameTrailer(ByteBuffer frameSegment1, ByteBuffer frameSegment2) throws Exception {
    frameSegment1.rewind();
    frameSegment2.rewind();

    this.authenticationKeyIdentifier = frameSegment1.getInt();
    this.authenticationSize = frameSegment1.getInt();

    // Calculate how much padding to add to the Authentication Value.
    int authValuePadding = FrameUtilities.calculateUnpaddedLength(this.authenticationSize, 4);
    int authValueSizeWithPadding = this.authenticationSize + authValuePadding;

    this.authenticationValue = new byte[authValueSizeWithPadding];
    frameSegment2.get(this.authenticationValue);
    if (this.authenticationValue.length != this.authenticationSize) {
      throw new IllegalArgumentException(
          "Bad input; not enough bytes to read authenticationValue byte[], expected "
              + this.authenticationSize + " actual = " + this.authenticationValue.length);
    }
    this.commVerification = frameSegment2.getLong();
  }

  /**
   * Creates a new frame trailer with all arguments.
   *
   * @param authenticationKeyIdentifier identifier of the public key certificate required to verify
   * the authentication value field; if non-zero, then authentication is used to verify
   * communications.
   * @param frameHeaderAndBody Byte array representing the frame header and body, used to generate
   * the CRC64 value.
   */
  public Cd11FrameTrailer(int authenticationKeyIdentifier, byte[] frameHeaderAndBody) {
    this.authenticationKeyIdentifier = authenticationKeyIdentifier;
    this.authenticationSize = 0;            // TODO: Generate this value.
    this.authenticationValue = new byte[0]; // TODO: Generate this value.
    byte[] entireFrame = Bytes.concat(
        frameHeaderAndBody,
        Ints.toByteArray(this.authenticationKeyIdentifier),
        Ints.toByteArray(this.authenticationSize),
        this.authenticationValue,
        Longs
            .toByteArray(0L)); // Have to add CRC as a long filled with zeroes to compute correctly.
    this.commVerification = CRC64.compute(entireFrame);
  }

  /**
   * Turns this frame trailer into a byte[]
   *
   * @return a byte[] representation of this trailer
   * @throws IOException if cannot write to byte array output stream, etc.
   */
  public byte[] toBytes() throws IOException {
    // Calculate how much padding to add to the Authentication Value.
    int authValuePadding = FrameUtilities
        .calculateUnpaddedLength(this.authenticationSize, Integer.BYTES);

    ByteBuffer output = ByteBuffer
        .allocate(MINIMUM_FRAME_LENGTH + this.authenticationSize + authValuePadding);
    output.putInt(this.authenticationKeyIdentifier);
    output.putInt(this.authenticationSize);
    output.put(this.authenticationValue);
    for (int i = 0; i < authValuePadding; i++) {
      output.putChar('\0');
    }
    output.putLong(this.commVerification);
    return output.array();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Cd11FrameTrailer that = (Cd11FrameTrailer) o;

    if (authenticationKeyIdentifier != that.authenticationKeyIdentifier) {
      return false;
    }
    if (authenticationSize != that.authenticationSize) {
      return false;
    }
    if (commVerification != that.commVerification) {
      return false;
    }
    return Arrays.equals(authenticationValue, that.authenticationValue);
  }

  @Override
  public int hashCode() {
    int result = authenticationKeyIdentifier;
    result = 31 * result + authenticationSize;
    result = 31 * result + Arrays.hashCode(authenticationValue);
    result = 31 * result + (int) (commVerification ^ (commVerification >>> 32));
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11FrameTrailer { ");
    out.append("authenticationKeyIdentifier: ").append(authenticationKeyIdentifier).append(", ");
    out.append("authenticationSize: ").append(authenticationSize).append(", ");
    out.append("authenticationValue: \"").append(Arrays.toString(authenticationValue))
        .append("\", ");
    out.append("commVerification: ").append(commVerification).append(" ");
    out.append("}");
    return out.toString();
  }
}
