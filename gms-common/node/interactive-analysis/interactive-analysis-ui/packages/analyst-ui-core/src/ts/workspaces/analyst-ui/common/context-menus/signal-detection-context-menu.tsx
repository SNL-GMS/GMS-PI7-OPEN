import { ContextMenu, Menu, MenuItem } from '@blueprintjs/core';
import { OperationVariables } from 'apollo-client';
import * as Immutable from 'immutable';
import { includes } from 'lodash';
import * as React from 'react';
import { MutationFn } from 'react-apollo';
import { systemConfig } from '~analyst-ui/config';
import { CommonTypes, SignalDetectionTypes } from '~graphql/';
import { ChangeSignalDetectionAssociationsMutationArgs, Event } from '~graphql/event/types';
import { SignalDetection, SignalDetectionHypothesis } from '~graphql/signal-detection/types';
import {
  findPhaseFeatureMeasurementValue
} from '~graphql/signal-detection/utils';
import { MeasurementMode, Mode } from '~state/analyst-workspace/types';
import { PhaseSelectionMenu } from '../dialogs/phase-selection-menu';

/**
 * DetectionRePhaser
 * a callback which executes re-phase logic
 * function to initiate re-phasing
 */
export type DetectionRePhaser
  = (sdIds: string[], phase: string) => void;

/**
 * DetectionRejecter
 * function to initiate rejecting detection
 */
export type DetectionRejecter
  = (sdIds: string[]) => void;

/**
 * DetectionFkGenerator
 * function to generate detections for fk's
 */
export type DetectionFkGenerator
  = () => void;

export type SignalDetectionAssociator =
  (signalDetectionHypoIds: string[], eventHypthesisId: string, associate: boolean) => void;

export interface SignalDetectionContextMenuProps {
  signalDetections: SignalDetection[];
  selectedSds: SignalDetection[];
  sdIdsToShowFk: string[];
  currentOpenEvent: Event;
  changeAssociation: MutationFn<{}, OperationVariables> ;
  associateToNewEvent: MutationFn<{}, OperationVariables>;
  rejectDetectionHypotheses?: MutationFn<{}, OperationVariables>;
  updateDetections?: MutationFn<{}, OperationVariables>;
  measurementMode: MeasurementMode;

  setSelectedSdIds(id: string[]): void;
  setSdIdsToShowFk?(signalDetectionIds: string[]): void;
  setMeasurementModeEntries(entries: Immutable.Map<string, boolean>): void;
}
export class SignalDetectionContextMenu extends React.Component<SignalDetectionContextMenuProps, {}> {
  private constructor(props) {
      super(props);
    }

