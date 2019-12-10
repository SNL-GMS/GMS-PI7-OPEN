import { RpcProvider } from 'worker-rpc';

import { createPositionBuffer } from './create-position-buffer';
import { createRecordSectionPositionBuffer } from './create-record-section-line';
import { WorkerOperations } from './operations';

// @ts-ignore
const rpcProvider = new RpcProvider(
  // @ts-ignore
  // tslint:disable-next-line:no-unnecessary-callback-wrapper
  (message, transfer) => postMessage(message, transfer),
);
onmessage = e => rpcProvider.dispatch(e.data);

rpcProvider.registerRpcHandler(
  WorkerOperations.CREATE_POSITION_BUFFER,
  createPositionBuffer);

rpcProvider.registerRpcHandler(
  WorkerOperations.CREATE_RECORD_SECTION_POSITION_BUFFER,
  createRecordSectionPositionBuffer);
