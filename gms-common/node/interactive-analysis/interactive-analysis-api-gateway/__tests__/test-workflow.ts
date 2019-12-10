// tslint:disable:max-line-length
import { graphql } from 'graphql';
import { schema } from '../src/ts/schema';

// No need for pre-test setup yet...
// beforeEach(async () => await setupTest());

// ---- Query test cases ----

// Test case - query by ID for Analysts
it('Analyst query by ID field should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query Analyst(id: "1"){
    analysts{
      userName
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - basic content query for ProcessingStages
it('ProcessingStage content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query stages {
    stages {
      id
      name
      stageType
      activities {
        id
        name
        activityType
      }
      intervals {
        id
        startTime
        endTime
        status
        eventCount
        completedBy{
          userName
        }
        activityIntervals {
          id
          activeAnalysts {
            userName
          }
          activity {
            name
          }
          status
          eventCount
          timeStarted
          completedBy{
            userName
          }
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - id-based query for ProcessingStage
it('ProcessingStage query by ID field should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query stage {
    stage(id: "1") {
      id
      name
      stageType
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - query for ProcessingStages with nested time range filter on intervals
it('ProcessingStage query with nested interval filter should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query stageWithIntervals{
    stage(id: "1"){
      id
      name
      intervals(timeRange: {startTime: 1276920001000, endTime: 1276948800000}){
        id
        startTime
        endTime
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - query for ProcessingIntervals by time range
it('ProcessingInterval query by time range should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query intervalsInRange{
    intervalsInRange(timeRange: { startTime: 1276920001000, endTime: 1276948800000 }){
      id
      startTime
      endTime
      stageIntervals{
        id
        stage{
          name
        }
      }
    }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - id-based query for ProcessingInterval
it('ProcessingInterval query by ID field should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query interval{
    interval(id: "1"){
      id
      startTime
      endTime
      stageIntervals{
        id
        stage{
          name
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - query for ProcessingStagesIntervals by time range
it('ProcessingStageInterval query by time range should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query stageIntervalsInRange{
    stageIntervalsInRange(timeRange: { startTime: 1276920001000, endTime: 1276948800000 }){
      id
      startTime
      endTime
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - content query for ProcessingStagesIntervals
it('ProcessingStageInterval content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query stageIntervals{
    stageIntervals{
      id
      stage{
        name
      }
      interval{
        id
        startTime
        endTime
      }
      startTime
      endTime
      eventCount
      status
      activityIntervals{
        id
        timeStarted
        eventCount
        status
        activeAnalysts{
          userName
        }
        completedBy{
          userName
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - query by ID for ProcessingStagesIntervals
it('ProcessingStageInterval query by ID field should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query stageInterval{
    stageInterval(id: "86785827-8cc9-4d78-af31-1a7ac8ea7b64"){
      id
      startTime
      endTime
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - basic content query for ProcessingActivities
it('ProcessingActivity content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query activities {
    activities{
      id
      name
      activityType
      stage{
        id
        name
        stageType
      }
      intervals{
        id
        timeStarted
        eventCount
        status
        completedBy{
          userName
        }
        stageInterval{
          id
          startTime
          endTime
          status
          completedBy{
            userName
          }
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - id-based query for ProcessingActivity
it('ProcessingActivity query by ID field should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query activity {
    activity(id: "1"){
      id
      name
      activityType
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - content query for ProcessingActivityIntervals
it('ProcessingActivityInterval content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query activityIntervals{
    activityIntervals{
      id
      timeStarted
      eventCount
      status
      activity{
        id
      }
      activeAnalysts{
        userName
      }
      stageInterval{
        id
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - query by ID for ProcessingStagesIntervals
it('ProcessingStageInterval query by ID field should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query activityInterval{
    activityInterval(id: b239612f-982e-4426-9baa-1ee0bc381891"){
      id
      timeStarted
      eventCount
      status
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// ---- Mutation test cases ----

// Test case - open a ProcessingStageInterval by marking status InProgress (mutation)
it('Opening a ProcessingStageInterval should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation openStageWIntervals{
    markStageInterval(stageIntervalId: "24a1dd29-6ada-47a5-af1f-a0883d570a5c", input: { status: InProgress, analystUserName: "Ryan"}){
      status
      activityIntervals{
        status
        activeAnalysts{
          userName
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Mark a ProcessingStageInterval complete (mutation)
it('Marking a ProcessingStageInterval Complete should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation completeStageInterval{
    markStageInterval(stageIntervalId: "d2e1af21-5554-4bc7-b089-29bf92c2c95e", input: {status: Complete, analystUserName: "Tim"}){
      id
      status
      completedBy{
        userName
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - open a ProcessingActivityInterval by marking status InProgress (mutation)
it('Opening a ProcessingActivityInterval should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation openActivityInterval{
    markActivityInterval(activityIntervalId: "b239612f-982e-4426-9baa-1ee0bc381891", input: {status: InProgress, analystUserName: "Tim"}){
      activityInterval {
        id
        status
        activeAnalysts{
          userName
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Mark a ProcessingActivityInterval complete (mutation)
it('Marking a ProcessingActivityInterval Complete should match snapshot', async () => {
  // language=GraphQL
  const mutation = `
  mutation completeActivityInterval{
    markActivityInterval(activityIntervalId: "fd1520fe-7721-4c52-aab5-ab401d9f9fd4", input: {status: Complete, analystUserName: "Tim"}){
      activityInterval {
        id
        status
        completedBy{
          userName
        }
        activeAnalysts{
          userName
        }
      }
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, mutation, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});

// Test case - Check for error when setting stage status to NotStarted
it('Changing stage status to NotComplete should result in an error', async () => {
  // language=GraphQL
  const mutation = `
  mutation m{
    markStageInterval(stageIntervalId: "79f0afe0-917f-4fa1-8187-f8c0b03dd98c", input: {status: NotComplete, analystUserName: "Tim"}){
      id
      status
    }
  }
  `;

  // Execute the GraphQL query
  const result = await graphql(schema, mutation, {});
  const { errors } = result;

  expect(errors).toBeDefined();
});

// Test case - Check for error when changing stage status from NotStarted to Complete
it('Changing stage status from NotStarted to Complete should result in an error', async () => {
  // language=GraphQL
  const mutation = `
  mutation m{
    markStageInterval(stageIntervalId: "79f0afe0-917f-4fa1-8187-f8c0b03dd98c", input: {status: Complete, analystUserName: "Tim"}){
      id
      status
    }
  }
  `;

  // Execute the GraphQL query
  const result = await graphql(schema, mutation, {});
  const { errors } = result;

  expect(errors).toBeDefined();
});

// Test case - Check for error when changing activity status from to NotStarted
it('Changing activity status from NotStarted to Complete should result in an error', async () => {
  // language=GraphQL
  const mutation = `
  mutation m{
    markActivityInterval(activityIntervalId: "79f0afe0-917f-4fa1-8187-f8c0b03dd98c", input: { status: Complete, analystUserName: "Ryan"}){
      id
      status
    }
  }
  `;

  // Execute the GraphQL query
  const result = await graphql(schema, mutation, {});
  const { errors } = result;

  expect(errors).toBeDefined();
});

// Test case - Check for error when changing stage to complete with activity not yet complete
it('Changing stage status to Complete with activity not Complete should result in an error', async () => {
  // language=GraphQL
  const mutation = `
  mutation m{
    markStageInterval(stageIntervalId: "e044db89-f6b9-4269-b986-635ebe26b0fe", input: {status: Complete, analystUserName: "Tim"}){
      id
      status
    }
  }
  `;

  // Execute the GraphQL query
  const result = await graphql(schema, mutation, {});
  const { errors } = result;

  expect(errors).toBeDefined();
});

// Test case - basic content query for Analysts
it('Analyst content query should match snapshot', async () => {
  // language=GraphQL
  const query = `
  query Analysts{
    analysts{
      userName
    }
  }
  `;

  // Execute the GraphQL query
  const rootValue = {};
  const result = await graphql(schema, query, rootValue);
  const { data } = result;

  // Compare response to snapshot
  expect(data).toMatchSnapshot();
});
