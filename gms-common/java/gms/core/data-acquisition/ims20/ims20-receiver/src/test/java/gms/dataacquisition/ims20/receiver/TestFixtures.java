package gms.dataacquisition.ims20.receiver;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestFixtures {

  public static final Instant RECEPTION_TIME = Instant.now();
  public static final Instant
      PAYLOAD_START_TIME = Instant.parse("2019-02-14T18:30:00.000Z");

  public static final int NUMBER_SAMPLES_1 = 2400;
  public static final double SAMPLE_RATE_HZ_1 = 40.0;
  public static final double CALCULATED_SECS_1 = (NUMBER_SAMPLES_1 / SAMPLE_RATE_HZ_1);
  public static final Instant
      PAYLOAD_END_TIME_1 = PAYLOAD_START_TIME.plusSeconds((long) CALCULATED_SECS_1);

  // KURK
  // array - 1 network, 2 stations, 2 channels
  public static String kurkResponseString =
      "WID2 2019/02/14 18:30:00.000 KUR01 BHZ      CM6     2400   40.000000   5.30e-03   0.333 CMG-3V  -1.0  0.0\n"
          + "STA2 KURK       50.72161   78.56336 WGS-84       0.163 0.025\n"
          + "DAT2\n"
          + "eMvEkJY+V7oCnOUFWIWQX2lCpFl2VGlD7YHVFm79kEn0PWBl0VJYEnFm8VFl6l7VNULlNX0W-nElRl-l\n"
          + "AVIZJV5p1AFlIVEl9VRVEmIDUHVGm8o2XBZ9m5n8Y-m9p5W5Z1VGm4mMnLWMWLn1W8ZJn-pDV-VS-6V0\n"
          + "nTVHb-mFoOl6KUEYTVQo7kN6m28W9lDV7Y8m1n+JKkQkJX5W4lGW3l-p+kKWAVL-UOkTlG+UIW2UQnEl\n"
          + "IXDkNl4UOkFFPW+USkPUFlNl2V5UJWDVJp0o9VAXDV41W7m4n1VAV7kQR3IVKVLGo3lGVP3WSUGlERSl\n"
          + "W2UIo5lLWFXJW9kSn3m1m8W2Z2l3lIUOUFmEUQX4pPn5ZHW7J\n"
          + "CHK2 909248\n"
          + "WID2 2019/02/14 18:30:00.000 KURBB BH1      CM6     2400   40.000000   1.60e-02   1.000 CMG3TB 332.5 90.0\n"
          + "STA2 KURK       50.62264   78.53039 WGS-84       0.200 0.042\n"
          + "DAT2\n"
          + "kvnAUvpLkoFW8UvPs-m8ZClgNUz-Vc7qTkzGw+b9sMUpS8ZRz9wDVW3pEklBv5VU0r+lWCUuFn2iRkl+\n"
          + "u6Vv+lWAkyAUrCyTZIUp2nFklBnQW7h8Ut4lpOtFVwKxNklMkNiK4kq-VSUvJaAliAa-d+WOUq6kk3lM\n"
          + "z9g3Uy1nAkw2ljOVvMVd0ljDy8l2g5tOhKi+klJfMkmOo2Uk5UlTw6lcCVn+pElfKViIYElu0cKWVBkk\n"
          + "ElUAuQj4UxSlWRdMVcIlZ4kwHUwScLoNyPqPUrBpLw1Uq9X8lULcJUtBmOkqIYEgHkk5d9XCUkQu1lm2\n"
          + "b2Vj5UxElWLlWReOUmFcMVNUHlVHknTVw2Us1lwOkxBVaMUoQu1iIkrBcNUSlcMVwLnOlV2lGxGVjCo8\n"
          + "CHK2 74719771";

  public static final UUID STATION_ID_KURK = UUID
      .fromString("fb37fc1c-9aaf-3715-a4c7-2f97455d7a7b");

  public static final UUID CHANNEL_ID_KUR01_BHZ = UUID
      .fromString("75b9ea42-6419-3f3b-9c80-9755d9d4fd74");
  public static final UUID CHANNEL_ID_KURBB_BH1 = UUID
      .fromString("006a8f28-b052-3d0e-9ef7-89f6d05a0160");

  public static final StationAndChannelId kur01Bhz = StationAndChannelId
      .from(STATION_ID_KURK, CHANNEL_ID_KUR01_BHZ);
  public static final StationAndChannelId kurbbBh1 = StationAndChannelId
      .from(STATION_ID_KURK, CHANNEL_ID_KURBB_BH1);

  public static Map<String, StationAndChannelId> kurkMap = Map
      .of("KURK/KUR01/BHZ", kur01Bhz, "KURK/KURBB/BH1", kurbbBh1);

  // AAK
  // 3-component - 1 station, 1 channel
  public static String aakResponseString =
      "WID2 2019/02/14 18:30:00.000 AAK   BHE      CM6     2400   40.000000   6.34e-02   1.000 STS2.5  90.0 90.0\n"
          + "STA2            42.63910   74.49420 WGS-84       1.645 0.030\n"
          + "DAT2\n"
          + "kuCUt8UK7kMUGATL2UEMkEUH+O+JUGHkKUH8kGD-kMUP0kLUKHkKUG6L8-RNUH1kHUS3l5UECLPFUFJ8\n"
          + "1kJC-40Q43ITUL4kFHKUJ0PHGBI00R-CFRCMJDT29P-F31kG6BkL2BQ93kGJ8FHUIkGkKUI1HKUH7l77\n"
          + "DI8kGkFUQUEkM-PRV-JP+0UMkTKCTUMHTUHkLIUKR56T6R38N5kG5URkRNCH-kG8ULkE6JkFUFGR74PA\n"
          + "+kNC8M+1HH7kKIV4RkMV-Jl1UECKF6MR72I80kFFUH-NkJ5UKKN66kGIULMQ27+K3MJDF10JIFULkKMU\n"
          + "FNFPUH0SA1QUFKkF5-BO-Al+UF6SULkF7FkLUJ+UFOkQD+1++116kGJ6+7IKA-kEGUJMHUFkF+S8URkQ\n"
          + "CHK2 1099739\n"
          + "WID2 2019/02/14 18:30:00.000 AAK   BHN      CM6     2400   40.000000   6.34e-02   1.000 STS2.5  90.0 90.0\n"
          + "STA2            42.63910   74.49420 WGS-84       1.645 0.030\n"
          + "DAT2\n"
          + "X9n5N23P08IL40++J-2LFAJJ000L30M3--GF00GG5N01H00FK4-K3G1GG01HF2GG++0GG000L01G1F1K\n"
          + "-3I1KF7HM42L2+FH11G0H+-++H4+HG00H+0J4+K6GJ3FI00G-+-G+0I10J+3+G1FFI1I-7M0-H2HG4GI\n"
          + "++2GF-F0-H+GG7M-4HG01L21L31HJ20J10FG0+H-0-GJ23IH00-G--K01-GH21L-4LF5GJ2-F+-+H-F-\n"
          + "-F-0HF-0+++H-0I1+H0-F--FH---+F-G--F0FF0FF0GF1+-I00J4HG+20J0HG1-F+2HF-F2K1-G2HG+-\n"
          + "G\n"
          + "CHK2 2215615";

  public static final UUID STATION_ID_AAK = UUID
      .fromString("c7d44668-bbe4-369f-9f1e-7d725a57622d");

  public static final UUID CHANNEL_ID_AAK_BHE = UUID
      .fromString("aba60bf2-0e2e-372c-afff-eae36d519745");
  public static final UUID CHANNEL_ID_AAK_BHN = UUID
      .fromString("21d53cd7-8aa4-3379-b729-4a6decbc35b8");

  public static final StationAndChannelId aakBhe = StationAndChannelId
      .from(STATION_ID_AAK, CHANNEL_ID_AAK_BHE);
  public static final StationAndChannelId aakBhn = StationAndChannelId
      .from(STATION_ID_AAK, CHANNEL_ID_AAK_BHN);

  public static Map<String, StationAndChannelId> aakMap = Map
      .of("AAK/AAK/BHE", aakBhe, "AAK/AAK/BHN", aakBhn);

  public static final AcquisitionProtocol ACQUISITION_PROTOCOL_WAVEFORM = AcquisitionProtocol.IMS_WAVEFORM;

  public static final List<String> REQUESTS = List.of("");

  static Instant actualChangeTime = Instant.parse("2019-02-14T18:30:00.000Z");
  static Instant systemChangeTime = Instant.parse("2019-02-14T18:35:00.000Z");

  public static final StationDataAcquisitionGroup SDAG_KURK = StationDataAcquisitionGroup.create(
      REQUESTS, ACQUISITION_PROTOCOL_WAVEFORM, "127.0.0.1", 18000,
      actualChangeTime, systemChangeTime, kurkMap, true, "");

  public static final StationDataAcquisitionGroup SDAG_AAK = StationDataAcquisitionGroup.create(
      REQUESTS, ACQUISITION_PROTOCOL_WAVEFORM, "127.0.0.1", 18000,
      actualChangeTime, systemChangeTime, aakMap, true, "");

  public static final StationDataAcquisitionGroup SDAG = StationDataAcquisitionGroup.create(
      REQUESTS, ACQUISITION_PROTOCOL_WAVEFORM, "127.0.0.1", 18000,
      Instant.now(), Instant.now(), Map.of(), true, "");

  // Files with CM6 and INT data types
  private static final String TEST_DATA_DIR = "/ims2/cm6/";
  // CM6 data for 10 seconds of data for the KURK station
  public static final String KURK_CM6_FILE = TEST_DATA_DIR + "KURK_CM6.response";
  // KURK CM6 data start time
  public static final Instant KURK_CM6_FILE_START_TIME = Instant.parse("2019-02-14T18:30:00.000Z");
}
