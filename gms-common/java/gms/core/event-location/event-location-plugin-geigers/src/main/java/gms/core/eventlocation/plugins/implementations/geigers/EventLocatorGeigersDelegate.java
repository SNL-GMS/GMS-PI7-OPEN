package gms.core.eventlocation.plugins.implementations.geigers;

import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.core.eventlocation.plugins.pluginutils.GeneralEventLocatorDelegate;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.utilities.geomath.RowFilteredRealMatrix;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.util.Optional;

public class EventLocatorGeigersDelegate extends
    GeneralEventLocatorDelegate<RowFilteredRealMatrix> {

  public void initialize(Optional<PreferredLocationSolution> start,
      EventLocationDefinitionGeigers geigerParameters) {

    super.initialize(
        GeneralEventLocatorDelegate.getDefaultSeedGenerator(start),

        new GeigersAlgorithm.Builder()
            .withMaximumIterationCount(geigerParameters.getMaximumIterationCount())
            .withConvergenceThreshold(geigerParameters.getConvergenceThreshold())
            .withConvergenceCount(geigerParameters.getConvergenceCount())
            .withLevenbergMarquardtEnabled(geigerParameters.isLevenbergMarquardtEnabled())
            .withLambda0(geigerParameters.getLambda0())
            .withLambdaX(geigerParameters.getLambdaX())
            .withDeltaNormThreshold(geigerParameters.getDeltaNormThreshold())
            .withSingularValueWFactor(geigerParameters.getSingularValueWFactor())
            .withMaximumWeightedPartialDerivative(geigerParameters.getMaximumWeightedPartialDerivative()),
        //TODO: applyTravelTimeCorrections

        new SignalFeaturePredictionUtility()
    );
  }

}
