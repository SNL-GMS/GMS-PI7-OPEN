from pandas.io.json import json_normalize

from util.flatfiles.shared import *

'''
write_network
@:param networkObj
Write NETWORK CSS flatfile with fields
net, netname, nettype, auth, commid, systemChangeTime
'''


def write_network(networkObj):
    if networkObj is None:
        open('p3.network', 'w').close()
        return
    with open('p3.network', 'w') as f:
        for n in networkObj:
            norm = json_normalize(n)
            net = str(norm['name'].tolist()[0])
            nn = str(norm['description'].tolist()[0])
            if not nn:
                netname = NA_DASH
            else:
                netname = nn
            r = str(norm['region'].tolist()[0])
            if r == 'GLOBAL':
                nettype = 'ww'
            elif r == 'REGIONAL':
                nettype = 'ar'
            elif r == 'LOCAL':
                nettype = 'LO'
            else:
                nettype = NA_DASH
            a = str(norm['source.originatingOrganization'].tolist()[0])
            if not a:
                auth = NA_DASH
            else:
                auth = a
            commid = NA_GENERAL_NEG
            systemChangeTime = str(norm['systemChangeTime'].tolist()[0])
            row = format_net(net) + gap() + format_netname(netname) + gap() + format_nettype(
                nettype) + gap() + format_auth(auth) + gap() + format_commid(
                commid) + gap() + format_system_change_time(systemChangeTime) + '\n'
            f.write(row)
