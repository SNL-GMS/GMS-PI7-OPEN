from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
write_instrument
@:param outputDir
@:param chanObj
@:param calibrationObj
@:param sensorObj
@:param responseObj
Write the INSTRUMENT CSS flatfile with fields:
inid, insname, instype, band, digital, samprate, ncalib, ncalper, dir, dfile, rsptype. lddate
'''


def write_instrument(outputDir, chanObj, calibrationObj, sensorObj, responseObj):
    if chanObj is None or calibrationObj is None or sensorObj is None or responseObj is None:
        open('p3.instrument', 'w').close()
        return
    if outputDir is None:
        baseDir = os.getcwd() + '/responses'
    else:
        baseDir = outputDir + '/responses'
    with open('p3.instrument', 'w') as f:
        inid = INSTRUMENTPRIMARYKEYSTART
        band = chanObj[0].get('name')[0].lower()
        digital = 'd'
        samprate = format_samprate(str(chanObj[0].get('nominalSampleRate')))
        for s in sensorObj:
            insname = format_insname(str(s.get('instrumentManufacturer')))
            instype = format_instype(str(s.get('instrumentModel')))
            for r in responseObj:
                dir = baseDir + str(inid) + '/'
                if not os.path.exists(dir):
                    os.mkdir(dir)
                dfile = base64.b64decode(r.get('responseData')).decode('utf8')
                n = 'data' + str(inid)
                filename = dir + n
                with open(filename, 'w') as d:
                    d.write(dfile)
                rsptype = format_rsptype(str(r.get('responseType')))
            for c in calibrationObj:
                ncalib = format_ncalib(str(c.get('calibrationFactor')))
                ncalper = format_ncalper(str(c.get('calibrationPeriod')))
                row = str(inid) + gap() + insname + gap() + instype + gap() + band + gap() + digital + gap() + \
                      samprate + gap() + ncalib + gap() + ncalper + gap() + dir + gap() + n + gap() + \
                      rsptype + gap() + lddate + '\n'
                f.write(row)
            inid += 1
