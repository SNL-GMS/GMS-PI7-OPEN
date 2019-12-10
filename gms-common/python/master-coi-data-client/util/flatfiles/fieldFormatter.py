# -*- coding: utf-8 -*-
import util.flatfiles.shared
from util.conversions import *

'''
helper methods for formatting CSS flatfiles
All methods use ljust to left justify the field. 
The integer argument to ljust specifies that a field has a width of exactly that integer value: the field 
is padded with whitespace if the contents do not subsume the entire width.

The slicing (truncating) mechanism is added at the end of each field as a safeguard to 
ensure that a field has width no more than the integer value.
'''


def format_lat(latitude):
    return latitude.ljust(11)[:11]


def format_lon(longitude):
    return longitude.ljust(11)[:11]


def format_sta(siteUUID):
    return siteUUID.ljust(6)[:6]


def format_refsta(refsta):
    return refsta.ljust(6)[:6]


def format_statype(station_type):
    return station_type.ljust(4)[:4]


def format_elev(elevation):
    return elevation.ljust(9)[:9]


def format_dnorth(north_displacement):
    return north_displacement.ljust(9)[:9]


def format_deast(east_displacement):
    return east_displacement.ljust(9)[:9]


def format_chan(chanUUID):
    return chanUUID.ljust(8)[:8]


def format_jdate(actualChangeTime):
    return iso8601_to_jdate(actualChangeTime).ljust(8)[:8]


def format_ondate(actualChangeTime):
    return iso8601_to_jdate(actualChangeTime).ljust(8)[:8]


def format_offdate(actualChangeTime):
    if actualChangeTime == '-1':
        return actualChangeTime.ljust(8)[:8]
    else:
        return iso8601_to_jdate(actualChangeTime).ljust(8)[:8]


def format_calratio(calibrationConversionRatio):
    return calibrationConversionRatio.ljust(16)[:16]


def format_chanid(UUID):
    return UUID.ljust(8)[:8]


def format_version_id(versionId):
    return versionId.ljust(5)[:5]


def format_ctype(n):
    return n.ljust(4)[:4]


def format_edepth(depth):
    return depth.ljust(24)[:24]


def format_hang(horizontalAngle):
    return horizontalAngle.ljust(24)[:24]


def format_vang(verticalAngle):
    return verticalAngle.ljust(24)[:24]


def format_descrip(description):
    return description.ljust(50)[:50]


def format_net(net):
    return net.ljust(8)[:8]


def format_netname(netname):
    return netname.ljust(80)[:80]


def format_nettype(nettype):
    return nettype.ljust(4)[:4]


def format_auth(auth):
    return auth.ljust(15)[:15]


def format_commid(commid):
    return commid.ljust(9)[:9]


def format_system_change_time(systemChangeTime):
    return iso8601_to_regular_datetime(systemChangeTime).ljust(17)[:17]


def format_time(actualChangeTime):
    return str(iso8601_to_epoch(actualChangeTime)).ljust(17)[:17]


def format_time_arrival(timeString):
    return timeString.ljust(17)[:17]


def format_endtime(actualChangeTime):
    if actualChangeTime == util.flatfiles.shared.NA_ENDTIME:
        return actualChangeTime.ljust(17)[:17]
    else:
        return str(iso8601_to_epoch(actualChangeTime)).ljust(17)[:17]


def format_calper(calibrationPeriod):
    return calibrationPeriod.ljust(16)[:16]


def format_tshift(timeShift):
    return timeShift.ljust(16)[:16]


def format_staname(description):
    return description.ljust(50)[:50]


def format_insname(manufacturer):
    return manufacturer.ljust(50)[:50]


def format_instype(model):
    return model.ljust(6)[:6]


def format_samprate(nominalSampleRate):
    return nominalSampleRate.ljust(11)[:11]


def format_ncalib(nominalCalibrationFactor):
    return nominalCalibrationFactor.ljust(16)[:16]


def format_ncalper(nominalCalibrationPeriod):
    return nominalCalibrationPeriod.ljust(16)[:16]


def format_rsptype(type):
    return type.ljust(6)[:6]


def format_elev(elevation):
    return elevation.ljust(9)[:9]


def format_wfid(waveformId):
    return waveformId.ljust(8)[:8]


def format_nsamp(numberOfSamples):
    return numberOfSamples.ljust(8)[:8]


def format_samprate(sampleRatePerSecond):
    return sampleRatePerSecond.ljust(11)[:11]


def format_calib(nominalCalibration):
    return nominalCalibration.ljust(16)[:16]


def format_segtype(indexingMethod):
    return indexingMethod.ljust(1)[:1]


def format_datatype(numericStorage):
    return numericStorage.ljust(2)[:2]


def format_clip(clippedFlag):
    return clippedFlag.ljust(1)[:1]


def format_dir(directory):
    return directory.ljust(64)[:64]


def format_dfile(dataFile):
    return dataFile.ljust(32)[:32]


def format_foff(byteOffset):
    return byteOffset.ljust(10)[:10]


def format_commid_wfdisc(commid):
    return commid.ljust(8)[:8]


def format_arid(arrivalId):
    return arrivalId.ljust(9)[:9]


def format_stassid(arrivalGroupId):
    return arrivalGroupId.ljust(9)[:9]


def format_iphase(reportedPhase):
    return reportedPhase.ljust(8)[:8]


def format_stype(signalType):
    return signalType.ljust(1)[:1]


