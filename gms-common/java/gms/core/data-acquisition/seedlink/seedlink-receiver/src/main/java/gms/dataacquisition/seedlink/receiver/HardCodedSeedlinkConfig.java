package gms.dataacquisition.seedlink.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HardCodedSeedlinkConfig {

  private static final List<String> REQUEST_STRINGS = new ArrayList<>();

  private static final UUID
//      PMG = UUID.fromString("bac60ff6-83ee-3245-b30c-e0e44b6ffc49"),
      ULN = UUID.fromString("c86ec6c1-26a6-335f-b2aa-be083d3e2081"),
      PDAR = UUID.fromString("3308666b-f9d8-3bff-a59e-928730ffa797"),
      TXAR = UUID.fromString("565ca127-6d78-32ba-bdc9-ce05fc3b8ddf");


  private static final Map<String, StationAndChannelId> M = new HashMap<>();

  static {
    // station PMG
//    M.put("IU/PMG/BH1/00", ids(PMG, "e95ff998-0948-3617-bc96-27fab0bd09da"));
//    M.put("IU/PMG/BH1/10", ids(PMG, "446734fd-86a7-3b1a-b5f6-c334579b514c"));
//    M.put("IU/PMG/BH1/60", ids(PMG, "8a2b7746-67a0-3e99-8e5f-e8fba3b9420f"));
//    M.put("IU/PMG/BH2/00", ids(PMG, "f2e0791d-1b2a-348b-8906-535de664f37c"));
//    M.put("IU/PMG/BH2/10", ids(PMG, "fe27d038-1650-3020-a7c3-932bd8f18e6f"));
//    M.put("IU/PMG/BH2/60", ids(PMG, "113377f5-310e-317d-911b-db51c40f2293"));
//    M.put("IU/PMG/BHZ/00", ids(PMG, "0ff10d79-adab-3439-b109-6e35423807a2"));
//    M.put("IU/PMG/BHZ/10", ids(PMG, "c0c1feb2-88f5-3099-af13-f3b703d30945"));
//    M.put("IU/PMG/BHZ/60", ids(PMG, "a086d525-51c6-3dc8-9d5c-64adcb4f117f"));
//    M.put("IU/PMG/HH1/10", ids(PMG, "84fe3a33-6ece-39aa-bde7-a0664de3b80f"));
//    M.put("IU/PMG/HH2/10", ids(PMG, "8834b8b3-b8cd-3349-950c-7abbb22a1644"));
//    M.put("IU/PMG/HHZ/10", ids(PMG, "3b9cbf29-e2a6-3890-8656-3cd8188a848b"));
    // station ULN
    M.put("IU/ULN/BH1/00", ids(ULN, "e97721f1-1bdd-31cd-9739-24746ae38d52"));
    M.put("IU/ULN/BH1/10", ids(ULN, "3d40700f-17ef-337f-ac49-e9dec9dd2311"));
    M.put("IU/ULN/BH2/00", ids(ULN, "49f438bb-540e-35b7-9deb-564d3c74e14d"));
    M.put("IU/ULN/BH2/10", ids(ULN, "bfe88e01-f244-3e58-8839-0f1198c3a2e9"));
    M.put("IU/ULN/BHZ/00", ids(ULN, "0c5ceef9-f3dd-3a71-a433-559253f91710"));
    M.put("IU/ULN/BHZ/10", ids(ULN, "f1d907b9-cffb-38e6-8a4d-80c2239b9af5"));
    M.put("IU/ULN/HH1/10", ids(ULN, "a2b5c8f8-c0df-3194-a467-67119ada0b17"));
    M.put("IU/ULN/HH2/10", ids(ULN, "5a5236ef-1af9-3bc3-87b0-f2e0a0175b1e"));
    M.put("IU/ULN/HHZ/10", ids(ULN, "228c679a-b49a-32ef-931f-6ebcccc7fb90"));
    // array PDAR, station PD01 - PD13, PD31, PD32
    M.put("IM/PD01/SHZ/", ids(PDAR, "3bca2889-d638-3d25-8219-4c046cba4330"));
    M.put("IM/PD02/SHZ/", ids(PDAR, "c8a8e4f8-3a48-3d4f-beba-17a861aa5306"));
    M.put("IM/PD03/SHZ/", ids(PDAR, "1f2e35b6-fe75-345b-9256-1fbf49a9562d"));
    M.put("IM/PD04/SHZ/", ids(PDAR, "a0445a17-5c14-3ddf-baf1-a582df98ba49"));
    M.put("IM/PD05/SHZ/", ids(PDAR, "4f4dd1e5-6627-3502-b0a2-d8ee2a148ad8"));
    M.put("IM/PD06/SHZ/", ids(PDAR, "630aa95c-7528-3b43-8d7d-f172efb2a909"));
    M.put("IM/PD07/SHZ/", ids(PDAR, "3c23bfa1-0e02-31e6-b11a-8d0ca33f66cc"));
    M.put("IM/PD08/SHZ/", ids(PDAR, "f866a188-6df6-385f-9963-5c1485439da1"));
    M.put("IM/PD09/SHZ/", ids(PDAR, "34fa5a8c-2d2e-3281-9d52-ca5181ab81e5"));
    M.put("IM/PD10/SHZ/", ids(PDAR, "a3af1caa-1579-32f2-85e8-378cacfd5233"));
    M.put("IM/PD11/SHZ/", ids(PDAR, "960ab525-f4c7-3669-871b-d7996adf2d22"));
    M.put("IM/PD12/SHZ/", ids(PDAR, "3c4f60f1-de07-3cc1-9e46-ebeb423c1124"));
    M.put("IM/PD13/SHZ/", ids(PDAR, "c71a4994-837e-3608-ba3e-b55f5cae32f6"));
    M.put("IM/PD31/BHE/", ids(PDAR, "ab74022d-7374-373e-9b00-2e7350c5b87b"));
    M.put("IM/PD31/BHN/", ids(PDAR, "1711bc5c-6855-3d43-8163-d86098024abb"));
    M.put("IM/PD31/BHZ/", ids(PDAR, "bfa65bf8-c384-3918-943a-c74ee86683c8"));
    M.put("IM/PD32/SHE/", ids(PDAR, "08e736e5-a390-3553-9093-8ece9050d7a4"));
    M.put("IM/PD32/SHN/", ids(PDAR, "010820ee-49ce-3b81-9bbb-d0e5d0c90dfa"));
    M.put("IM/PD32/SHZ/", ids(PDAR, "5c93b628-6b57-323c-8533-d8fdf6f2518c"));
    // array TXAR, station TX01 - TX04, TX06-TX10, TX31, TX32
    M.put("IM/TX01/SHZ/", ids(TXAR, "2435a511-a234-3831-b974-4ef2f8f3a896"));
    M.put("IM/TX02/SHZ/", ids(TXAR, "ca7daefb-4c1a-380f-bc59-5cbe1e062427"));
    M.put("IM/TX03/SHZ/", ids(TXAR, "57d66b29-a6bb-3796-8c82-aabb4574c3cb"));
    M.put("IM/TX04/SHZ/", ids(TXAR, "531dc765-707f-3018-93c1-62e05412ee92"));
    M.put("IM/TX06/SHZ/", ids(TXAR, "c71a60a0-ad5d-3215-8598-f3bdec40f2cc"));
    M.put("IM/TX07/SHZ/", ids(TXAR, "61c8863e-adbd-3720-9b67-b216ccbca9a0"));
    M.put("IM/TX08/SHZ/", ids(TXAR, "63ce3f7d-c073-317b-9cb0-c7be4a0472eb"));
    M.put("IM/TX09/SHZ/", ids(TXAR, "d0a1352f-d072-33c3-b86f-517e86fd6ccd"));
    M.put("IM/TX10/SHZ/", ids(TXAR, "2ee36e1f-861f-337f-a2d3-411765d2e0d5"));
//    M.put("IM/TX11/SHE/", ids(TXAR, "469682a7-41e5-3b20-a159-08a9294d5844"));
//    M.put("IM/TX11/SHN/", ids(TXAR, "4f0564b9-1f54-32b4-95c2-170e389b2a8e"));
//    M.put("IM/TX11/SHZ/", ids(TXAR, "37c80c19-4d14-3a67-90a7-a4839f853a47"));
    M.put("IM/TX31/BHE/", ids(TXAR, "82bc0127-2639-36cf-b538-3937d6ad8793"));
    M.put("IM/TX31/BHN/", ids(TXAR, "6fb03580-d8e7-31da-a548-9d5845a8c4d6"));
    M.put("IM/TX31/BHZ/", ids(TXAR, "28ae16d8-f343-3514-83bf-d9f89eac8866"));
    M.put("IM/TX32/BHE/", ids(TXAR, "2f1c9126-3fef-3774-b371-a9a755bb9d6B"));
    M.put("IM/TX32/BHN/", ids(TXAR, "34f3eed4-e3d7-3592-a3ba-d9ad2c90d301"));
    M.put("IM/TX32/BHZ/", ids(TXAR, "df869c1f-94b1-3893-9761-e73e4d3701b0"));

    // request strings for seedlink handshaking
    REQUEST_STRINGS.add("STATION ULN IU");
    REQUEST_STRINGS.add("SELECT ??BH?.D");
    REQUEST_STRINGS.add("SELECT ??HH?.D");

    //**AR stations need to SELECT explicitly
    REQUEST_STRINGS.add("STATION PD01 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD02 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD03 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD04 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD05 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD06 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD07 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD08 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD09 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD10 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD11 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD12 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD13 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION PD31 IM");
    REQUEST_STRINGS.add("SELECT BHE.D");
    REQUEST_STRINGS.add("SELECT BHN.D");
    REQUEST_STRINGS.add("SELECT BHZ.D");
    REQUEST_STRINGS.add("STATION PD32 IM");
    REQUEST_STRINGS.add("SELECT SHE.D");
    REQUEST_STRINGS.add("SELECT SHN.D");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    //TX*
    REQUEST_STRINGS.add("STATION TX01 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX02 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX03 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX04 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX05 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX06 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX07 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX08 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX09 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX10 IM");
    REQUEST_STRINGS.add("SELECT SHZ.D");
    REQUEST_STRINGS.add("STATION TX31 IM");
    REQUEST_STRINGS.add("SELECT BHE.D");
    REQUEST_STRINGS.add("SELECT BHN.D");
    REQUEST_STRINGS.add("SELECT BHZ.D");
    REQUEST_STRINGS.add("STATION TX32 IM");
    REQUEST_STRINGS.add("SELECT BHE.D");
    REQUEST_STRINGS.add("SELECT BHN.D");
    REQUEST_STRINGS.add("SELECT BHZ.D");
  }

  public static Map<String, StationAndChannelId> getMappings() {
    return Collections.unmodifiableMap(M);
  }

  public static List<String> getRequestStrings() {
    return Collections.unmodifiableList(REQUEST_STRINGS);
  }

  public static void main(String[] args) throws IOException {
    final ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();
    final String mappingsJson = om.writeValueAsString(getMappings());
    System.out.println("mappings = " + mappingsJson);
    System.out.println("requestStrings = " + om.writeValueAsString(getRequestStrings()));
    final Map<String, StationAndChannelId> deser = om.readValue(mappingsJson,
        om.getTypeFactory().constructMapType(HashMap.class, String.class, StationAndChannelId.class));
    System.out.println("deserialized map: " + deser);
  }

  private static StationAndChannelId ids(UUID stationId, String chanIdString) {
    return StationAndChannelId.from(stationId, UUID.fromString(chanIdString));
  }
}
