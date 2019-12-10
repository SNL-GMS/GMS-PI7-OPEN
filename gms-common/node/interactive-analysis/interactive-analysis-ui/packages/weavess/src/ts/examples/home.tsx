import { Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import * as React from 'react';
import { Link } from 'react-router-dom';

export class Home extends React.Component<{}, {}> {
  public constructor(props: {}) {
    super(props);
  }

  /* tslint:disable:no-magic-numbers */
  public render() {
    return (
      <div>
        <div>
          <h3>Examples</h3>
          <p>
            If this project is cloned or downloaded, these examples can be
            loaded as files in the browser. Otherwise, peruse the source code
            for ideas on how to use WEAVESS in various ways.
          </p>
          <h3>Documentation</h3>
          <p>
            Currently, no formal API Docs exist. Hopefully they will be coming
            soon. For now, these examples and the source code are your only
            hope.
          </p>
        </div>
        <br />
        <div>
          <h4>
            <Icon icon={IconNames.CHART} />
            <Link to="/WeavessExample"> Weavess Example</Link>
          </h4>
          <p>Basic introduction to using Weavess. Start here first.</p>
          <br />
          <h4>
            <Icon icon={IconNames.MULTI_SELECT} />
            <Link to="/MultipleDisplaysExample"> Multiple Displays Example</Link>
          </h4>
          <p>
            Multiple Weavess displays can be displayed anywhere on the screen
            and operate completely independent of each other
          </p>
          <br />
          <h4>
            <Icon icon={IconNames.TIMELINE_EVENTS} />
            <Link to="/EventsExample"> Events Example</Link>
          </h4>
          <p>
            Register callbacks for various events triggered by the Weavess
            display
          </p>
          <br />
          <h4>
            <Icon icon={IconNames.RECORD} />
            <Link to="/RecordSectionExample"> Record Section Example</Link>
          </h4>
          <p>Record Section-style waveform display</p>
          <br />
        </div>
      </div>
    );
  }
}
