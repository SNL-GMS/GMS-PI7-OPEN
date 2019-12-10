import { cond, identity, stubTrue } from 'lodash';
import * as transit from 'transit-js';

export const transitToObj: (item: any) => any = cond([
  [
    transit.isMap,
    item =>
      Object.assign(
        {},
        ...Array.from(item.entries())
          .map(([k, v]) => ({
            [k.name()]: transitToObj(v)
          }))
      )
  ],
  // tslint:disable-next-line:no-unbound-method
  [Array.isArray, (item: [any]) => item.map(transitToObj)],
  [transit.isKeyword, item => item.name()],
  [stubTrue, identity]
]);

export const transitReader = transit.reader('json', {
  handlers: { list: identity }
});
