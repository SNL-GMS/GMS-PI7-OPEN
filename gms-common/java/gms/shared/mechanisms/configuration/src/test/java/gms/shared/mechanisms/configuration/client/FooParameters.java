package gms.shared.mechanisms.configuration.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class FooParameters {

  private final int a;
  private final String b;
  private final boolean c;

  private FooParameters(int a, String b, boolean c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  @JsonCreator
  public static FooParameters from(
      @JsonProperty("a") int a,
      @JsonProperty("b") String b,
      @JsonProperty("c") boolean c) {

    return new FooParameters(a, b, c);
  }

  static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private int a;
    private String b;
    private boolean c;

    public Builder a(int a) {
      this.a = a;
      return this;
    }

    public Builder b(String b) {
      this.b = b;
      return this;
    }

    public Builder c(boolean c) {
      this.c = c;
      return this;
    }

    public FooParameters build() {
      return FooParameters.from(a, b, c);
    }
  }

  public int getA() {
    return a;
  }

  public String getB() {
    return b;
  }

  public boolean getC() {
    return c;
  }

  public Builder toBuilder() {
    return new Builder()
        .a(getA())
        .b(getB())
        .c(getC());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FooParameters that = (FooParameters) o;
    return a == that.a &&
        c == that.c &&
        Objects.equals(b, that.b);
  }

  @Override
  public int hashCode() {
    return Objects.hash(a, b, c);
  }
}
