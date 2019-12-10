package gms.core.waveformqc.waveformsignalqc.plugin.util;

import java.util.Optional;
import java.util.function.Supplier;

public class OptionalUtility {
  private OptionalUtility() {
  }

  public static <T> Boolean equals(Optional<T> o1, T o2) {
    return o1.map(o2::equals).orElse(Boolean.FALSE);
  }

  public static Supplier<IllegalStateException> illegalState(String message) {
    return () -> new IllegalStateException(message);
  }

  public static Supplier<NullPointerException> nullPointer(String message) {
    return () -> new NullPointerException(message);
  }

  public static Supplier<IllegalArgumentException> illegalArgument(String message) {
    return () -> new IllegalArgumentException(message);
  }
}

