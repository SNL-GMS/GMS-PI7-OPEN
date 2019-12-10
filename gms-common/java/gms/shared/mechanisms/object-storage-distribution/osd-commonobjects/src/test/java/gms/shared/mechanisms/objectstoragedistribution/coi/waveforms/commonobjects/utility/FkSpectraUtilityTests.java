package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;

public class FkSpectraUtilityTests {

  @Test
  public void testMergeValidateArguments() {
    FkSpectra first = TestFixtures.fkSpectra;
    FkSpectra.Builder builder = first.toBuilder();
    FkSpectra.Metadata.Builder metadataBuilder = builder.metadataBuilder();

    metadataBuilder.setPhaseType(PhaseType.S);
    Executable differentPhase = metadataExecutable(first, builder.build(), "different phase");
    metadataBuilder.setPhaseType(first.getMetadata().getPhaseType());

    metadataBuilder.setSlowStartX(first.getMetadata().getSlowStartX() + 1);
    Executable differentXSlowStart = metadataExecutable(first, builder.build(),
        "different x slow start");
    metadataBuilder.setSlowStartX(first.getMetadata().getSlowStartX());

    metadataBuilder.setSlowDeltaX(first.getMetadata().getSlowDeltaX() + 1);
    Executable differentXSlowDelta = metadataExecutable(first, builder.build(),
        "different x slow delta");
    metadataBuilder.setSlowDeltaX(first.getMetadata().getSlowDeltaX());

    metadataBuilder.setSlowStartY(first.getMetadata().getSlowStartY() + 1);
    Executable differentYSlowStart = metadataExecutable(first, builder.build(),
        "different y slow start");
    metadataBuilder.setSlowStartY(first.getMetadata().getSlowStartY());

    metadataBuilder.setSlowDeltaY(first.getMetadata().getSlowDeltaY() + 1);
    Executable differentYSlowDelta = metadataExecutable(first, builder.build(),
        "different y slow delta");
    metadataBuilder.setSlowDeltaY(first.getMetadata().getSlowDeltaY());

    assertAll("Merge argument validation",
        differentPhase,
        differentXSlowStart,
        differentXSlowDelta,
        differentYSlowStart,
        differentYSlowDelta);
  }

  private static Executable metadataExecutable(FkSpectra first, FkSpectra second, String message) {
    return () -> assertThrows(IllegalStateException.class, () -> FkSpectraUtility
        .mergeChannelSegments(TestFixtures.createFkChannelSegments(first, second)), message);
  }

  /**
   * The following set of unit tests involve testing the merging of subsets of 5 numbered fkspectra
   * connected in time. This allows us to cover all cases of merging due to gaps.
   *
   * 1:*
   * 2: *
   * 3:  *
   * 4:   *
   * 5:    *
   */

  @Test
  public void testMerge123() {
    FkSpectra first = TestFixtures.fkSpectra;
    FkSpectra second = TestFixtures.fkSpectra2;
    FkSpectra third = TestFixtures.fkSpectra3;

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, second, third);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(1, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra = actualSegment.getTimeseries().get(0);
    assertAll("Spectra Equality",
        () -> assertEquals(first.getStartTime(), actualSpectra.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra.getSampleRate()),
        () -> assertEquals(3, actualSpectra.getSampleCount()),
        () -> assertEquals(3, actualSpectra.getValues().size()),
        () -> assertEquals(first.getMetadata(), actualSpectra.getMetadata()));

    assertAll("Spectrum Equality",
        () -> assertEquals(first.getValues().get(0), actualSpectra.getValues().get(0)),
        () -> assertEquals(second.getValues().get(0), actualSpectra.getValues().get(1)),
        () -> assertEquals(third.getValues().get(0), actualSpectra.getValues().get(2)));
  }

