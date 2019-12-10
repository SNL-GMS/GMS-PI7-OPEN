package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;

@AutoValue
public abstract class ProcessingResponse<T> {

  public abstract ImmutableList<T> getUpdated();

  public abstract ImmutableList<T> getUnchanged();

  public abstract ImmutableList<T> getFailed();

  public static <T> Builder<T> builder() {
    return new AutoValue_ProcessingResponse.Builder<>();
  }

  @JsonCreator
  public static <T> ProcessingResponse<T> from(
      @JsonProperty("updated") Collection<T> updated,
      @JsonProperty("unchanged") Collection<T> unchanged,
      @JsonProperty("failed") Collection<T> failed) {

    return ProcessingResponse.<T>builder()
        .setUpdated(updated)
        .setUnchanged(unchanged)
        .setFailed(failed)
        .build();
  }

  public abstract Builder<T> toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder<T> {

    protected abstract ImmutableList.Builder<T> updatedBuilder();

    protected abstract Builder<T> setUpdated(ImmutableList<T> updated);

    public Builder<T> setUpdated(Collection<T> updated) {
      return setUpdated(ImmutableList.copyOf(updated));
    }

    public Builder<T> addUpdated(T updated) {
      updatedBuilder().add(updated);
      return this;
    }

    protected abstract ImmutableList.Builder<T> unchangedBuilder();

    protected abstract Builder<T> setUnchanged(ImmutableList<T> unchanged);

    public Builder<T> setUnchanged(Collection<T> unchanged) {
      return setUnchanged(ImmutableList.copyOf(unchanged));
    }

    public Builder<T> addUnchanged(T unchanged) {
      unchangedBuilder().add(unchanged);
      return this;
    }

    protected abstract ImmutableList.Builder<T> failedBuilder();

    protected abstract Builder<T> setFailed(ImmutableList<T> failed);

    public Builder<T> setFailed(Collection<T> failed) {
      return setFailed(ImmutableList.copyOf(failed));
    }

    public Builder<T> addFailed(T failed) {
      failedBuilder().add(failed);
      return this;
    }

    public abstract ProcessingResponse<T> build();
  }
}
