package gms.dataacquisition.ims20.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HardCodedIms20Config {

  private static final UUID KURK = UUID.fromString("fb37fc1c-9aaf-3715-a4c7-2f97455d7a7b");
  private static final UUID AAK  = UUID.fromString("c7d44668-bbe4-369f-9f1e-7d725a57622d");

  private static final Map<String, StationAndChannelId> M = new HashMap<>();

  static {
    // station KURK
    M.put("KURK/KUR01/BHZ", ids(KURK, "75b9ea42-6419-3f3b-9c80-9755d9d4fd74"));
    M.put("KURK/KUR02/BHZ", ids(KURK, "795d2ae4-a369-31cb-8857-9097620c4cec"));
    M.put("KURK/KUR03/BHZ", ids(KURK, "37d692d7-0267-3a16-8d7f-551d2ece45fa"));
    M.put("KURK/KUR04/BHZ", ids(KURK, "5120ead3-cce2-31aa-9f2d-c65e46fcd800"));
    M.put("KURK/KUR05/BHZ", ids(KURK, "7f410ecd-1d9a-343f-b274-1ce32ccf19e5"));
    M.put("KURK/KUR06/BHZ", ids(KURK, "61858cac-56ec-3ca4-b31e-940c9147083d"));
    M.put("KURK/KUR07/BHZ", ids(KURK, "54f364a5-3509-375b-a9af-1ffffe5d8a2d"));
    M.put("KURK/KUR08/BHZ", ids(KURK, "99726183-a6d8-3540-b2ca-ef0dffd0c91b"));
    M.put("KURK/KUR09/BHZ", ids(KURK, "a9c430e8-da16-39b9-b6b6-58f73c39ac41"));
    M.put("KURK/KUR10/BHZ", ids(KURK, "531fc7aa-f0eb-3776-8c0d-ca12a54f4c76"));
    M.put("KURK/KUR11/BHZ", ids(KURK, "fa30cd9d-1ae1-336d-93b8-4abfa9734917"));
    M.put("KURK/KUR12/BHZ", ids(KURK, "8ef5646e-d6da-3d79-9e96-2a8ecf56df4b"));
    M.put("KURK/KUR13/BHZ", ids(KURK, "e757dc53-d856-30e7-af30-2129b0d339f5"));
    M.put("KURK/KUR14/BHZ", ids(KURK, "84321d57-0c1d-33ac-a01f-15d5fca308f0"));
    M.put("KURK/KUR15/BHZ", ids(KURK, "7ee0aa7c-2b24-3718-adaf-1ecff28b3d89"));
    M.put("KURK/KUR16/BHZ", ids(KURK, "69e7475e-68cd-31cb-8071-4904e1ca9302"));
    M.put("KURK/KUR17/BHZ", ids(KURK, "eb67666f-6a20-3ca5-8dbd-3429ec68b003"));
    M.put("KURK/KUR18/BHZ", ids(KURK, "33e39a96-76d5-37f3-b39f-399e1e9447a0"));
    M.put("KURK/KUR19/BHZ", ids(KURK, "393ae525-118d-3a54-9484-5597e2c1271d"));
    M.put("KURK/KUR20/BHZ", ids(KURK, "FD5E9171-0B08-38D4-898E-FF727CA17E1B"));
    M.put("KURK/KURBB/BH1", ids(KURK, "006a8f28-b052-3d0e-9ef7-89f6d05a0160"));
    M.put("KURK/KURBB/BH2", ids(KURK, "f0de344c-542d-35f3-adcd-000f2842f06b"));
    M.put("KURK/KURBB/BHZ", ids(KURK, "41f8a0f6-f8a1-31b9-b073-95f1c7fda5c0"));

    // station AAK
    M.put("AAK/AAK/BHE", ids(AAK, "aba60bf2-0e2e-372c-afff-eae36d519745"));
    M.put("AAK/AAK/BHN", ids(AAK, "21d53cd7-8aa4-3379-b729-4a6decbc35b8"));
    M.put("AAK/AAK/BHZ", ids(AAK, "5f39af69-efa3-32fa-8c5e-542739fdc02d"));
  }

  private static StationAndChannelId ids(UUID stationId, String chanIdString) {
    return StationAndChannelId.from(stationId, UUID.fromString(chanIdString));
  }

  public static Map<String, StationAndChannelId> getMappings() {
    return Collections.unmodifiableMap(M);
  }

  public static void main(String[] args) throws IOException {
    final ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();
    final String mappingsJson = om.writeValueAsString(getMappings());
    System.out.println("mappings = " + mappingsJson);
  }

}

