package gms.core.signalenhancement.beam.core;

import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.constraints.DefaultConstraint;
import gms.shared.mechanisms.configuration.constraints.StringConstraint;
import gms.shared.mechanisms.configuration.constraints.TimeOfDayRange;
import gms.shared.mechanisms.configuration.constraints.TimeOfDayRangeConstraint;
import gms.shared.mechanisms.configuration.constraints.TimeOfYearRange;
import gms.shared.mechanisms.configuration.constraints.TimeOfYearRangeConstraint;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TestUtil {

  static BeamDefinitionFile exampleMKARBeamRecipe() {

    double[] azimuth = {
        53.083487527974796,
        98.4219459268874,
        105.1628154664315,
        105.33864941019878,
        73.0717784451373,
        81.56485729721867,
        162.256204725241,
        121.1783560178298,
        228.14279120231492,
        98.06562061061669,
        98.23416843048996
    };

    double[] slowness = {
        0.040931678676839806,
        0.06022357793894427,
        0.0682695641481502,
        .06751015004210807,
        0.07302259689481606,
        0.07216124921552042,
        0.07759591881718156,
        0.06689781578273099,
        0.07362112407771981,
        0.06363210098807612,
        0.06340635332411894
    };

    String[] outputChannelIds = {
        "985f22db-2877-43a8-b01f-fc95c4f39a69",
        "fcb09e96-003a-4e88-8e13-4ba9f2fc993f",
        "4b7260c8-907e-4aa1-8375-36be3186bc6d",
        "6b160154-8b59-4820-84a5-f196b72ffe6f",
        "0f79f45f-fac3-4d41-a1fe-5bf9c56662e1",
        "1701952b-1293-4c01-bc70-9e6fe68773d3",
        "0272ab52-396a-4140-b184-d44310156e65",
        "bb37337a-4842-4d1f-b766-99089a886d6c",
        "8f66e02e-233f-426b-bdfb-9b951ca425c9",
        "4bbf9e76-ca75-4540-8a70-6dd0bdb25a1a",
        "c4719ffa-1b24-4b3e-b3ce-5ec0bae7f6a8"
    };

    List<SlownessAzimuthPair> grid = new ArrayList<>();

    for (int i = 0; i < outputChannelIds.length; i++) {
      grid.add(
          SlownessAzimuthPair.from(slowness[i], azimuth[i]));
    }

    return BeamDefinitionFile.from(
        PhaseType.P,
        true,
        true,
        true,
        20.,
        0.5,
        grid,
        Location.from(46.793683, 82.290569, 0., 0.6176),
        Map.of(
            UUID.fromString("73c4b1d0-9c03-4ae8-9627-ab02a86f4763"), RelativePosition
                .from(-2.74705572395441, 0.7702303946063784, -0.009500000000000064),
            UUID.fromString("8d7478a5-4b64-42f7-be1f-5db79466a3fd"),
            RelativePosition.from(-2.6245493337720838, 1.3747319605396393, 0.017899999999999916),
            UUID.fromString("531443af-7d91-4b7c-8ea9-29829cc0aafd"),
            RelativePosition.from(-3.0993449712215355, 0.8174486664086149, 0.011699999999999933),
            UUID.fromString("9461e4f7-91e4-4a95-b8e6-9b05e0be2e78"),
            RelativePosition.from(-2.481921658087334, 0.35226993930012235, -0.019500000000000073),
            UUID.fromString("ff3867b0-97ef-4858-9d10-3f9b921056c3"), RelativePosition
                .from(0.029681675298326746, 0.040464802873514255, -0.012600000000000056),
            UUID.fromString("9541f309-b70f-4ce4-b7c9-ae206f68cdbc"),
            RelativePosition.from(-2.0802741193137213, 1.7260116934242136, 0.0252),
            UUID.fromString("7bfeea91-a06e-4c2a-847f-94133ee4461c"),
            RelativePosition.from(-4.474707094031362, 1.9166868614157464, 0.022199999999999998),
            UUID.fromString("ec682ff5-4806-4383-b12c-495358126a5e"),
            RelativePosition.from(-4.265490191741564, -0.5892401826252712, 0.0035999999999999366),
            UUID.fromString("7265ac9e-ee94-4e7a-8581-782145e7878e"),
            RelativePosition.from(-2.1297435781448275, -1.0744949911376485, -0.04050000000000009),
            UUID.fromString("671d4db0-773c-46d8-b2a5-a9ceffe5058f"),
            RelativePosition.from(1.9246774295742795, 4.4549363977030145, 0.07279999999999998)),
        2);
  }

  public static List<Constraint> exampleMKARConstraints() {
    return List.of(
        DefaultConstraint.from(),
        StringConstraint.from(
            "channelGroupProcessingId",
            Operator.from(Type.IN, false),
            Set.of("0e7b18b9-d7ab-438d-a224-5808f777a638"), 100),
        TimeOfDayRangeConstraint.from(
            "timeOfDay",
            Operator.from(Type.IN, false),
            TimeOfDayRange.from(LocalTime.of(20, 0, 0, 0),
                LocalTime.of(5, 0, 0, 0)), 100),

        TimeOfYearRangeConstraint.from(
            "timeOfYear",
            Operator.from(Type.IN, false),
            TimeOfYearRange.from(LocalDateTime.of(2017, 10, 1, 0, 0),
                LocalDateTime.of(2017, 11, 1, 0, 0)), 100)

    );
  }

  public static ConfigurationOption exampleMKARConfigurationOption() {
    return ConfigurationOption.from("name", exampleMKARConstraints(),
        ObjectSerialization.toFieldMap(exampleMKARBeamRecipe()));
  }

  public static void serializeConfigurationOption() throws Exception {
    System.out.println(
        ObjectSerialization.getObjectMapper().writeValueAsString(exampleMKARConfigurationOption()));
  }

  public static void main(String[] args) throws Exception {
    serializeConfigurationOption();
  }
}

