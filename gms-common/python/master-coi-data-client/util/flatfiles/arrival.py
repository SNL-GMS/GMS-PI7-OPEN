from util.access import retrieve_signal_detections
from util.access import retrieve_stations_by_id
from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

'''
Write the CSS ARRIVAL flatfile.
@:param sigDetHyps : a list of signal detection hypotheses
@:param hostname: ability to override hostname for service queries
'''


def write_arrival(sigDetHyps, hostname):
    arid = ARIDPRIMARYKEYSTART
    with open('p3.ARRIVAL', 'w') as f:
        #  for each sigdethypid get sig det hyp object by id
        for sdh in sigDetHyps:
            # assign all NA values
            stassid = format_stassid(NA_GENERAL_NEG)
            chan = format_chan(NA_DASH)
            auth = format_auth(NA_DASH)
            chanid = format_chanid(NA_GENERAL_NEG)
            logat = format_logat(NA_LOGAT)
            clip = format_clip(NA_DASH)
            fm = format_fm(NA_DASH)
            qual = format_qual(NA_DASH)
            commid = format_commid(NA_GENERAL_NEG)
            stype = format_stype(NA_DASH)
            deltim = format_deltim(NA_GENERAL_NEG_FLOAT)
            azimuth = format_azimuth(NA_GENERAL_NEG_FLOAT)
            delaz = format_delaz(NA_GENERAL_NEG_FLOAT)
            slow = format_slow(NA_GENERAL_NEG_FLOAT)
            delslo = format_delslo(NA_GENERAL_NEG_FLOAT)
            ema = format_ema(NA_GENERAL_NEG_FLOAT)
            rect = format_rect(NA_GENERAL_NEG_FLOAT)
            amp = format_amp(NA_GENERAL_NEG_FLOAT)
            per = format_per(NA_GENERAL_NEG_FLOAT)
            snr = format_snr(NA_GENERAL_NEG_FLOAT)
            iphase = format_iphase(NA_DASH)
            sta = format_sta(NA_DASH)
            time = format_time_arrival(NA_ZERO)
            arid += 1
            aid = format_arid(str(arid))
            # make the chain of calls to get associated station name here
            parentSigDetId = sdh.get('parentSignalDetectionId', None)
            if parentSigDetId:
                stations = None
                #  call this method with the parentSigDetId in a list, as the method will automatically extract it
                #  due to how command line args for ids are collated into lists
                parentSigDets, url = retrieve_signal_detections(None, [parentSigDetId], False, None, None, hostname)
                if parentSigDets is not None:
                    stationId = parentSigDets.get('stationId')
                    stations, url = retrieve_stations_by_id(stationId, None, None, hostname)
                if stations:
                    sta = format_sta(stations[0].get('name'))
            featureMeasurements = sdh.get('featureMeasurements')  # get the list of fms associated with this sdh
            if not featureMeasurements:
                f.write(make_row([sta, time, aid, jdate, stassid, chanid, chan, iphase, stype, deltim, azimuth,
                                  delaz, slow, delslo, ema, rect, per, logat, clip, fm, snr, qual, auth, commid,
                                  lddate]) + '\n')
                continue
            types = [feat.get('featureMeasurementType') for feat in featureMeasurements]
            for fms, t, in zip(featureMeasurements, types):
                fs = fms.get('featureMeasurement')
                if t == 'ARRIVAL_TIME':
                    time = format_time_arrival(str(fs.get('value', time)))
                    deltim = format_deltim(str(fs.get('standardDeviation', deltim)))
                if t == 'AZIMUTH':
                    azimuth = format_azimuth(str(fs.get('value', azimuth)))
                    delaz = format_delaz(str(fs.get('standardDeviation', delaz)))
                if t == 'SLOWNESS':
                    slow = format_slow(str(fs.get('value', slow)))
                    delslo = format_delslo(str(fs.get('standardDeviation', delslo)))
                if t == 'RECTILINEARITY':
                    rect = format_rect(str(fs.get('value', rect)))
                if t == 'AMPLITUDE':
                    amp = format_amp(str(fs.get('value', amp)))
                if t == 'PERIOD':
                    per = format_per(str(fs.get('value', per)))
                if t == 'SNR':
                    snr = format_snr(str(fs.get('value', snr)))
            f.write(make_row([sta, time, aid, jdate, stassid, chanid, chan, iphase, stype, deltim, azimuth,
                                  delaz, slow, delslo, ema, rect, per, logat, clip, fm, snr, qual, auth, commid,
                                  lddate]) + '\n')
            # reset values to NAs before next FM row
            deltim = format_deltim(NA_GENERAL_NEG_FLOAT)
            azimuth = format_azimuth(NA_GENERAL_NEG_FLOAT)
            delaz = format_delaz(NA_GENERAL_NEG_FLOAT)
            slow = format_slow(NA_GENERAL_NEG_FLOAT)
            delslo = format_delslo(NA_GENERAL_NEG_FLOAT)
            rect = format_rect(NA_GENERAL_NEG_FLOAT)
            amp = format_amp(NA_GENERAL_NEG_FLOAT)
            per = format_per(NA_GENERAL_NEG_FLOAT)
            snr = format_snr(NA_GENERAL_NEG_FLOAT)
