import { Classes, Colors } from '@blueprintjs/core';
import * as React from 'react';
import { Weavess, WeavessTypes, WeavessUtils } from '../weavess';

export class EventsExample extends React.Component<{}, {}> {
  public weavess: Weavess;

  public constructor(props: {}) {
    super(props);
  }

  /* tslint:disable:no-magic-numbers */
  public render() {
    const waveforms: WeavessTypes.Station[] = [];

    const startTimeSecs = new Date().valueOf() / 1000;
    const endTimeSecs = startTimeSecs + 1800; // + 30 minutes

    for (let i = 0; i < 25; ++i) {
        const waveform = WeavessUtils.Waveform.createDummyWaveform(
            startTimeSecs, endTimeSecs, 20, WeavessUtils.RandomNumber.getSecureRandomNumber() * 2,
            WeavessUtils.RandomNumber.getSecureRandomNumber() * 0.25);
        waveform.id = `Channel${i}`;
        waveform.name = `Channel ${i}`;
        waveforms.push(waveform);
    }

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
          alignItems: 'center',
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
            <Weavess
              ref={ref => {
                if (ref) {
                  this.weavess = ref;
                }
              }}
              mode={WeavessTypes.Mode.DEFAULT}
              stations={waveforms}
              startTimeSecs={startTimeSecs}
              endTimeSecs={endTimeSecs}
              events={{
                stationEvents: {
                  defaultChannelEvents: {
                    labelEvents: {},
                    events: {
                      onSignalDetectionClick: (e: React.MouseEvent<HTMLDivElement>, sdId: string) => {
                        // tslint:disable-next-line:no-console
                        console.log('signal detection deleted!');
                      },
                      onSignalDetectionDragEnd: (sdId: string, timeSecs: number) => {
                          // tslint:disable-next-line:no-console
                          console.log('signal detection modified!');
                      },
                    }
                  },
                  nonDefaultChannelEvents: {
                    labelEvents: {},
                    events: {}
                  }
                }
              }}
              flex={false}
            />
          </div>
        </div>
      </div>
      </div>
    );
  }
  // tslint:disable-next-line:max-file-line-count
}
