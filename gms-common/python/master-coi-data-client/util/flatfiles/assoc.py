import itertools
from copy import deepcopy

from util.access import retrieve_signal_detections
from util.access import retrieve_stations_by_id
from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

"""
Write the CSS ASSOC flat file.  
"""


def write_assoc(events, assocs, sigDetHypotheses, start, end, hostname):
    arid = ARIDPRIMARYKEYSTART
    orid = deepcopy(ORIDPRIMARYKEYSTART)
    with open('p3.ASSOC', 'w') as f:
        for e in events:
            # assign all  NA values
            sta = format_sta(NA_DASH)
            belief = format_belief(NA_GENERAL_NEG)
            phase = format_phase(NA_DASH)
            delta = format_delta(NA_GENERAL_NEG_FLOAT)
            seaz = format_seaz(NA_LOGAT)
            esaz = format_esaz(NA_LOGAT)
            timeres = format_timeres(NA_LOGAT)
            timedef = format_timedef(NA_DASH)
            azres = format_azres(NA_LOGAT)
            azdef = format_azdef(NA_DASH)
            slores = format_slores(NA_LOGAT)
            slodef = format_slodef(NA_DASH)
            emares = format_emares(NA_LOGAT)
            wgt = format_wqt(NA_GENERAL_NEG)
            vmodel = format_vmodel(NA_DASH)
            commid = format_commid(NA_GENERAL_NEG)
            aid = format_arid(str(arid))
            oid = format_orid(str(orid))
            eh = e.get('hypotheses')
            if not eh:  # no hypotheses for this event
                f.write(make_row([aid, oid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef,
                                  slores, slodef, emares, wgt, vmodel, commid, lddate]) + '\n')
                orid += 1
                continue
            for eventHypothesis in eh:
                # assign all  NA values
                sta = format_sta(NA_DASH)
                belief = format_belief(NA_GENERAL_NEG)
                phase = format_phase(NA_DASH)
                delta = format_delta(NA_GENERAL_NEG_FLOAT)
                seaz = format_seaz(NA_LOGAT)
                esaz = format_esaz(NA_LOGAT)
                timeres = format_timeres(NA_LOGAT)
                timedef = format_timedef(NA_DASH)
                azres = format_azres(NA_LOGAT)
                azdef = format_azdef(NA_DASH)
                slores = format_slores(NA_LOGAT)
                slodef = format_slodef(NA_DASH)
                emares = format_emares(NA_LOGAT)
                wgt = format_wqt(NA_GENERAL_NEG)
                vmodel = format_vmodel(NA_DASH)
                commid = format_commid(NA_GENERAL_NEG)
                aid = format_arid(str(arid))
                oid = format_orid(str(orid))
                if not eventHypothesis:
                    f.write(make_row([aid, oid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef,
                                      slores, slodef, emares, wgt, vmodel, commid, lddate]) + '\n')
                    orid += 1
                    continue
                sigDetHypIds = [a.get('signalDetectionHypothesisId') for a in assocs if a.get('eventHypothesisId') ==
                                eventHypothesis.get('id')]
                associatedSigDetHypes = [s for s in sigDetHypotheses if s.get('id') in sigDetHypIds]
                if not associatedSigDetHypes:
                    f.write(make_row([aid, oid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef,
                                      slores, slodef, emares, wgt, vmodel, commid, lddate]) + '\n')
                    orid += 1
                    continue
                    # make the chain of calls to get associated station name
                for sdh in associatedSigDetHypes:
                    parentSigDetId = sdh.get('parentSignalDetectionId', None)
                    if parentSigDetId:
                        stations = None
                        #  call this method with the parentSigDetId in a list, as the method will automatically extract it
                        #  due to how command line args for ids are collated into lists
                        parentSigDets, url = retrieve_signal_detections(None, [parentSigDetId], False, None, None,
                                                                        hostname)
                        if parentSigDets is not None:
                            stationId = parentSigDets.get('stationId')
                            stations, url = retrieve_stations_by_id(stationId, None, None, hostname)
                        if stations:
                            sta = format_sta(stations[0].get('name'))
                    featureMeasurements = sdh.get('featureMeasurements')
                    if not featureMeasurements:
                        f.write(
                            make_row(
                                [aid, oid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef,
                                 slores, slodef, emares, wgt, vmodel, commid, lddate]) + '\n')
                        continue
                    locationSolutions = eventHypothesis.get('locationSolutions')
                    behaviors = list(
                        itertools.chain.from_iterable([sol.get('locationBehaviors') for sol in locationSolutions]))
                    types = [feat.get('featureMeasurementType') for feat in featureMeasurements]
                    for fms, t, in zip(featureMeasurements, types):
                        if t == 'PHASE':
                            phase = format_phase(str(fms.get('featureMeasurement').get('value')))
                        if t == 'SOURCE_TO_RECEIVER_DISTANCE':
                            delta = format_delta(str(fms.get('featureMeasurement').get('value')))
                        if t == 'SOURCE_TO_RECEIVER_AZIMUTH':
                            esaz = format_esaz(str(fms.get('featureMeasurement').get('value')))
                        if t == 'RECEIVER_TO_SOURCE_AZIMUTH':
                            seaz = format_seaz(str(fms.get('featureMeasurement').get('value')))
                        if t == 'ARRIVAL_TIME':
                            # get the location behavior in the location solution that corresponds to this fm id
                            timer = [b.get('residual') for b in behaviors
                                     if b.get('featureMeasurementId') == fms.get('id')]
                            tim = [t for t in timer if t != timeres]
                            if tim and tim != timeres:
                                timeres = format_timeres(str(tim[0]))
                            timed = [b.get('defining') for b in behaviors
                                     if b.get('featureMeasurementId') == fms.get('id')]
                            tim = [t for t in timed if t != timedef]
                            if tim and tim != timedef:
                                timedef = format_timedef(str(tim[0]))
                        if t == 'AZIMUTH':
                            azr = [b.get('residual') for b in behaviors if
                                   b.get('featureMeasurementId') == fms.get('id')]
                            az = [a for a in azr if a != azres]
                            if az and az != azres:
                                azres = format_azres(str(az[0]))
                            azde = [b.get('residual') for b in behaviors if
                                    b.get('featureMeasurementId') == fms.get('id')]
                            azd = [d for d in azde if d != azdef]
                            if azd and azd != azdef:
                                azdef = format_azdef(str(azd[0]))
                        if t == 'SLOWNESS':
                            slore = [b.get('residual') for b in behaviors
                                     if b.get('featureMeasurementId') == fms.get('id')]
                            slor = [s for s in slore if s != slores]
                            if slor and slor != slores:
                                slores = format_slores(str(slor[0]))
                            slod = [b.get('residual') for b in behaviors
                                    if b.get('featureMeasurementId') == fms.get('id')]
                            slo = [sl for sl in slod if sl != slodef]
                            if slo and slo != slodef:
                                slodef = format_slodef(str(slo[0]))


                    f.write(
                        make_row([aid, oid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef,
                                  slores, slodef, emares, wgt, vmodel, commid, lddate]) + '\n')
                    # reset to default values
                    phase = format_phase(NA_DASH)
                    delta = format_delta(NA_GENERAL_NEG_FLOAT)
                    seaz = format_seaz(NA_LOGAT)
                    esaz = format_esaz(NA_LOGAT)
                    timeres = format_timeres(NA_LOGAT)
                    timedef = format_timedef(NA_DASH)
                    azres = format_azres(NA_LOGAT)
                    azdef = format_azdef(NA_DASH)
                    slores = format_slores(NA_LOGAT)
                    slodef = format_slodef(NA_DASH)
                arid += 1
            orid += 1