  @Test
  public void testMerge123WithoutValues() {
    FkSpectra first = TestFixtures.withoutValues(TestFixtures.fkSpectra);
    FkSpectra second = TestFixtures.withoutValues(TestFixtures.fkSpectra2);
    FkSpectra third = TestFixtures.withoutValues(TestFixtures.fkSpectra3);

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, second, third);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(1, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra = actualSegment.getTimeseries().get(0);
    assertAll("Spectra Equality",
        () -> assertEquals(first.getStartTime(), actualSpectra.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra.getSampleRate()),
        () -> assertEquals(3, actualSpectra.getSampleCount()),
        () -> assertTrue(actualSpectra.getValues().isEmpty()),
        () -> assertEquals(first.getMetadata(), actualSpectra.getMetadata()));
  }

  @Test
  public void testMerge124() {
    FkSpectra first = TestFixtures.fkSpectra;
    FkSpectra second = TestFixtures.fkSpectra2;
    FkSpectra fourth = TestFixtures.fkSpectra4;

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, second, fourth);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(2, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra1 = actualSegment.getTimeseries().get(0);
    FkSpectra actualSpectra2 = actualSegment.getTimeseries().get(1);

    assertAll("Spectra Equality (1)",
        () -> assertEquals(first.getStartTime(), actualSpectra1.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra1.getSampleRate()),
        () -> assertEquals(2, actualSpectra1.getSampleCount()),
        () -> assertEquals(2, actualSpectra1.getValues().size()),
        () -> assertEquals(first.getMetadata(), actualSpectra1.getMetadata()));

    assertAll("Spectra Equality (2)",
        () -> assertEquals(fourth.getStartTime(), actualSpectra2.getStartTime()),
        () -> assertEquals(fourth.getSampleRate(), actualSpectra2.getSampleRate()),
        () -> assertEquals(1, actualSpectra2.getSampleCount()),
        () -> assertEquals(1, actualSpectra2.getValues().size()),
        () -> assertEquals(fourth.getMetadata(), actualSpectra2.getMetadata()));

    assertAll("Spectrum Equality",
        () -> assertEquals(first.getValues().get(0), actualSpectra1.getValues().get(0)),
        () -> assertEquals(second.getValues().get(0), actualSpectra1.getValues().get(1)),
        () -> assertEquals(fourth.getValues().get(0), actualSpectra2.getValues().get(0)));
  }

  @Test
  public void testMerge124WithoutValues() {
    FkSpectra first = TestFixtures.withoutValues(TestFixtures.fkSpectra);
    FkSpectra second = TestFixtures.withoutValues(TestFixtures.fkSpectra2);
    FkSpectra fourth = TestFixtures.withoutValues(TestFixtures.fkSpectra4);

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, second, fourth);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(2, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra1 = actualSegment.getTimeseries().get(0);
    FkSpectra actualSpectra2 = actualSegment.getTimeseries().get(1);

    assertAll("Spectra Equality (1)",
        () -> assertEquals(first.getStartTime(), actualSpectra1.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra1.getSampleRate()),
        () -> assertEquals(2, actualSpectra1.getSampleCount()),
        () -> assertTrue(actualSpectra1.getValues().isEmpty()),
        () -> assertEquals(first.getMetadata(), actualSpectra1.getMetadata()));

    assertAll("Spectra Equality (2)",
        () -> assertEquals(fourth.getStartTime(), actualSpectra2.getStartTime()),
        () -> assertEquals(fourth.getSampleRate(), actualSpectra2.getSampleRate()),
        () -> assertEquals(1, actualSpectra2.getSampleCount()),
        () -> assertTrue(actualSpectra2.getValues().isEmpty()),
        () -> assertEquals(fourth.getMetadata(), actualSpectra2.getMetadata()));
  }

