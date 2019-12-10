package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Triple;

public class DziewonskiGilbertEllipticityModel1dDelegate {

  public void initialize(Set<String> earthModelNames) {
    tauTableByModelPhase = new HashMap<>();
    earthModelNames.forEach(m -> {
      URL ellipticityCorrectionsUrl = this.getClass().getResource(m + "/ELCOR.dat");
      try (InputStream inputStream = ellipticityCorrectionsUrl.openStream()) {
        tauTableByModelPhase.put(m, parseCorrectionsString(new String(inputStream.readAllBytes())));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public Set<String> getModelNames() {
    return tauTableByModelPhase.keySet();
  }

  public Set<PhaseType> getPhaseTypesForModelName(String modelName) {
    return tauTableByModelPhase.get(modelName).keySet();
  }

  public double[] getDepthKmForModelPhase(String modelName, PhaseType p) {
    return tauTableByModelPhase.get(modelName).get(p).depth;
  }

  public double[] getAngleDegreesForModelPhase(String modelName, PhaseType p) {
    return tauTableByModelPhase.get(modelName).get(p).distance;
  }

  public Triple<double[][], double[][], double[][]> getTravelTimesForModelPhase(String modelName,
      PhaseType p) {
    return Triple.of(tauTableByModelPhase.get(modelName).get(p).tau0, tauTableByModelPhase.get(modelName).get(p).tau1, tauTableByModelPhase
        .get(modelName).get(p).tau2);
  }

  public Optional<double[]> getModelingErrorDistancesForModelPhase(String modelName, PhaseType p) {
    return Optional.empty();
  }

  public Optional<double[]> getModelingErrorDepthsForModelPhase(String modelName, PhaseType p) {
    return Optional.empty();
  }

  public Optional<Triple<double[][], double[][], double[][]>> getModelingErrorValuesForModelPhase(
      String modelName, PhaseType p) {
    return Optional.empty();
  }

  /**
   * Holds all necessary values for calculating ellipticity correction.  The distance and depth
   * arrays hold the X and Y values respectively for the tau0, tau1, and tau2 tables.
   */
  private static class TauTable {

    double[] distance;
    double[] depth;
    double[][] tau0;
    double[][] tau1;
    double[][] tau2;
  }

  private Map<String, Map<PhaseType, TauTable>> tauTableByModelPhase;

  /**
   * Parses a {@link String} containing elliptiticy corrections values into a new {@link Map} of
   * {@link PhaseType} to {@link TauTable}.  Each {@link PhaseType} has its own unique ellipticity
   * correction values contained in the associated {@link TauTable}.
   *
   * @param string {@link String} containg ellipticity corrections values, likely from an
   * ellipticity corrections file
   */
  private static Map<PhaseType, TauTable> parseCorrectionsString(String string) {
    Objects.requireNonNull(string,
        "StandardAsciiDgEllipticityCorrectionPlugin::parseCorrectionsString() requires non-null string parameter");

    String[] lines = string.split("\n");

    Map<PhaseType, TauTable> tauTable = new HashMap<>();

    int lineNumber = 0;
    while (lineNumber < lines.length) {
      // Each loop of this while() will be one phase type, each phase type has tau0, tau1, and tau2
      TauTable t = new TauTable();

      String[] phaseLine = lines[lineNumber].split("\\s+");
      lineNumber++;

      PhaseType p;
      int numDistances = Integer.parseInt(phaseLine[1]);
      try {
        p = PhaseType.valueOf(phaseLine[0]);
      } catch (IllegalArgumentException e) {
        lineNumber += 1 + (numDistances * 4);
        continue;
      }

      String[] depthsLine = lines[lineNumber].trim().split("\\s+");
      lineNumber++;

      int numDepths = depthsLine.length;
      double[] depthValues = new double[numDepths];
      for (int i=0; i<depthValues.length; ++i) {
        depthValues[i] = Double.parseDouble(depthsLine[i]);
      }

      double[] distanceValues = new double[numDistances];

      double[][] t0TauValues = new double[distanceValues.length][depthValues.length];

      double[][] t1TauValues = new double[distanceValues.length][depthValues.length];

      double[][] t2TauValues = new double[distanceValues.length][depthValues.length];

      for (int i = 0; i < numDistances; i++) {

        distanceValues[i] = Double.parseDouble(lines[lineNumber].trim());
        lineNumber++;

        String[] t0Strings = lines[lineNumber].trim().split("\\s+");
        lineNumber++;

        String[] t1Strings = lines[lineNumber].trim().split("\\s+");
        lineNumber++;

        String[] t2Strings = lines[lineNumber].trim().split("\\s+");
        lineNumber++;

        for (int j = 0; j < depthValues.length; j++) {
          t0TauValues[i][j] = Double.parseDouble(t0Strings[j]);
        }

        for (int j = 0; j < depthValues.length; j++) {
          t1TauValues[i][j] = Double.parseDouble(t1Strings[j]);
        }

        for (int j = 0; j < depthValues.length; j++) {
          t2TauValues[i][j] = Double.parseDouble(t2Strings[j]);
        }
      }

      t.depth = depthValues;
      t.distance = distanceValues;

      t.tau0 = t0TauValues;
      t.tau1 = t1TauValues;
      t.tau2 = t2TauValues;

      tauTable.put(p, t);
    }

    return tauTable;
  }  

}
