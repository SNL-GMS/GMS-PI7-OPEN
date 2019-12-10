from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
write_sensor
@:param chanObj
@:param siteObj
@:param calibrationObj
@:param sensorObj
Write the SENSOR CSS flatfile with fields:
sta ,chan, time ,endtime, inid, chanid, jdate, calratio, calper, tshift, instant, lddate
'''


def write_sensor(chanObj, siteObj, calibrationObj, sensorObj):
    if chanObj is None or siteObj is None or calibrationObj is None or sensorObj is None:
        open('p3.sensor', 'w').close()
        return
    with open('p3.sensor', 'w') as f:
        chanIdIndex = -1
        jdate = format_offdate(NA_GENERAL_NEG)
        calratio = format_calratio(NA_GENERAL_POS)
        instant = 'y'
        chan = format_chan(str(chanObj[0].get('name')))
        inid = INSTRUMENTPRIMARYKEYSTART - 1  # will have to increment once before writing
        chanid = CHANPRIMARYKEYSTART
        for index, s in enumerate(siteObj):
            chanIdIndex += 1
            if index < len(siteObj) - 1:
                if s.get('entityId') == siteObj[index + 1].get('entityId'):
                    continue
            sta = format_sta(str(s.get('name')))
            for dex, n in enumerate(sensorObj):
                time = format_time(str(n.get('actualTime')))
                if dex < len(sensorObj) - 1:
                    endtime = format_endtime(str(sensorObj[index + 1].get('actualTime')))
                    if n.get('id') == sensorObj[dex + 1].get('id'):
                        break
                else:
                    endtime = format_endtime(NA_ENDTIME)
                inid += 1

                i = str(inid)
                for index, x in enumerate(calibrationObj):
                    if index > 0 and x.get('entityId') == calibrationObj[index - 1].get('entityId'):
                        continue
                    else:
                        calper = format_calper(str(x.get('calibrationPeriod')))
                        tshift = format_tshift(str(x.get('timeShift')))
                    row = sta + gap() + chan + gap() + time + gap() + endtime + gap() + i + gap() + \
                          str(chanid) + gap() + jdate + gap() + calratio + gap() + calper + gap() + tshift + gap() + \
                          instant + gap() + lddate + '\n'
                    f.write(row)