  /**
   * React component lifecycle.
   */
  public render() {
    const selectedSdIds = this.props.selectedSds.map(sd => sd.id);
    let allRejected = true;
    this.props.selectedSds.forEach(sd => {
      if (!sd.currentHypothesis.rejected) {
        allRejected = false;
      }
    });

    const manualShowMeasurementForSds = [...this.props.measurementMode.entries.entries()]
      .filter(({ 1: v }) => v)
      .map(([k]) => k);

    const manualHideMeasurementForSds = [...this.props.measurementMode.entries.entries()]
      .filter(({ 1: v }) => !v)
      .map(([k]) => k);

    const associatedSignalDetectionHypothesisIds = this.props.currentOpenEvent ?
      this.props.currentOpenEvent.currentEventHypothesis.eventHypothesis.signalDetectionAssociations
        .map(association => association.signalDetectionHypothesis.id) : [];

    const areAllSelectedAccociatedAndAutoShow = this.props.measurementMode.mode === Mode.MEASUREMENT &&
     this.props.selectedSds.every(sd =>
      includes(
        systemConfig.measurementMode.phases,
        findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase) &&
      includes(associatedSignalDetectionHypothesisIds, sd.currentHypothesis.id) &&
      !includes(manualHideMeasurementForSds, sd.id));

    const areAllSelectedSdsMarkedAsMeasurementEntriesToShow =
      this.props.selectedSds.every(sd => includes(manualShowMeasurementForSds, sd.id));

    return (
    <Menu>
      {
        this.props.updateDetections ?
        <MenuItem
          text="Set Phase..."
          label={'Ctrl+s'}
          disabled={selectedSdIds.length === 0 || allRejected}
        >
        {
          setPhaseContextMenu(selectedSdIds, this.props.updateDetections)
        }
        </MenuItem>

        : null
      }
      <MenuItem
        text="Event Association"
        disabled={allRejected}
      >
        {
          this.eventAssociationContextMenu(this.props.selectedSds, this.props.currentOpenEvent)
        }
      </MenuItem>
      {
        this.props.setSdIdsToShowFk ?
        <MenuItem
          text="Show FK"
          disabled={(selectedSdIds.length > 0 && canDisplayFkForSds(this.props.selectedSds))
            || allRejected}
          onClick={this.setSdIdsToShowFk}
        />
        : null
      }
      {
        this.props.rejectDetectionHypotheses ?
          <MenuItem
            text="Reject"
            disabled={selectedSdIds.length === 0 || allRejected}
            onClick={() => this.rejectDetections(selectedSdIds)}
          />
          : null
      }
      {
        <MenuItem
          text="Measure"
        >
          <MenuItem
            text={areAllSelectedSdsMarkedAsMeasurementEntriesToShow ||
              areAllSelectedAccociatedAndAutoShow ? 'Hide A5/2' : 'Show A5/2'}
            onClick={() => {
              // show or hide all selected sds
              const updatedEntires = {};
              selectedSdIds.forEach(id =>
                updatedEntires[id] =
                  !(areAllSelectedSdsMarkedAsMeasurementEntriesToShow || areAllSelectedAccociatedAndAutoShow)
              );
              this.props.setMeasurementModeEntries(this.props.measurementMode.entries.merge(updatedEntires));
            }}
          />
          <MenuItem
            text={'Hide all A5/2'}
            disabled={!(this.props.measurementMode.mode === Mode.MEASUREMENT ||
              this.props.measurementMode.entries.size !== 0)}
            onClick={() => {
              // clear out all the additional measurement mode entries
              let updatedEntries = Immutable.Map<string, boolean>();
              updatedEntries.forEach((value, key) =>
                updatedEntries = this.props.measurementMode.entries.set(key, false));

              if (this.props.measurementMode.mode === Mode.MEASUREMENT) {
                // hide all auto show
                this.props.signalDetections.filter(sd =>
                  includes(associatedSignalDetectionHypothesisIds, sd.currentHypothesis.id) &&
                  includes(
                    systemConfig.measurementMode.phases,
                    findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements).phase))
                .forEach(sd =>
                  updatedEntries = updatedEntries.set(sd.id, false));
              }

              this.props.setMeasurementModeEntries(updatedEntries);
            }}
          />

          {this.props.measurementMode.mode === Mode.MEASUREMENT ?
          (
            <MenuItem
              text={'Show all A5/2 for associated'}
              onClick={() => {
                // clear out all the additional measurement mode entries
                let updatedEntries = Immutable.Map<string, boolean>();
                this.props.measurementMode.entries.forEach((value, key) => {
                  const signalDetection = this.props.signalDetections.find(sd => sd.id === key);
                  if (signalDetection && !value) {
                    if (includes(associatedSignalDetectionHypothesisIds, signalDetection.currentHypothesis.id) &&
                      includes(
                        systemConfig.measurementMode.phases,
                        findPhaseFeatureMeasurementValue(
                          signalDetection.currentHypothesis.featureMeasurements).phase)) {
                      updatedEntries = updatedEntries.delete(key);
                    }
                 }
                });
                this.props.setMeasurementModeEntries(updatedEntries);
              }}
            />
          )
          : undefined
          }
        </MenuItem>
      }
    </Menu>
    );
  }

  /**
   * Displays a blueprint context menu for event association.
   * 
   * @param signalDetections a list of signal detections
   * @returns the evetn association context menu
   */
  private readonly eventAssociationContextMenu = (signalDetections: SignalDetection[],
    event: Event): JSX.Element[] => {
    const sdHypoList = signalDetections.map(sd => sd.currentHypothesis);

    const associatedInList: boolean =
      sdHypoList.filter(sdHyp => this.isAssociatedToCurrentEventHypothesis(sdHyp, event)).length > 0;
    const unassociatedInList: boolean =
      sdHypoList.filter(sdHyp => !this.isAssociatedToCurrentEventHypothesis(sdHyp, event)).length > 0;
    const menuOptions = [];
    menuOptions.push((
      <MenuItem
        text="Associate to new event"
        onClick={() => {
          this.associateToNewEvent(sdHypoList.map(sdHyp => sdHyp.id));
        }}
        key="assocnew"
      />
    ));
    menuOptions.push(
      associatedInList && unassociatedInList ?
      [(
        <MenuItem
          text="Associate to currently open event"
          onClick={() => {
            const sdHypoIdList =
              sdHypoList.filter(sdHyp => !this.isAssociatedToCurrentEventHypothesis(sdHyp, event))
              .map(sdHyp => sdHyp.id);
            this.unassociateOrAssociateSignalDetections(sdHypoIdList, event, true);
          }}
          disabled={this.props.currentOpenEvent === undefined}
          key="assocopen"
        />),
        (
        <MenuItem
          text="Unassociate from currently open event"
          onClick={() => {
            const sdHypoIdList =
            sdHypoList.filter(sdHyp => this.isAssociatedToCurrentEventHypothesis(sdHyp, event))
            .map(sdHyp => sdHyp.id);
            this.unassociateOrAssociateSignalDetections(sdHypoIdList, event, false);
          }}
          key="unassocopen"
        />
      )]
      : associatedInList ?
        [(
            <MenuItem
              text="Unassociate from currently open event"
              onClick={() => {
                const sdHypoIdList = sdHypoList.map(sdHyp => sdHyp.id);
                this.unassociateOrAssociateSignalDetections(sdHypoIdList, event, false);
              }}
              key="unassocopen"
            />
        )]
        : unassociatedInList ?
          [(
              <MenuItem
                text="Associate to currently open event"
                onClick={() => {
                  const sdHypoIdList = sdHypoList.map(sdHyp => sdHyp.id);
                  this.unassociateOrAssociateSignalDetections(sdHypoIdList, event, true);
                }}
                disabled={this.props.currentOpenEvent === undefined}
                key="assocopen"
              />
          )]
          : null);

    return menuOptions;
  }

  /**
   * Returns true if te provied signal detection hypothesis is associated
   * to the provided event; false otherwise.
   * 
   * @param sdHypothesis the signal detection hypothesis to check
   * @param event the event to check association status
   * @returns true if the signal detection hypothesis is associated
   * to the provided event; false otherwise.
   */
  private readonly isAssociatedToCurrentEventHypothesis = (
    sdHypothesis: SignalDetectionHypothesis, event: Event): boolean => {
    if (!event) {
      return false;
    }
    let isAssociated = false;
    event.currentEventHypothesis.eventHypothesis.signalDetectionAssociations.forEach(sdAssoc => {
      if (sdAssoc.signalDetectionHypothesis.id === sdHypothesis.id) {
        isAssociated = true;
      }
    });
    return isAssociated;
  }

  /**
   * Rejects the signal detections for the provided ids.
   * 
   * @param sdIds the signal detection ids to reject
   */
  private readonly rejectDetections = (sdIds: string[]) => {
    const input: SignalDetectionTypes.RejectDetectionsMutationArgs = {
      detectionIds: sdIds
    };
    this.props.rejectDetectionHypotheses({
      variables: input
    })
      .catch(err => window.alert(err));
  }

  /**
   * Returns true if the provided signal detection can be used to generate
   * an FK.
   * 
   * @param signalDetection the signal detection to check if it can be used to 
   * generate an FK.
   * @returns true if the signal detection can be used to generate an FK; false otherwise
   */
  private readonly canGenerateFk =
    (signalDetection: SignalDetectionTypes.SignalDetection): boolean => {
      const fmPhase = findPhaseFeatureMeasurementValue(signalDetection.currentHypothesis.featureMeasurements);
      if (!fmPhase) {
        return false;
      }
      return systemConfig.nonFkSdPhases
        // tslint:disable-next-line:newline-per-chained-call
        .findIndex(phase => phase.toLowerCase() === fmPhase.phase.toString().toLowerCase()) === -1;
  }

  /**
   * Sets or updates the signal detection ids to show FK based on
   * the selected signal detections.
   */
  private readonly setSdIdsToShowFk = () => {
    const sdIdsToShowFk =
      this.props.selectedSds
      .filter(sd => sd && this.canGenerateFk(sd))
      .map(sd => sd.id)
      .filter(id => this.props.sdIdsToShowFk.indexOf(id) < 0);
    if (sdIdsToShowFk.length > 0) {
      this.props.setSdIdsToShowFk([...this.props.sdIdsToShowFk, ...sdIdsToShowFk]);
    }
    this.props.setSelectedSdIds(this.props.selectedSds.map(sd => sd.id));
  }

  /**
   * Unassociate or associate the signal detections for the provided event.
   * 
   * @param signalDetectionHypoIds the signal detection hypothesis ids
   * @param event the event to unassociate or associate too
   * @param associate boolean flag indicating if we are associating or unassociating
   * to the provided event
   */
  private readonly unassociateOrAssociateSignalDetections = (
    signalDetectionHypoIds: string[], event: Event, associate: boolean): void => {
    if (!event) {
      return;
    }
    const input: ChangeSignalDetectionAssociationsMutationArgs = {
      eventHypothesisId: event.currentEventHypothesis.eventHypothesis.id,
      signalDetectionHypoIds,
      associate
    };
    this.props.changeAssociation({
      variables: input
    })
    .catch(err => window.alert(err));
  }
  private readonly associateToNewEvent = (sdHypIds: string[]) => {
    const input = {
      signalDetectionHypoIds: sdHypIds
    };
    this.props.associateToNewEvent({
      variables: input
    })
    .catch(err => window.alert(err));

  }
}

