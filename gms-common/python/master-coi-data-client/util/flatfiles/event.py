from copy import deepcopy

from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

"""
 Write the CSS EVENT flat file.
"""


def write_event(events):
    with open('p3.EVENT', 'w') as f:
        evid = deepcopy(EVIDPRIMARYKEYSTART)
        for e in events:
            evid += 1
            #  setup NA values
            eid = format_evid(str(evid))
            evname = format_evname(NA_DASH)
            prefor = format_prefor(NA_GENERAL_NEG)
            auth = format_auth(NA_DASH)
            commid = format_commid(NA_GENERAL_NEG)
            if 'preferredEventHypothesisHistory' not in e:
                f.write(make_row([eid, evname, prefor, auth, commid, lddate]) + '\n')
                continue
            else:
                # for each preferred event hypothesis for this event
                # write its id
                preferredEventHypotheses = e.get('preferredEventHypothesisHistory')
                for h in preferredEventHypotheses:
                    # take only the numeric values from the UUID string as the prefor id
                    prefor = format_prefor(str(int(''.join(filter(str.isdigit, h.get('eventHypothesisId', prefor))))))
                    f.write(make_row([eid, evname, prefor, auth, commid, lddate]) + '\n')
