import calendar
import datetime

import dateutil

"""
iso8601_to_epoch
@:param date
convert UTC iso8601 date into unix epoch time
"""


def iso8601_to_epoch(date):
    return calendar.timegm(dateutil.parser.parse(date).timetuple())


'''
iso8601_to_jdate
@:param date
convert UTC iso8601 into julian (jdate)
'''


def iso8601_to_jdate(date):
    t = dateutil.parser.parse(date).timetuple()
    if t.tm_yday < 100:
        return str(t.tm_year) + '0' + str(t.tm_yday)
    return str(t.tm_year) + str(t.tm_yday)


'''
iso8601_to_regular_datetime
@:param date
convert UTC iso8601 into easily human readable time format
'''


def iso8601_to_regular_datetime(date):
    t = dateutil.parser.parse(date).timetuple()
    mon = str(t.tm_mon)
    if int(mon) < 10:
        mon = zero_padder(mon)
    mday = str(t.tm_mday)
    if int(mday) < 10:
        mday = zero_padder(mday)
    hour = str(t.tm_hour)
    if int(hour) < 10:
        hour = zero_padder(hour)
    min = str(t.tm_min)
    if int(min) < 10:
        min = zero_padder(min)
    sec = str(t.tm_sec)
    if int(sec) < 10:
        sec = zero_padder(sec)
    return str(t.tm_year)[2:] + '/' + mon + '/' + mday + ' ' + hour + ':' + min + ':' + sec


'''
iso8601_to_regular_datetime
@:param e
convert epoch time (in seconds) into UTC iso8601 format
'''


def epoch_to_iso(e):
    return datetime.datetime.utcfromtimestamp(e).isoformat()


'''
zero_padder
@:param s
pad the string with one leading zero
'''


def zero_padder(s):
    return '0' + s
