from pandas.io.json import json_normalize

from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
write_affiliation
@:param stationObj
@:param networkObj
@:param networkMembersObj
@:param siteObj
@:param stationMembersObj
Write NETWORK CSS flatfile with fields 
net, sta, time, endtime, lddate
'''


def write_affiliation(stationObj, networkObj, networkMembersObj, siteObj, stationMembersObj):
    if stationObj is None or siteObj is None or stationMembersObj is None:
        open('p3.affiliation', 'w').close()
        return
    with open('p3.affiliation', 'w') as f:
        if networkMembersObj is not None:
            networkMembersObj = networkMembersObj[0]
            for index, n in enumerate(networkMembersObj):
                norm = json_normalize(n)  # get a row
                networkId = norm['networkId'].tolist()[0]  # only one object in this row
                stationId = norm['stationId'].tolist()[0]
                time = format_time(str(norm['actualChangeTime'].tolist()[0]))
                endtime = format_endtime(NA_ENDTIME)
                if index < len(networkMembersObj) - 1:
                    normNext = json_normalize(networkMembersObj[index + 1])  # get the next row
                    if normNext['networkId'].tolist()[0] == networkId:
                        endtime = format_endtime(str(normNext['actualChangeTime'].tolist()[0]))
                for netObj in networkObj:
                    netObj = netObj[0]
                    if netObj.get('entityId') == networkId:
                        net = format_net(str(netObj.get('name')))
                for staObj in stationObj:
                    staObj = staObj[0]
                    if staObj.get('entityId') == stationId:
                        sta = format_sta(str(staObj.get('name')))
                row = net + gap() + sta + gap() + time + gap() + endtime + gap() + lddate + '\n'
                f.write(row)

        for index, n in enumerate(stationMembersObj):
            norm = json_normalize(n)
            stationId = norm['stationId'].tolist()[0]
            siteId = norm['siteId'].tolist()[0]
            time = format_time(str(norm['actualChangeTime'].tolist()[0]))
            if index < len(stationMembersObj) - 1:
                normNext = json_normalize(stationMembersObj[index + 1])
                endtime = format_endtime(str(normNext['actualChangeTime'].tolist()[0]))
            else:
                endtime = format_endtime(NA_ENDTIME)
            for staObj in stationObj:
                staObj = staObj[0]
                if staObj.get('entityId') == stationId:
                    net = format_net(str(staObj.get('name')))
            for siObj in siteObj:
                if siObj.get('entityId') == siteId:
                    sta = format_sta(str(siObj.get('name')))
            row = net + gap() + sta + gap() + time + gap() + endtime + gap() + lddate + '\n'
            f.write(row)
