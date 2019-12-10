import {
  Intent,
  Spinner
} from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import { Toolbar, ToolbarTypes } from '@gms/ui-core-components';
import { defer, intersection } from 'lodash';
import * as React from 'react';
import { AlignmentMenu, PhaseSelectionMenu, QcMaskFilter } from '~analyst-ui/common/dialogs';
import { analystUiConfig } from '~analyst-ui/config';
import { CommonTypes } from '~graphql/';
import { Mode, WaveformSortType } from '~state/analyst-workspace/types';
import { AlignWaveformsOn, PanType } from '../../types';
import { DEFAULT_INITIAL_WAVEFORM_CLIENT_STATE } from '../../waveform-client/constants';
import { WaveformDisplayControlsProps, WaveformDisplayControlsState } from './types';

const GL_CONTAINER_PADDING_PX = 16;
/**
 *  Waveform Display Controls Component
 */
export class WaveformDisplayControls extends
  React.Component<WaveformDisplayControlsProps, WaveformDisplayControlsState> {

  /**
   * The rank of the alignment popover
   */
  private RANK_OF_ALIGNMENT_POPOVER: number = 0;

  /**
   * handle to the alignement button
   */
  private toolbarRef: Toolbar;

  public constructor(props: WaveformDisplayControlsProps) {
    super(props);
    this.state = {
      hasMounted: false,
      waveformState: {
        ...DEFAULT_INITIAL_WAVEFORM_CLIENT_STATE
      }
    };
  }

  public componentDidMount() {
    this.setState({ hasMounted: true });
  }

  /**
   * React component lifecycle
   */
  public render() {
    const defaultSdPhasesList = analystUiConfig.systemConfig.defaultSdPhases;
    const prioritySdPhasesList = analystUiConfig.systemConfig.prioritySdPhases;

    let rank = 1;

    const leftToolbarLeftItems: ToolbarTypes.ToolbarItem[] = [];
    leftToolbarLeftItems.push({
      label: 'Mode',
      tooltip: 'Set the display mode',
      type: ToolbarTypes.ToolbarItemType.Dropdown,
      value: this.props.measurementMode.mode,
      disabled: this.props.currentOpenEventId === null
        || this.props.currentOpenEventId === undefined || this.props.currentOpenEventId === '',
      rank: rank++,
      onChange: value => { this.props.setMode(value); },
      dropdownOptions: Mode,
      widthPx: 130
    });

    const rightToolbarItemDefs: ToolbarTypes.ToolbarItem [] = [];
    const phaseSelectionDropDown = (
      <PhaseSelectionMenu
        phase={this.props.createSignalDetectionPhase}
        sdPhases={defaultSdPhasesList}
        prioritySdPhases={prioritySdPhasesList}
        onBlur={() => {return; }}
        onEnterForPhases={phase => {
          this.hideToolbarPopover();
          this.props.setCreateSignalDetectionPhase(phase);
        }}
        onPhaseClicked={phase => {
          this.hideToolbarPopover();
          this.props.setCreateSignalDetectionPhase(phase);  }}
      />
    );

    rightToolbarItemDefs.push({
      label: this.props.createSignalDetectionPhase,
      menuLabel: 'Default Phase',
      tooltip: 'Set default phase of new signal detections',
      type: ToolbarTypes.ToolbarItemType.Popover,
      rank: rank++,
      popoverContent: phaseSelectionDropDown,
      widthPx: 88,
      onChange: () => {return; }});

    rightToolbarItemDefs.push({
      label: 'Visible Waveforms',
      labelRight: 'per screen',
      tooltip: 'Sets the number of visible waveforms per screen',
      type: ToolbarTypes.ToolbarItemType.NumericInput,
      rank: rank++,
      onChange: value => this.props.setAnalystNumberOfWaveforms(value),
      value: this.props.analystNumberOfWaveforms,
      minMax: {
        min: 1,
        max: 100
      }
    });

    const alignmentDropdown = (
      <AlignmentMenu
        alignedOn={this.props.alignwaveFormsOn}
        sdPhases={this.props.alignablePhases ? this.alignablePhasesUnion(defaultSdPhasesList) : defaultSdPhasesList}
        phaseAlignedOn={this.props.phaseToAlignOn}
        prioritySdPhases={this.props.alignablePhases ?
            this.alignablePhasesUnion(prioritySdPhasesList) : prioritySdPhasesList}
        onSubmit={(alignedOn: AlignWaveformsOn, sdPhase?: CommonTypes.PhaseType) => {
          this.hideToolbarPopover();
          this.props.setWaveformAlignment(alignedOn, sdPhase);
        }}
      />
    );

    this.RANK_OF_ALIGNMENT_POPOVER = rank++;
    const alignmentLabel = this.props.alignwaveFormsOn === AlignWaveformsOn.TIME ?
      'Time'
      : `${this.props.alignwaveFormsOn} ${this.props.phaseToAlignOn}`;
    rightToolbarItemDefs.push({
      label: alignmentLabel,
      tooltip: 'Align waveforms to time or phase',
      type: ToolbarTypes.ToolbarItemType.Popover,
      menuLabel: 'Alignment',
      disabled: this.props.currentOpenEventId === null
                || this.props.currentOpenEventId === undefined || this.props.currentOpenEventId === '',
      rank: this.RANK_OF_ALIGNMENT_POPOVER,
      popoverContent: alignmentDropdown,
      widthPx: 154,
      onChange: () => {
        return;
      }
    });

    rightToolbarItemDefs.push({
      label: 'Station Sort',
      tooltip: 'Set the sort order of stations',
      type: ToolbarTypes.ToolbarItemType.Dropdown,
      value: this.props.currentSortType,
      disabled: this.props.alignwaveFormsOn !== AlignWaveformsOn.TIME || !this.props.currentOpenEventId,
      rank: rank++,
      onChange: value => {
          this.props.setSelectedSortType(value);
      },
      dropdownOptions: WaveformSortType,
      widthPx: 130
    });

    rightToolbarItemDefs.push({
      label: 'Predicted Phases',
      tooltip: 'Show/Hide predicted phases',
      rank: rank++,
      onChange: val => this.props.setShowPredictedPhases(val),
      type: ToolbarTypes.ToolbarItemType.Switch,
      value: this.props.showPredictedPhases,
      menuLabel: this.props.showPredictedPhases ? 'Hide Predicted Phase' : 'Show Predicted Phases'
    });

    rightToolbarItemDefs.push({
      label: 'QC Masks',
      tooltip: 'Show/Hide categories of QC masks',
      rank: rank++,
      widthPx: 110,
      onChange: () => {return; },
      type: ToolbarTypes.ToolbarItemType.Popover,
      popoverContent: (
      <QcMaskFilter
        maskDisplayFilters={this.props.maskDisplayFilters}
        setMaskDisplayFilters={this.props.setMaskDisplayFilters}
      />
      )
    });

    rightToolbarItemDefs.push({
      label: 'Measure Window',
      tooltip: 'Show/Hide Measure Window',
      type: ToolbarTypes.ToolbarItemType.Switch,
      value: this.props.isMeasureWindowVisible,
      rank: rank++,
      onChange: e => this.props.toggleMeasureWindow(),
      menuLabel: this.props.isMeasureWindowVisible ? 'Hide Measure Window' : 'Show Measure Window'
    });

    rightToolbarItemDefs.push({
      buttons: [{
          label: 'Pan Left',
          tooltip: 'Pan waveforms to the left',
          type: ToolbarTypes.ToolbarItemType.Button,
          rank: rank++,
          icon: IconNames.ARROW_LEFT,
          onlyShowIcon: true,
          onChange: async e => this.props.pan(PanType.Left)
        },
        {
          label: 'Pan Right',
          tooltip: 'Pan waveforms to the Right',
          type: ToolbarTypes.ToolbarItemType.Button,
          rank: rank++,
          icon: IconNames.ARROW_RIGHT,
          onlyShowIcon: true,
          onChange: async e => this.props.pan(PanType.Right)
      }],
      label: 'Pan',
      tooltip: '',
      type: ToolbarTypes.ToolbarItemType.ButtonGroup,
      rank,
      onChange: () => {return; },
    });

    return (
      <div
        className={'waveform-display-control-pannel'}
        onKeyDown={e => {
          this.props.onKeyPress(e);
        }}
        onMouseEnter={e => {
          e.currentTarget.focus();
        }}
      >
        <div
          className={'waveform-display-control-pannel__status'}
          onKeyDown={e => {
            this.props.onKeyPress(e);
          }}
          onMouseEnter={e => {
            e.currentTarget.focus();
          }}
        >
          <div
            className="waveform-display-controls-status"
          >
          {this.state.waveformState.isLoading ? (
            <Spinner
              intent={Intent.PRIMARY}
              small={true}
              value={this.state.waveformState.percent}
            />
          ) : (
              undefined
            )}
          {this.state.waveformState.isLoading ? (
            <span>
              {this.state.waveformState.description}
            </span>
          ) : (
              undefined
            )}
          </div>
        </div>
        <Toolbar
          itemsLeft={leftToolbarLeftItems}
          items={rightToolbarItemDefs}
          ref={ref => {if (ref) { this.toolbarRef = ref; } }}
          toolbarWidthPx={this.props.glContainer ? this.props.glContainer.width - GL_CONTAINER_PADDING_PX : 0}
        />
      </div>
    );
  }

  /**
   * Toggles the alignment dropdown
   */
  public readonly toggleAlignmentDropdown = () => {
    if (this.toolbarRef) {
      this.toolbarRef.togglePopover(this.RANK_OF_ALIGNMENT_POPOVER);
    }
  }

  /**
   * Hides the toolbar popover.
   */
  private readonly hideToolbarPopover = () => {
    if (this.toolbarRef) {
      document.addEventListener('dblclick', this.preventDoubleClick, {capture: true});
      this.toolbarRef.hidePopup();
      defer(() => document.removeEventListener('dblclick', this.preventDoubleClick, {capture: true}));
    }
  }

  /**
   * Prevents a double click event.
   */
  private readonly preventDoubleClick = (event: Event) => {
    event.preventDefault();
    event.stopPropagation();
  }

  /**
   * Returns the alignable phases.
   */
  private readonly alignablePhasesUnion = (defaultList: CommonTypes.PhaseType[]) => {
    const unionResult = intersection(this.props.alignablePhases, defaultList);
    return unionResult;
  }
}
