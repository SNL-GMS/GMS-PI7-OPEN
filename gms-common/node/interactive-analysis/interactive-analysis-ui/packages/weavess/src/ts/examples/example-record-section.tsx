import { Classes, Colors } from '@blueprintjs/core';
import * as React from 'react';
import { WeavessRecordSection, WeavessUtils } from '../weavess';

export class RecordSectionExample extends React.Component<{}, {}> {
  public recordSection: WeavessRecordSection;

  public constructor(props: {}) {
    super(props);
  }

  /* tslint:disable:no-magic-numbers */
  public render() {
    return (
      <div
        className={Classes.DARK}
        style={{
          height: '80%',
          width: '100%',
          padding: '0.5rem',
          color: Colors.GRAY4,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}
      >
        <div
          style={{
            height: '100%',
            width: '100%',
            display: 'flex',
            flexDirection: 'column'
          }}
        >
          <div
            style={{
              flex: '1 1 auto',
              position: 'relative'
            }}
          >
            <div
              style={{
                position: 'absolute',
                top: '0px',
                bottom: '0px',
                left: '0px',
                right: '0px'
              }}
            >
              <WeavessRecordSection
                ref={ref => {
                  if (ref) {
                    this.recordSection = ref;
                  }
                }}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }

  public componentDidMount() {
    if (this.recordSection) {
      this.recordSection.update(true);

      this.recordSection.addWaveformArray(
        [
          {
            channel: 'close',
            // tslint:disable-next-line:newline-per-chained-call
            data: Array.apply(null, Array(100000)).map(function() {
              return Math.round(WeavessUtils.RandomNumber.getSecureRandomNumber() * 100);
            }),
            distance: 1079184.644731988, // in meters
            phase: 'P',
            // tslint:disable-next-line:number-literal-format
            startTime: new Date('2016-01-01T00:00:00Z').valueOf() / 1000.0,
            sampleRate: 40,
            signalDetection: [
              {
                time: new Date('2016-01-01T00:02:30Z'),
                id: 0,
                color: '#f00',
                label: 's'
              }
            ]
          },
          {
            channel: 'medium',
            // tslint:disable-next-line:newline-per-chained-call
            data: Array.apply(null, Array(100000)).map(function() {
              return Math.round(WeavessUtils.RandomNumber.getSecureRandomNumber() * 5000);
            }),
            distance: 3379184.644731988,
            phase: 'P',
            // tslint:disable-next-line:number-literal-format
            startTime: new Date('2016-01-01T00:00:00Z').valueOf() / 1000.0,
            sampleRate: 40,
            signalDetection: [
              {
                time: new Date('2016-01-01T00:05:00Z'),
                id: 1,
                color: '#f00',
                label: ''
              }
            ]
          },
          {
            channel: 'far',
            // tslint:disable-next-line:newline-per-chained-call
            data: Array.apply(null, Array(100000)).map(function() {
              return Math.round(WeavessUtils.RandomNumber.getSecureRandomNumber() * 5000);
            }),
            distance: 3914023.8687042934,
            phase: 'P',
            // tslint:disable-next-line:number-literal-format
            startTime: new Date('2016-01-01T00:00:00Z').valueOf() / 1000.0,
            sampleRate: 40,
            signalDetection: [
              {
                time: new Date('2016-01-01T00:10:00'),
                id: 2,
                color: '#f00',
                label: ''
              }
            ]
          }
        ],
        false
      );

      this.recordSection.update(false);
      this.recordSection.forceUpdate();
    }
  }
}
