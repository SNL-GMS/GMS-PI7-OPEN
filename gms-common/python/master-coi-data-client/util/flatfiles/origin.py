from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

"""
Write an ORIGIN flat file.

@:param event: A single Event object.
@:param assocs: associations between signal detection hypotheses and events
@:param sigDetHyps: signal detection hypotheses
@param eventHypothesis: A single EventHypothesis object


"""


def write_origin(events, assocs, sigDetHypes):
    evid = EVIDPRIMARYKEYSTART
    orid = ORIDPRIMARYKEYSTART
    with open('p3.ORIGIN', 'w') as f:
        for e in events:
            # initialize values to NAs
            grn = format_grn(NA_GENERAL_NEG)
            srn = format_srn(NA_GENERAL_NEG)
            etype = format_etype(NA_DASH)
            depdp = format_depdp(NA_LOGAT)
            mb = format_mb(NA_LOGAT)
            mbid = format_mbid(NA_GENERAL_NEG)
            ms = format_ms(NA_LOGAT)
            msid = format_msid(NA_GENERAL_NEG)
            ml = format_ml(NA_LOGAT)
            mlid = format_mlid(NA_GENERAL_NEG)
            algorithm = format_algor(NA_DASH)
            auth = format_auth(NA_DASH)
            commid = format_commid(NA_GENERAL_NEG)
            jdate = format_jdate(NA_GENERAL_NEG)
            time = format_time_arrival(NA_ZERO)
            ndef = format_ndef(NA_ZERO)
            ndp = format_ndp(NA_ZERO)
            nass = format_nass(NA_ZERO)
            lat = format_lat(NA_LONG_AND_LAT)
            long = format_lon(NA_LONG_AND_LAT)
            depth = format_depth(NA_LONG_AND_LAT)
            dtype = format_dtype(NA_GENERAL_NEG)
            eh = e.get('hypotheses')
            if not eh:  # no hypotheses for this event
                f.write(make_row([lat, long, depth, time,
                                  format_orid(str(orid)), format_evid(str(evid)), jdate, nass, ndef,
                                  format_ndp(str(ndp)), grn, srn, etype, depdp, dtype, mb, mbid, ms,
                                  msid, ml, mlid, algorithm, auth, commid, lddate]) + '\n')
                orid += 1
                continue
            for eventHypothesis in eh:
                # initialize values to NAs
                grn = format_grn(NA_GENERAL_NEG)
                srn = format_srn(NA_GENERAL_NEG)
                etype = format_etype(NA_DASH)
                depdp = format_depdp(NA_LOGAT)
                mb = format_mb(NA_LOGAT)
                mbid = format_mbid(NA_GENERAL_NEG)
                ms = format_ms(NA_LOGAT)
                msid = format_msid(NA_GENERAL_NEG)
                ml = format_ml(NA_LOGAT)
                mlid = format_mlid(NA_GENERAL_NEG)
                algorithm = format_algor(NA_DASH)
                auth = format_auth(NA_DASH)
                commid = format_commid(NA_GENERAL_NEG)
                jdate = format_jdate(NA_GENERAL_NEG)
                time = format_time_arrival(NA_ZERO)
                ndef = format_ndef(NA_ZERO)
                ndp = format_ndp(NA_ZERO)
                nass = format_nass(NA_ZERO)
                lat = format_lat(NA_LONG_AND_LAT)
                long = format_lon(NA_LONG_AND_LAT)
                depth = format_depth(NA_LONG_AND_LAT)
                dtype = format_dtype(NA_GENERAL_NEG)
                if not eventHypothesis:
                    f.write(make_row([lat, long, depth, time,
                                      format_orid(str(orid)), format_evid(str(evid)), jdate, nass, ndef,
                                      format_ndp(str(ndp)), grn, srn, etype, depdp, dtype, mb, mbid, ms,
                                      msid, ml, mlid, algorithm, auth, commid, lddate]) + '\n')
                    orid += 1
                    continue
                sigDetHypIds = [a.get('signalDetectionHypothesisId') for a in assocs if a.get('eventHypothesisId') ==
                                eventHypothesis.get('id')]
                associatedSigDetHypes = [s for s in sigDetHypes if s.get('id') in sigDetHypIds]
                if not associatedSigDetHypes:
                    f.write(make_row([lat, long, depth, time,
                                      format_orid(str(orid)), format_evid(str(evid)), jdate, nass, ndef,
                                      format_ndp(str(ndp)), grn, srn, etype, depdp, dtype, mb, mbid, ms,
                                      msid, ml, mlid, algorithm, auth, commid, lddate]) + '\n')
                    orid += 1
                    continue
                arrival_time_ids = []
                ndp = 0
                for sdh in associatedSigDetHypes:
                    fms = sdh.get('featureMeasurements')
                    for fm in fms:
                        if fm.get('featureMeasurementType') == 'ARRIVAL_TIME':
                            arrival_time_ids.append(fm.get('id'))
                        elif fm.get('featureMeasurementType') == 'PHASE':
                            if fm.get('featureMeasurement').get('value') in ['pP', 'sP']:
                                ndp = ndp + 1
                # Get number of associated phases
                nass = format_nass(str(sum([a.get('eventHypothesisId') == eventHypothesis.get('id') for a in assocs])))
                ndef = 0
                locationSolutions = eventHypothesis.get('locationSolutions')
                if locationSolutions:
                    for sol in locationSolutions:
                        loc = sol.get('location')
                        lat = format_lat(str(loc.get('latitudeDegrees')))
                        long = format_lon(str(loc.get('longitudeDegrees')))
                        depth = format_depth(str(loc.get('depthKm')))
                        dtype = format_dtype(str(sol.get('locationRestraint').get('depthRestraintType')))
                        lb = sol.get('locationBehaviors')
                        if lb:
                            ndef = format_ndef(
                                str(sum(
                                    [l.get('featureMeasurementId') in arrival_time_ids and l.get('defining') for l in
                                     lb])))
                            f.write(make_row([lat, long, depth, format_time(str(loc.get('time'))),
                                              format_orid(str(orid)), format_evid(str(evid)), jdate, nass, ndef,
                                              format_ndp(str(ndp)), grn, srn, etype, depdp, dtype, mb, mbid, ms,
                                              msid, ml, mlid, algorithm, auth, commid, lddate]) + '\n')
                else:
                    f.write(make_row([lat, long, depth, format_time(str(loc.get('time'))),
                                      format_orid(str(orid)), format_evid(str(evid)), jdate, nass, ndef,
                                      format_ndp(str(ndp)), grn, srn, etype, depdp, dtype, mb, mbid, ms,
                                      msid, ml, mlid, algorithm, auth, commid, lddate]) + '\n')
                orid += 1
            evid += 1
