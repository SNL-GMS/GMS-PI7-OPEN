package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Cd11StationConfig {

  public static final List<Cd11NetworkDescriptor> fakeStations = new ArrayList<>();

  static {

    // Populate the fake stations list. Random UUIDs are used because the Cd11NetworkDescriptor
    // requires a UUID, but they are not used to validate against versionIds in the OSD
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "MKAR", 8108));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "H04N", 8100));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "H04S", 8101));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "I37NO", 8102));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "I56US", 8105));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "KSRS", 8103));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "LPAZ", 8104));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "BOSA", 8106));
    fakeStations.add(new Cd11NetworkDescriptor(UUID.randomUUID(), "LBTB", 8107));
  }
}