def format_deltim(arrivalTimeUncertainty):
    return arrivalTimeUncertainty.ljust(6)[:6]


def format_azimuth(observedAzimuth):
    return observedAzimuth.ljust(7)[:7]


def format_delaz(azimuthUncertainty):
    return azimuthUncertainty.ljust(7)[:7]


def format_slow(slownessMeasurement):
    return slownessMeasurement.ljust(7)[:7]


def format_delslo(slownessUncertainty):
    return slownessUncertainty.ljust(7)[:7]


def format_ema(emergenceAngle):
    return emergenceAngle.ljust(7)[:7]


def format_rect(signalRectilinearity):
    return signalRectilinearity.ljust(7)[:7]


def format_amp(measuredAmplitude):
    return measuredAmplitude.ljust(11)[:11]


def format_per(measuredPeriodAtTimeOfAmplitudeMeasurement):
    return measuredPeriodAtTimeOfAmplitudeMeasurement.ljust(7)[:7]


def format_logat(logOfAmpDividedByPeriod):
    return logOfAmpDividedByPeriod.ljust(7)[:7]


def format_fm(firstMotion):
    return firstMotion.ljust(2)[:2]


def format_snr(signalToNoiseRation):
    return signalToNoiseRation.ljust(10)[:10]


def format_qual(onsetArrivalQuality):
    return onsetArrivalQuality.ljust(1)[:1]


def format_belief(val):
    return val.ljust(4)[:4]


def format_delta(val):
    return val.ljust(8)[:8]


def format_evid(evid):
    return evid.ljust(9)[:9]


def format_evname(evname):
    return evname.ljust(32)[:32]


def format_perfor(perfor):
    return perfor.ljust(9)[:9]


# Generic format
def format(val, size):
    return val.ljust(size)[:size]


def format_lat_11(lat):
    return lat.ljust(11)[:11]


def format_dtype(dtype):
    return dtype.ljust(1)[:1]


def format_mb(mb):
    return mb.ljust(7)[:7]


def format_mbid(mbid):
    return mbid.ljust(9)[:9]


def format_ms(ms):
    return ms.ljust(7)[:7]


def format_msid(msid):
    return msid.ljust(9)[:9]


def format_ml(ml):
    return ml.ljust(7)[:7]


def format_mlid(mlid):
    return mlid.ljust(9)[:9]


def format_algor(algor):
    return algor.ljust(15)[:15]


def format_sxx(sxx):
    return sxx.ljust(15)[:15]


def format_syy(syy):
    return syy.ljust(15)[:15]


def format_szz(szz):
    return szz.ljust(15)[:15]


def format_stt(stt):
    return stt.ljust(15)[:15]


def format_sxy(sxy):
    return sxy.ljust(15)[:15]


def format_sxz(sxz):
    return sxz.ljust(15)[:15]


def format_sxt(sxt):
    return sxt.ljust(15)[:15]


def format_syz(syz):
    return syz.ljust(15)[:15]


def format_syt(syt):
    return syt.ljust(15)[:15]


def format_szt(szt):
    return szt.ljust(15)[:15]


def format_sdobs(stdDevObservation):
    return stdDevObservation.ljust(9)[:9]


def format_smajax(majorAxis):
    return majorAxis.ljust(9)[:9]


def format_sminax(minorAxis):
    return minorAxis.ljust(9)[:9]


def format_strike(majorAxisTrend):
    return majorAxisTrend.ljust(6)[:6]


def format_depth(depthKm):
    return depthKm.ljust(9)[:9]


def format_orid(orid):
    return orid.ljust(9)[:9]


def format_grn(grn):
    return grn.ljust(8)[:8]


def format_srn(srn):
    return srn.ljust(8)[:8]


def format_etype(etype):
    return etype.ljust(7)[:7]


def format_depdp(depdp):
    return depdp.ljust(9)[:9]


def format_sdepth(depthUncertainty):
    return depthUncertainty.ljust(9)[:9]


def format_stime(timeUncertainty):
    return timeUncertainty.ljust(6)[:6]


def format_conf(confidenceLevel):
    return confidenceLevel.ljust(5)[:5]


def format_prefor(preferredEventHypId):
    return preferredEventHypId.ljust(8)[:8]


def format_nass(nass):
    return nass.ljust(4)[:4]


def format_ndef(ndef):
    return ndef.ljust(4)[:4]


def format_ndp(ndp):
    return ndp.ljust(4)[:4]


def format_phase(phase):
    return phase.ljust(8)[:8]


def format_seaz(receiverToSourceAz):
    return receiverToSourceAz.ljust(7)[:7]


def format_esaz(sourceToReceiverAz):
    return sourceToReceiverAz.ljust(7)[:7]


def format_timeres(timeRes):
    return timeRes.ljust(8)[:8]


def format_timedef(timeDef):
    return timeDef.ljust(1)[:1]


def format_azres(azres):
    return azres.ljust(7)[:7]


def format_azdef(azdef):
    return azdef.ljust(1)[:1]


def format_slores(slores):
    return slores.ljust(7)[:7]


def format_slodef(slodef):
    return slodef.ljust(1)[:1]


def format_emares(emares):
    return emares.ljust(7)[:7]


def format_wqt(wqt):
    return wqt.ljust(6)[:6]


def format_vmodel(vmodel):
    return vmodel.ljust(15)[:15]


'''
1 space
'''


def gap():
    return ' '
