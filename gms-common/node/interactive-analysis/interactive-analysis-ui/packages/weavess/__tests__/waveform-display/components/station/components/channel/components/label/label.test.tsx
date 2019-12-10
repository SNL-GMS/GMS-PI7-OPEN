import { ReactWrapper, ShallowWrapper } from 'enzyme';
import { date, finance, hacker, internet, random, seed } from 'faker';
import * as React from 'react';
// tslint:disable-next-line:max-line-length
import { Label } from '../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/label/label';
// tslint:disable-next-line:max-line-length
import { LabelProps } from '../../../../../../../../src/ts/components/waveform-display/components/station/components/channel/components/label/types';
import * as Entities from '../../../../../../../../src/ts/entities';

// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Enzyme = require('enzyme');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const Adapter = require('enzyme-adapter-react-16');

const fakerDummyWaveformSeed = 123;
seed(fakerDummyWaveformSeed);

let labelProps: LabelProps;
let labelContainer: ReactWrapper;
let shallowWrapper: ShallowWrapper;
let wrapper: ReactWrapper;

export function generateConfiguration(): Entities.Configuration {
  return {
    shouldRenderWaveforms: true,
    shouldRenderSpectrograms: true,
    defaultChannelHeightPx: random.arrayElement([
      undefined,
      random.number({ min: 1, max: 100 })
    ]),
    labelWidthPx: random.arrayElement([
      undefined,
      random.number({ min: 1, max: 100 })
    ]),
    hotKeys: generateHotKeyConfiguration(),
    defaultChannel: generateChannelConfiguration(),
    nonDefaultChannel: generateChannelConfiguration()
  };
}

export function generateHotKeyConfiguration() {
  return {
    amplitudeScale: random.arrayElement([undefined, random.alphaNumeric(1)]),
    amplitudeScaleSingleReset: random.arrayElement([
      undefined,
      random.alphaNumeric(1)
    ]),
    amplitudeScaleReset: random.arrayElement([
      undefined,
      random.alphaNumeric(1)
    ]),
    maskCreate: random.arrayElement([undefined, random.alphaNumeric(1)])
  };
}

export function generateChannelConfiguration() {
  return {
    disableMeasureWindow: random.arrayElement([undefined, random.boolean()]),
    disableSignalDetectionModification: random.arrayElement([
      undefined,
      random.boolean()
    ]),
    disableMaskModification: random.arrayElement([undefined, random.boolean()])
  };
}

export function generateChannelConfig(): Entities.Channel {
  return {
    id: random.uuid(),
    name: finance.currencyCode(),
    channelType: undefined,
    waveform: {
      channelSegmentId: random.uuid(),
      channelSegments: generateChannelSegments(),
      masks: undefined,
      signalDetections: undefined,
      theoreticalPhaseWindows: undefined
    }
  };
}

export function generateChannelSegments(): Map<
  string,
  Entities.ChannelSegment
> {
  const channelSegmentsMap: Map<string, Entities.ChannelSegment> = new Map();

  for (let i = 0; i < random.number({ min: 1, max: 3 }); i++) {
    channelSegmentsMap.set(random.uuid(), generateChannelSegment());
  }

  return channelSegmentsMap;
}

export function generateChannelSegment(): Entities.ChannelSegment {
  return {
    description: random.arrayElement([undefined, hacker.phrase()]),
    descriptionLabelColor: random.arrayElement([undefined, internet.color()]),

    dataSegments: generateDataSegments()
  };
}

export function generateDisplayType(): Entities.DisplayType[] {
  const displayTypes: Entities.DisplayType[] = [];
  for (let i = 0; i < random.number({ min: 1, max: 3 }); i++) {
    displayTypes.push(
      Entities.DisplayType[random.objectElement(Entities.DisplayType)]
    );
  }
  return displayTypes;
}

export function generateDataSegments(): Entities.DataSegment[] {
  const dataSegments: Entities.DataSegment[] = [];
  for (let i = 0; i < random.number({ min: 10, max: 100 }); i++) {
    dataSegments.push(generateDataSegment());
  }
  return dataSegments;
}

