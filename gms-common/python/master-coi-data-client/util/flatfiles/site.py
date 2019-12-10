from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
write_site
@:param siteObj
@:param stationObj
@:param siteToStaMemRelationship
Write SITE CSS flatfile with fields

name, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast, lddate
'''


def write_site(siteObj, stationObj, siteToStaMemRelationship):
    if siteObj is None or stationObj is None or siteToStaMemRelationship is None:
        open('p3.site', 'w').close()
        return
    stationObj = stationObj[0]
    with open('p3.site', 'w') as f:
        for index, station in enumerate(stationObj):
            name = format_sta(str(station.get('name')))
            refsta = name
            ondate = format_ondate(str(station.get('actualChangeTime')))
            if index < len(stationObj) - 1:
                offdate = format_offdate(str(stationObj[index + 1].get('actualChangeTime')))
            else:
                offdate = format_offdate(NA_GENERAL_NEG)
            lat = format_lat(str(station.get('latitude')))
            if not lat:
                lat = format_lat(NA_LONG_AND_LAT)
            lon = format_lon(str(station.get('longitude')))
            if not lon:
                lon = format_lon(NA_LONG_AND_LAT)
            elev = format_elev(str(station.get('elevation')))
            if not elev:
                elev = format_elev(NA_LONG_AND_LAT)
            d = station.get('description')
            if not d:
                d = NA_DASH
            staname = format_staname(d)
            type = str(station.get('stationType'))
            statype = STATYPE_DEFAULT
            if 'array' in type.lower():
                statype = format_statype('ar')
            elif 'component' in type.lower():
                statype = format_statype('ss')
            dnorth = format_dnorth(NA_ZERO)
            deast = format_deast(NA_ZERO)
            stationRow = name + gap() + ondate + gap() + offdate + gap() + lat + gap() + lon + gap() + elev + gap() + staname + gap() + statype + gap() + refsta + gap() + \
                         dnorth + gap() + deast + gap() + lddate + '\n'
            f.write(stationRow)
        for index, site in enumerate(siteObj):
            name = format_sta(str(site.get('name')))  # refsta stays the same as above
            ondate = format_ondate(str(site.get('actualChangeTime')))
            if index < len(siteObj) - 1:
                offdate = format_offdate(str(siteObj[index + 1].get('actualChangeTime')))
            else:
                offdate = format_offdate('-1')
            lat = format_lat(str(site.get('latitude')))
            if not lat:
                lat = format_lat(NA_LONG_AND_LAT)
            lon = format_lon(str(site.get('longitude')))
            if not lon:
                lon = format_lon(NA_LONG_AND_LAT)
            elev = format_elev(str(site.get('elevation')))
            if not elev:
                elev = format_elev(NA_LONG_AND_LAT)
            d = site.get('description')
            if not d:
                d = NA_DASH
            staname = format_staname(d)
            statype = format_statype('ss')
            dnorth = format_dnorth(NA_ZERO)
            deast = format_deast(NA_ZERO)
            siteRow = name + gap() + ondate + gap() + offdate + gap() + lat + gap() + lon + gap() + elev + gap() + staname + gap() + statype + gap() + refsta + gap() + \
                      dnorth + gap() + deast + gap() + lddate + '\n'
            f.write(siteRow)