/**
 * Displays a blueprint context menu for selecting a signal detection phase.
 * 
 * @param sdIds string array of signal detection ids
 * @param rePhaser graphql mutation that updates a detection
 * @returns the phase selection context menu
 */
export function setPhaseContextMenu(sdIds: string[], rePhaser: MutationFn<{}, OperationVariables>): JSX.Element {
  return (
    <PhaseSelectionMenu
      sdPhases={systemConfig.defaultSdPhases}
      prioritySdPhases={systemConfig.prioritySdPhases}
      onBlur={phase => { rePhaseDetections(sdIds, phase, rePhaser); }}
      onEnterForPhases={phase => {rePhaseDetections(sdIds, phase, rePhaser); ContextMenu.hide(); }}
      onPhaseClicked={phase => {rePhaseDetections(sdIds, phase, rePhaser); ContextMenu.hide(); }}
    />
  );
}

/**
 * Returns true if the provided signal detections can be used to display an FK.
 * 
 * @param sds a list of signal detections
 * @returns true if the signal detections can be used to display an FK; false otherwise
 */
function canDisplayFkForSds(sds: SignalDetection[]): boolean {
  sds.forEach(sd => {
    const fmPhase = findPhaseFeatureMeasurementValue(sd.currentHypothesis.featureMeasurements);
    if (!fmPhase) {
      return false;
    }
    if (systemConfig.nonFkSdPhases
      // tslint:disable-next-line:newline-per-chained-call
      .findIndex(phase => phase === fmPhase.phase) < 0) {
      return true;
    }
  });
  return false;
}

/**
 * Rephases the provided signal detection ids.
 * 
 * @param phase the signal detection phase to set
 * @param detectionRephaser the mutation for rephasing a signal detection
 */
function rePhaseDetections(sdIds: string[],
  phase: CommonTypes.PhaseType, detectionRephaser: MutationFn<{}, OperationVariables>) {
  const input: SignalDetectionTypes.UpdateDetectionsMutationArgs = {
    detectionIds: sdIds,
    input: {
      phase
    }
  };
  detectionRephaser({
    variables: input
  })
  .catch(err => window.alert(err));
}
