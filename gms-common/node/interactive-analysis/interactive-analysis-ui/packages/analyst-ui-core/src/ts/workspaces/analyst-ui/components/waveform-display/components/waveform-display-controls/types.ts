import * as Gl from '@gms/golden-layout';
import { QcMaskDisplayFilters } from '~analyst-ui/config';
import { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';
import { CommonTypes } from '~graphql/';
import { MeasurementMode, Mode, WaveformSortType } from '~state/analyst-workspace/types';
import { AlignWaveformsOn, PanType } from '../../types';
import { WaveformClientState } from '../../waveform-client/types';

/**
 * Waveform Display Controls Props
 */
export interface WaveformDisplayControlsProps {
  createSignalDetectionPhase: CommonTypes.PhaseType;
  currentSortType: WaveformSortType;
  currentOpenEventId: string;
  analystNumberOfWaveforms: number;
  showPredictedPhases: boolean;
  maskDisplayFilters: QcMaskDisplayFilters;
  alignwaveFormsOn: AlignWaveformsOn;
  phaseToAlignOn: CommonTypes.PhaseType | undefined;
  alignablePhases: CommonTypes.PhaseType[] | undefined;
  glContainer: Gl.Container;
  isMeasureWindowVisible: boolean;
  measurementMode: MeasurementMode;
  setMode(mode: Mode): void;
  setCreateSignalDetectionPhase(phase: CommonTypes.PhaseType): void;
  setSelectedSortType(sortType: WaveformSortType): void;
  setAnalystNumberOfWaveforms(value: number, valueAsString?: string): void;
  setMaskDisplayFilters(
    key: string,
    maskDisplayFilter: MaskDisplayFilter
  ): void;
  setWaveformAlignment(alignOn: AlignWaveformsOn, phase: CommonTypes.PhaseType): void;
  toggleMeasureWindow(): void;
  setShowPredictedPhases(showPredicted: boolean): void;
  pan(panDirection: PanType): void;
  onKeyPress(
    e: React.KeyboardEvent<HTMLDivElement>,
    clientX?: number,
    clientY?: number,
    channelId?: string,
    timeSecs?: number
  ): void;
}

export interface WaveformDisplayControlsState {
  hasMounted: boolean;
  waveformState: WaveformClientState;
}
