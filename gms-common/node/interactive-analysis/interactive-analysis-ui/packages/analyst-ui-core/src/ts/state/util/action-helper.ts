import { Action } from 'redux';

// tslint:disable-next-line:no-empty-interface
export interface Action extends Action {}

export interface ActionWithPayload<T> extends Action {
  readonly payload: T;
}

export interface ActionCreator<T> {
  readonly type: string;
  (payload: T): ActionWithPayload<T>;

  test(action: Action): action is ActionWithPayload<T>;
}

export interface ActionCreatorVoid {
  readonly type: string;
  (): Action;

  test(action: Action): action is Action;
}

export const actionCreator = <T>(type: string): ActionCreator<T> =>
  Object.assign((payload: T): any => ({ type, payload }), {
    type,
    test(action: Action): action is ActionWithPayload<T> {
      return action.type === type;
    }
  });

export const actionCreatorVoid = (type: string): ActionCreatorVoid =>
  Object.assign((): any => ({ type }), {
    type,
    test(action: Action): action is Action {
      return action.type === type;
    }
  });