export function generateDataSegment(): Entities.DataSegment {
  const data: number[] = [];
  for (let i = 0; i < random.number({ min: 10, max: 100 }); i++) {
    data.push(random.number({ min: -200, max: 200, precision: 3 }));
  }
  return {
    startTimeSecs: date.recent()
      .getUTCMilliseconds(),
    data,
    sampleRate: random.number({ min: 20, max: 100 }),
    color: random.arrayElement([undefined, internet.color()]),
    displayType: random.arrayElement([undefined, generateDisplayType()]),
    pointSize: random.arrayElement([
      undefined,
      random.number({ min: 1, max: 100 })
    ]),
  };
}

export function generateLabelProps(): LabelProps {
  return {
    configuration: generateConfiguration(),
    channel: generateChannelConfig(),
    isDefaultChannel: false,
    isExpandable: false,
    expanded: false,
    yAxisBounds: [
      {
        heightInPercentage: 50,
        minAmplitude: random.number({ min: 10, max: 50 }),
        maxAmplitude: random.number({ min: 150, max: 200 })
      }
    ],
    // selectedChannels: undefined,
    selections: {
      channels: []
    },
    showMaskIndicator: false,
    distance: random.number({ min: 50, max: 1000 }),
    // assumption for this is that element name and element value are identical (case-sensitive)
    distanceUnits:
      Entities.DistanceUnits[random.objectElement(Entities.DistanceUnits)],
    events: undefined,
    toggleExpansion: undefined
  };
}

export function fakeChannelLabelClick(
  e: React.MouseEvent<HTMLDivElement>,
  channelId: string
): void {
  if (labelProps && labelProps.selections.channels) {
    labelProps.selections.channels.push(channelId);
  }
  // labelContainer.get(0).props.style.textShadow = '0px 1px 15px';
  wrapper.setProps({ style: { textShadow: '0px 1px 15px' } });
}

export function generateUpdatedLabelEvents() {
  return {
    onChannelExpanded: undefined,
    onChannelCollapsed: undefined,
    onChannelLabelClick: fakeChannelLabelClick
  };
}

describe('Label Tests', () => {
  beforeEach(() => {
    Enzyme.configure({ adapter: new Adapter() });
  });

  test('label displays proper distance units', () => {
    labelProps = generateLabelProps();

    labelProps.distanceUnits = Entities.DistanceUnits.km;
    // tslint:disable-next-line:no-magic-numbers
    labelProps.distance = 555.557;

    const expectedValue = '555.56 km ';

    shallowWrapper = Enzyme.shallow(<Label {...labelProps} />);

    // css selector for p child of div
    const distanceLabel = shallowWrapper.find(
      'div.label-container-content-label p'
    );

    // sanity check for <p>
    expect(distanceLabel)
      .toHaveLength(1);
    expect(distanceLabel.text())
      .toMatch(expectedValue);
  });

  test('mount should be able to offer click response', (done: jest.DoneCallback) => {
    labelProps = generateLabelProps();

    labelProps.distanceUnits = Entities.DistanceUnits.km;
    // tslint:disable-next-line:no-magic-numbers
    labelProps.distance = 555.557;

    labelProps.selections.channels = [];
    // labelProps.selectedChannels = ['730b69ba-2721-48b8-aa4d-7bb95b65a553'];
    labelProps.events = generateUpdatedLabelEvents();

    wrapper = Enzyme.mount(<Label {...labelProps} />);

    setImmediate(() => {
      wrapper.update();

      labelContainer = wrapper.find('div.label-container-content-label');

      let containerStyle = labelContainer.get(0).props.style;
      expect(containerStyle)
        .toHaveProperty('textShadow', 'initial');

      // tslint:disable-next-line: no-unnecessary-type-assertion
      expect((wrapper.prop('selections') as any).channels)
        .toHaveLength(0);

      labelContainer.first()
        .simulate('click');

      wrapper.update();

      labelContainer = wrapper.find('div.label-container-content-label');

      // tslint:disable-next-line: no-unnecessary-type-assertion
      expect((wrapper.prop('selections') as any).channels)
        .toHaveLength(1);

      containerStyle = labelContainer.get(0).props.style;
      expect(containerStyle)
        .toHaveProperty('textShadow', '0px 1px 15px');

      done();
    });
  });
});