  @Test
  public void testMerge134() {
    FkSpectra first = TestFixtures.fkSpectra;
    FkSpectra third = TestFixtures.fkSpectra3;
    FkSpectra fourth = TestFixtures.fkSpectra4;

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, third, fourth);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(2, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra1 = actualSegment.getTimeseries().get(0);
    FkSpectra actualSpectra2 = actualSegment.getTimeseries().get(1);

    assertAll("Spectra Equality (1)",
        () -> assertEquals(first.getStartTime(), actualSpectra1.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra1.getSampleRate()),
        () -> assertEquals(1, actualSpectra1.getSampleCount()),
        () -> assertEquals(1, actualSpectra1.getValues().size()),
        () -> assertEquals(first.getMetadata(), actualSpectra1.getMetadata()));

    assertAll("Spectra Equality (2)",
        () -> assertEquals(third.getStartTime(), actualSpectra2.getStartTime()),
        () -> assertEquals(third.getSampleRate(), actualSpectra2.getSampleRate()),
        () -> assertEquals(2, actualSpectra2.getSampleCount()),
        () -> assertEquals(2, actualSpectra2.getValues().size()),
        () -> assertEquals(third.getMetadata(), actualSpectra2.getMetadata()));

    assertAll("Spectrum Equality",
        () -> assertEquals(first.getValues().get(0), actualSpectra1.getValues().get(0)),
        () -> assertEquals(third.getValues().get(0), actualSpectra2.getValues().get(0)),
        () -> assertEquals(fourth.getValues().get(0), actualSpectra2.getValues().get(1)));
  }

  @Test
  public void testMerge134WithoutValues() {
    FkSpectra first = TestFixtures.withoutValues(TestFixtures.fkSpectra);
    FkSpectra third = TestFixtures.withoutValues(TestFixtures.fkSpectra3);
    FkSpectra fourth = TestFixtures.withoutValues(TestFixtures.fkSpectra4);

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, third, fourth);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(2, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra1 = actualSegment.getTimeseries().get(0);
    FkSpectra actualSpectra2 = actualSegment.getTimeseries().get(1);

    assertAll("Spectra Equality (1)",
        () -> assertEquals(first.getStartTime(), actualSpectra1.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra1.getSampleRate()),
        () -> assertEquals(1, actualSpectra1.getSampleCount()),
        () -> assertTrue(actualSpectra1.getValues().isEmpty()),
        () -> assertEquals(first.getMetadata(), actualSpectra1.getMetadata()));

    assertAll("Spectra Equality (2)",
        () -> assertEquals(third.getStartTime(), actualSpectra2.getStartTime()),
        () -> assertEquals(third.getSampleRate(), actualSpectra2.getSampleRate()),
        () -> assertEquals(2, actualSpectra2.getSampleCount()),
        () -> assertTrue(actualSpectra2.getValues().isEmpty()),
        () -> assertEquals(third.getMetadata(), actualSpectra2.getMetadata()));
  }

  @Test
  public void testMerge135() {
    FkSpectra first = TestFixtures.fkSpectra;
    FkSpectra third = TestFixtures.fkSpectra3;
    FkSpectra fifth = TestFixtures.fkSpectra5;

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, third, fifth);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(3, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra1 = actualSegment.getTimeseries().get(0);
    FkSpectra actualSpectra2 = actualSegment.getTimeseries().get(1);
    FkSpectra actualSpectra3 = actualSegment.getTimeseries().get(2);

    assertAll("Spectra Equality (1)",
        () -> assertEquals(first.getStartTime(), actualSpectra1.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra1.getSampleRate()),
        () -> assertEquals(1, actualSpectra1.getSampleCount()),
        () -> assertEquals(1, actualSpectra1.getValues().size()),
        () -> assertEquals(first.getMetadata(), actualSpectra1.getMetadata()));

    assertAll("Spectra Equality (2)",
        () -> assertEquals(third.getStartTime(), actualSpectra2.getStartTime()),
        () -> assertEquals(third.getSampleRate(), actualSpectra2.getSampleRate()),
        () -> assertEquals(1, actualSpectra2.getSampleCount()),
        () -> assertEquals(1, actualSpectra2.getValues().size()),
        () -> assertEquals(third.getMetadata(), actualSpectra2.getMetadata()));

    assertAll("Spectra Equality (3)",
        () -> assertEquals(fifth.getStartTime(), actualSpectra3.getStartTime()),
        () -> assertEquals(fifth.getSampleRate(), actualSpectra3.getSampleRate()),
        () -> assertEquals(1, actualSpectra3.getSampleCount()),
        () -> assertEquals(1, actualSpectra3.getValues().size()),
        () -> assertEquals(fifth.getMetadata(), actualSpectra3.getMetadata()));

    assertAll("Spectrum Equality",
        () -> assertEquals(first.getValues().get(0), actualSpectra1.getValues().get(0)),
        () -> assertEquals(third.getValues().get(0), actualSpectra2.getValues().get(0)),
        () -> assertEquals(fifth.getValues().get(0), actualSpectra3.getValues().get(0)));
  }

