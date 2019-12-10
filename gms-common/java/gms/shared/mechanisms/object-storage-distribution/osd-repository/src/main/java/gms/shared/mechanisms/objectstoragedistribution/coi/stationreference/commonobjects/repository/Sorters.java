package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository;

import java.util.List;

public class Sorters {

  public static final List<String> byActualTimeSystemTime = List.of(
      "actualTime ASC", "systemTime ASC");

  public static final List<String> byNameActualTimeSystemTime = List.of(
      "name ASC", "actualTime ASC", "systemTime ASC");
}
