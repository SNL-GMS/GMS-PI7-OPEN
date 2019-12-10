package gms.core.eventlocation.plugins.implementations.apachelm;

import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionApacheLm;
import gms.core.eventlocation.plugins.pluginutils.GeneralEventLocatorDelegate;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.math3.linear.RealMatrix;

public class EventLocatorApacheLmDelegate extends
    GeneralEventLocatorDelegate<RealMatrix> {

  public void initialize(Optional<PreferredLocationSolution> start,
      EventLocationDefinitionApacheLm apacheLmParameters) {

    Objects.requireNonNull(start, "Null start");
    Objects.requireNonNull(apacheLmParameters, "Null parameters");

    super.initialize(
        GeneralEventLocatorDelegate.getDefaultSeedGenerator(start),

        new ApacheLmAlgorithm.Builder()
            .withMaximumIterationCount(apacheLmParameters.getMaximumIterationCount())
            .withResidualConvergenceThreshold(apacheLmParameters.getConvergenceThreshold()),
        //TODO: applyTravelTimeCorrections

        new SignalFeaturePredictionUtility()
    );
  }
}
