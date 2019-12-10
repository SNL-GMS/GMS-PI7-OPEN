import * as React from 'react';
import { WeavessExample } from './example-weavess';

export class MultipleDisplaysExample extends React.Component<{}, {}> {
  public constructor(props: {}) {
    super(props);
  }

  /* tslint:disable:no-magic-numbers */
  public render() {
    return (
      <div
        style={{
          height: '80%',
          display: 'flex',
          justifyContent: 'space-around',
          flexDirection: 'column',
          alignItems: 'center'
        }}
      >
        <WeavessExample key={1} showExampleControls={false} />
        <WeavessExample key={2} showExampleControls={false} />
      </div>
    );
  }
}
