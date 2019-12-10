package gms.shared.utilities.service.exemplate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExampleDto {

  private final String s;
  private final int i;

  @JsonCreator
  public ExampleDto(
      @JsonProperty("s") String s,
      @JsonProperty("i") int i) {
    this.s = s;
    this.i = i;
  }

  public String getS() {
    return s;
  }

  public int getI() {
    return i;
  }
}
