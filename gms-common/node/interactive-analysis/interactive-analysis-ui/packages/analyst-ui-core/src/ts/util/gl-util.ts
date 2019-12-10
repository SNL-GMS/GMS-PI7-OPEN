import * as Gl from '@gms/golden-layout';
import * as lodash from 'lodash';
import * as React from 'react';

type callback = () => void;

/**
 * Attaches an event handler to the golden-layout event 'show' that will force 
 * the component to update when dispatched. 
 * 
 * @param glContainer the golden-layout container
 * @param callbackFn the callback to invoke on show
 */
export const addGlUpdateOnShow = (glContainer: Gl.Container, callbackFn: callback) => {
  if (glContainer && callbackFn) {
    // force update when the golden-layout cotainer is resized
    glContainer.on('show', () => {
      lodash.defer(() => {
        callbackFn();
      });
    });
  }
};

/**
 * Attaches an event handler to the golden-layout event 'show' that will force 
 * the component to update when dispatched. 
 * 
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnShow = (glContainer: Gl.Container, component: React.Component) => {
  if (glContainer && component) {
    addGlUpdateOnShow(glContainer, () => component.forceUpdate());
  }
};

/**
 * Attaches an event handler to the golden-layout event 'resize' that will force 
 * the component to update when dispatched. 
 * 
 * @param glContainer the golden-layout container
 * @param callbackFn the callback to invoke on resize
 */
export const addGlUpdateOnResize = (glContainer: Gl.Container, callbackFn: callback) => {
  if (glContainer && callbackFn) {
    // force update when the golden-layout cotainer is resized
    glContainer.on('resize', () => {
      callbackFn();
    });
  }
};

/**
 * Attaches an event handler to the golden-layout event 'resize' that will force 
 * the component to update when dispatched. 
 * 
 * @param glContainer the golden-layout container
 * @param component the react component to force update
 */
export const addGlForceUpdateOnResize = (glContainer: Gl.Container, component: React.Component) => {
  if (glContainer && component) {
    addGlUpdateOnResize(glContainer, () => component.forceUpdate());
  }
};
