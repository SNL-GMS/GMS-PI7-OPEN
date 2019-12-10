from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
write_sitechan
@:param channelObj
@:param siteObj
Write SITECHAN CSS flatfile with fields
sta, chan, ondate, chanId, offdate, ctype, edepth, hang, vang, descrip, lddate

'''


def write_sitechan(channelObj, siteObj):
    if channelObj is None or siteObj is None:
        open('p3.sitechan', 'w').close()
        return
    site = siteObj[0]
    sta = format_sta(str(site.get('name')))
    chanid = CHANPRIMARYKEYSTART - 1
    chan = channelObj[0].get('name')
    with open('p3.sitechan', 'w') as f:
        for index, ch in enumerate(channelObj):
            last = chan
            chan = format_chan(str(ch.get('name')))
            if last != chan:
                chanid += 1
            ondate = format_ondate(str(ch.get('actualTime')))
            if index < len(channelObj) - 1:
                offdate = format_offdate(str(channelObj[index + 1].get('actualTime')))
            else:
                offdate = format_offdate('-1')
            ctype = format_ctype('n')
            edepth = format_edepth(str(ch.get('depth')))
            h = str(ch.get('horizontalAngle'))
            if not h:
                h = '-1.0'
            hang = format_hang(h)
            v = str(ch.get('verticalAngle'))
            if not v:
                v = '-1.0'
            vang = format_vang(v)
            d = ch.get('description')
            if not d:
                d = NA_DASH
            descrip = format_descrip(d)
            row = sta + gap() + chan + gap() + ondate + gap() + str(chanid) + gap() + offdate + gap() + ctype + gap() + \
                  edepth + gap() + hang + gap() + vang + gap() + descrip + gap() + lddate + '\n'
            f.write(row)
