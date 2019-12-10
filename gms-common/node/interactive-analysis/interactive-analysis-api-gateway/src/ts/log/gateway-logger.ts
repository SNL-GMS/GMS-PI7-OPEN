/**
 * provide back-end logger for gateway 
 */

import { Logger, transports, LoggerInstance, LoggerOptions } from 'winston';
import * as fs from 'fs';
import * as config from 'config';

// TODO daily rotate file does not seem to work without require-style import
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
const winston = require('winston');
// tslint:disable-next-line:no-implicit-dependencies no-var-requires no-require-imports
require('winston-daily-rotate-file');

const env = process.env.NODE_ENV || 'development';
const logDir = 'log';
const logFile = 'gateway.log';
const dailyLogFile = 'gateway-daily.log';
const errorLogFile = 'errors.log';

// Create the log directory if it does not exist
if (!fs.existsSync(logDir)) {
  try {
    fs.mkdirSync(logDir);
  } catch (e) {
    // Do nothing if error caught
    // Log directory already exists and that's ok
    // When running tests - race conditions cause us to enter this state
  }
}

// const tsFormat = () => (new Date()).toLocaleTimeString();
const tsUTCFormat = () => (new Date()).toUTCString();
const tsISOFormat = () => (new Date()).toISOString();

const fileMaxNumber = 3;
const fileBaseSize = 1024;
const fileMaxSize = Math.pow(fileBaseSize, 2);
const options: LoggerOptions = {
  levels: {
    info: 0,
    error: 1,
    warn: 2,
    data: 3,
    debug: 4
  },
  colors: {
    info: 'green',
    debug: 'blue',
    error: 'red',
    warn: 'yellow',
    data: 'magenta'
  },
  exitOnError: false,
  transports: [
    // colorize the output to the console
    new (transports.Console)({
      timestamp: tsISOFormat,
      colorize: true,
      prettyPrint: true,
      // label: __filename,
      level: config.get('logLevel')
    }),
    new (transports.File)({
      name: 'debug',
      label: 'Log',
      filename: `${logDir}/${logFile}`,
      timestamp: tsISOFormat,
      maxFiles: fileMaxNumber,
      maxsize: fileMaxSize,
      level: env === 'development' ? 'debug' : 'info'
    }),
    new (transports.File)({
      name: 'error',
      label: 'errorLog',
      filename: `${logDir}/${errorLogFile}`,
      timestamp: tsUTCFormat,
      level: 'error'
    }),
    new (winston.transports.DailyRotateFile)({
      name: 'daily',
      filename: `${logDir}/${dailyLogFile}`,
      datePattern: 'yyyy-MM-dd.',
      prepend: true,
      localTime: false,
      zippedArchive: false,
      maxDays: 7,
      createTree: false,
      level: process.env.ENV === 'development' ? 'debug' : 'info'
    })
  ]
};
// create our logger instance
export const gatewayLogger: LoggerInstance = new Logger(options);

gatewayLogger.info('Logging to %s/%s', logDir, logFile);
gatewayLogger.info('NODE_ENV set to %s', process.env.NODE_ENV);
gatewayLogger.info('NODE_CONFIG_ENV set to %s', process.env.NODE_CONFIG_ENV);