  @Test
  public void testMerge135WithoutValues() {
    FkSpectra first = TestFixtures.withoutValues(TestFixtures.fkSpectra);
    FkSpectra third = TestFixtures.withoutValues(TestFixtures.fkSpectra3);
    FkSpectra fifth = TestFixtures.withoutValues(TestFixtures.fkSpectra5);

    List<ChannelSegment<FkSpectra>> fks = TestFixtures
        .createFkChannelSegments(first, third, fifth);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(0).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(0).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(0).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(0).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(0).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(3, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra1 = actualSegment.getTimeseries().get(0);
    FkSpectra actualSpectra2 = actualSegment.getTimeseries().get(1);
    FkSpectra actualSpectra3 = actualSegment.getTimeseries().get(2);

    assertAll("Spectra Equality (1)",
        () -> assertEquals(first.getStartTime(), actualSpectra1.getStartTime()),
        () -> assertEquals(first.getSampleRate(), actualSpectra1.getSampleRate()),
        () -> assertEquals(1, actualSpectra1.getSampleCount()),
        () -> assertTrue(actualSpectra1.getValues().isEmpty()),
        () -> assertEquals(first.getMetadata(), actualSpectra1.getMetadata()));

    assertAll("Spectra Equality (2)",
        () -> assertEquals(third.getStartTime(), actualSpectra2.getStartTime()),
        () -> assertEquals(third.getSampleRate(), actualSpectra2.getSampleRate()),
        () -> assertEquals(1, actualSpectra2.getSampleCount()),
        () -> assertTrue(actualSpectra2.getValues().isEmpty()),
        () -> assertEquals(third.getMetadata(), actualSpectra2.getMetadata()));

    assertAll("Spectra Equality (3)",
        () -> assertEquals(fifth.getStartTime(), actualSpectra3.getStartTime()),
        () -> assertEquals(fifth.getSampleRate(), actualSpectra3.getSampleRate()),
        () -> assertEquals(1, actualSpectra3.getSampleCount()),
        () -> assertTrue(actualSpectra3.getValues().isEmpty()),
        () -> assertEquals(fifth.getMetadata(), actualSpectra3.getMetadata()));
  }


  @Test
  public void TestMergeContainsReturnsLargerSegment() {
    FkSpectra firstsecond = TestFixtures.fkSpectra(TestFixtures.fkSpectra.getStartTime(),
        TestFixtures.fkSpectrum, TestFixtures.fkSpectrum2);

    FkSpectra first = TestFixtures.fkSpectra;

    FkSpectra second = TestFixtures.fkSpectra2;

    List<ChannelSegment<FkSpectra>> fks = TestFixtures.createFkChannelSegments(first, second, firstsecond);

    ChannelSegment<FkSpectra> actualSegment = FkSpectraUtility
        .mergeChannelSegments(fks);

    assertAll("Channel Equality",
        () -> assertEquals(fks.get(2).getChannelId(), actualSegment.getChannelId()),
        () -> assertEquals(fks.get(2).getName(), actualSegment.getName()),
        () -> assertEquals(fks.get(2).getType(), actualSegment.getType()),
        () -> assertEquals(fks.get(2).getTimeseriesType(), actualSegment.getTimeseriesType()),
        () -> assertEquals(fks.get(2).getStartTime(), actualSegment.getStartTime()),
        () -> assertEquals(fks.get(2).getEndTime(), actualSegment.getEndTime()));

    assertEquals(1, actualSegment.getTimeseries().size());
    assertEquals(firstsecond, actualSegment.getTimeseries().get(0));
  }
}

