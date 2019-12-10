
import * as moment from 'moment';

/**
 * Time utilty.
 */
// tslint:disable-next-line:no-unnecessary-class
export class TimeUtil {

    /**
     * Date and time format to the second precision
     */
    public static DATE_TIME_FORMAT_S: string = 'YYYY-MM-DD HH:mm:ss';

    /** 
     * Date and time format to the sub-second precision (two decimals places) 
     */
    public static DATE_TIME_FORMAT_MS: string = 'YYYY-MM-DD HH:mm:ss.SS';

    /** 
     * Date format when there's no time (beyond 00:00:00) available 
     */
    public static DATE_FORMAT: string = 'YYYY-MM-DD';

    /** 
     * Date format when there's no time (beyond 00:00:00) available 
     */
    public static HOUR_MINUTE_SECOND: string = 'HH:mm:ss';

    /** 
     * Time format to the second precision 
     */
    public static TIME_FORMAT: string = 'HH:mm:ss.SS';

    /** Time format to the sub-second precision (two decimals places) */
    public static TIME_FORMAT_MS: string = 'HH:mm:ss.SS';

    /** ISO time format */
    public static DATE_ISO_FORMAT: string = 'YYYY/MM/DDTHH:mm:ss.SSSSSS';

    /** ISO short format */
    public static SHORT_ISO_FORMAT: string = 'YYYY/MM/DDTHH:mm';

    private constructor() { /* no-op */ }

    /**
     * Format seconds to a Moment  object.
     * @param seconds the seconds
     */
    public static toMoment = (secs: number): moment.Moment =>
        moment.unix(secs)
            .utc()

    /**
     * Format seconds to a JS Date object.
     * @param seconds the seconds
     */
    public static toDate = (secs: number): Date =>
        moment.unix(secs)
            .utc()
            .toDate()

    /**
     * Format seconds to a readable date string
     * @param seconds the seconds
     */
    public static toDateString = (secs: number): string =>
        TimeUtil.toString(secs, TimeUtil.DATE_FORMAT)

    /**
     * Format seconds to a readable time string
     * @param seconds the seconds
     */
    public static toTimeString = (secs: number): string =>
        TimeUtil.toString(secs, TimeUtil.TIME_FORMAT)

    /**
     * Format seconds to a readable date/time string.
     * @param seconds the seconds
     * @param format the format string of the date/time - defaults to 
     *  'YYYY-MM-DD HH:mm:ss.SS'
     */
    public static toString = (secs: number,
        format: string = TimeUtil.DATE_TIME_FORMAT_MS): string =>
        TimeUtil.toMoment(secs)
            .utc()
            .format(format)

    /**
     * Format a JS date to a readable date/time string.
     * @param date the JS date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY-MM-DD HH:mm:ss'
     */
    public static dateToString = (date: Date,
        format: string = TimeUtil.DATE_TIME_FORMAT_S): string =>
        moment(date)
            .format(format)
    /**
     * Format a JS date to a readable date/time string.
     * @param date the JS date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDTHH:mm:ss.SSSSS'
     */

    public static dateToISOString = (date: Date,
        format: string = TimeUtil.DATE_ISO_FORMAT): string =>
        moment.utc(date)
            .format(format)
    /**
     * Format a JS date to a readable date/time string.
     * @param date the JS date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDTHH:mm:ss.SSSSS'
     */

    public static dateToShortISOString = (date: Date,
        format: string = TimeUtil.SHORT_ISO_FORMAT): string =>
        moment.utc(date)
            .format(format)
    /**
     * Format a time in secs to a readable date/time string.
     * @param date the JS date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDTHH:mm:ss.SSSSS'
     */

    public static timesecsToISOString = (date: Date,
        format: string = TimeUtil.DATE_ISO_FORMAT): string =>
        moment.utc(date)
            .format(format)
    /**
     * Convert an iso string to a date.
     * @param isoString the iso datestring
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDTHH:mm:ss.SSSSS'
     */

    public static parseISOString = (isoString: string,
        format: string = TimeUtil.DATE_ISO_FORMAT): Date =>
        new Date(moment.utc(isoString, TimeUtil.DATE_ISO_FORMAT)
            .valueOf())

    /**
     * Format a time in secs to a readable date/time string.
     * @param date the JS date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDT'
     */

    public static timesecsToShortISOString = (date: Date,
        format: string = TimeUtil.SHORT_ISO_FORMAT): string =>
        moment.utc(date)
            .format(format)
    /**
     * Format a time in secs to a readable date/time string.
     * @param date the JS date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDT'
     */

    public static dateToHoursMinutesSeconds = (date: Date,
        format: string = TimeUtil.HOUR_MINUTE_SECOND): string =>
        moment.utc(date)
            .format(format)
    /**
     * Convert an iso string to a date.
     * @param isoString the iso datestring
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDTHH'
     */

    public static parseShortISOString = (isoString: string,
        format: string = TimeUtil.SHORT_ISO_FORMAT): Date =>
        new Date(moment.utc(isoString, TimeUtil.DATE_ISO_FORMAT)
            .valueOf())

    /**
     * Format a julian date (YYYYDDD) to a readable date/time string.
     * @param julianDate the julian date
     * @param format the format string of the date/time - defaults to 
     *  'YYYY/MM/DDTHH:mm:ss.SSSSS'
     */

    public static julianDateToISOString = (julianDate: string,
        format: string = TimeUtil.DATE_ISO_FORMAT): string =>
        moment.utc(moment(julianDate, 'YYYYDDD'))
            .format(format)

}
