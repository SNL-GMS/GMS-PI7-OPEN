package gms.processors.invokesignaldetector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvokeSignalDetectorDto {

  private final UUID stationId;
  private final Instant startTime;
  private final Instant endTime;
  private final ProcessingContext processingContext;

  private final List<String> parameterOverrideKeys;
  private final List<String> parameterOverrideValues;

  public InvokeSignalDetectorDto(UUID stationId, Instant startTime, Instant endTime,
      ProcessingContext processingContext, Map<String, String> parameterOverrides) {
    this.stationId = stationId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.processingContext = processingContext;

    // Need to convert map<string,string> into two list<string> objects because msgpack
    // does not support serializing and deserializing maps.
    parameterOverrideKeys = new ArrayList<>();
    parameterOverrideValues = new ArrayList<>();
    for (Map.Entry<String, String> e : parameterOverrides.entrySet()) {
      parameterOverrideKeys.add(e.getKey());
      parameterOverrideValues.add(e.getValue());
    }
  }

  public UUID getStationId() {
    return stationId;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  public List<String> getParameterOverrideKeys() {
    return parameterOverrideKeys;
  }

  public List<String> getParameterOverrideValues() {
    return parameterOverrideValues;
  }
}
