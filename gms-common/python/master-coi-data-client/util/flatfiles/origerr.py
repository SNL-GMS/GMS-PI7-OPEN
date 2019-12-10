from copy import deepcopy

from util.flatfiles.fieldFormatter import *
from util.flatfiles.shared import *

"""
 Write the CSS ORIGERR flat file.
 @:param events: a list of events
"""


def write_origerr(events):
    if not events:
        print('No event was provided, or the event had no associated hypotheses.')
        open('p3.origerr', 'w').close()
        return
    orid = deepcopy(ORIDPRIMARYKEYSTART)
    with open('p3.ORIGERR', 'w') as f:
        for e in events:
            orid += 1
            # setup NAs here
            row = ''
            oid = format_orid(str(orid))
            sxx = format_sxx(NA_GENERAL_NEG_FLOAT)
            syy = format_syy(NA_GENERAL_NEG_FLOAT)
            szz = format_szz(NA_GENERAL_NEG_FLOAT)
            stt = format_stt(NA_GENERAL_NEG_FLOAT)
            sxy = format_sxy(NA_GENERAL_NEG_FLOAT)
            sxz = format_sxz(NA_GENERAL_NEG_FLOAT)
            syz = format_syz(NA_GENERAL_NEG_FLOAT)
            stx = format_sxt(NA_GENERAL_NEG_FLOAT)
            sty = format_syt(NA_GENERAL_NEG_FLOAT)
            stz = format_szt(NA_GENERAL_NEG_FLOAT)
            sdobs = format_sdobs(NA_GENERAL_NEG_FLOAT)
            smajax = format_smajax(NA_GENERAL_NEG_FLOAT)
            sminax = format_sminax(NA_GENERAL_NEG_FLOAT)
            strike = format_strike(NA_GENERAL_NEG_FLOAT)
            sdepth = format_sdepth(NA_GENERAL_NEG_FLOAT)
            stime = format_stime(NA_GENERAL_NEG_FLOAT)
            conf = format_conf(NA_GENERAL_NEG_FLOAT)
            commid = format_commid(NA_GENERAL_NEG)
            if 'hypotheses' not in e:
                f.write(make_row([oid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax, strike,
                                  sdepth, stime, conf, commid, lddate]) + '\n')
                continue
            else:
                hypotheses = e.get('hypotheses')
                for h in hypotheses:
                    # make sure we have at least one locationSolution, or we use all NAs
                    if 'locationSolutions' in h and h.get('locationSolutions') is not None:
                        locationSolutions = h.get('locationSolutions')
                    else:
                        f.write(make_row(
                            [oid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax, strike,
                             sdepth, stime, conf, commid, lddate]) + '\n')
                        continue
                    for sol in locationSolutions:
                        # make sure we have at least one locationUncertainty, or we use NAs for locationUncertainty values
                        if 'locationUncertainty' in sol and sol.get('locationUncertainty') is not None:
                            # we found a locationUncertainty, get the values for the keys.
                            # if the keys don't exist or there are missing values, use the defaults.
                            lu = sol.get('locationUncertainty')
                            oid = format_orid(oid)
                            sxx = format_sxx(str(lu.get('xx', sxx)))
                            syy = format_syy(str(lu.get("yy", syy)))
                            szz = format_szz(str(lu.get("zz", szz)))
                            stt = format_stt(str(lu.get("tt", stt)))
                            sxy = format_sxy(str(lu.get("xy", sxy)))
                            sxz = format_sxz(str(lu.get("xz", sxz)))
                            syz = format_syz(str(lu.get("yz", syz)))
                            stx = format_sxt(str(lu.get("xt", stx)))
                            sty = format_syt(str(lu.get("yt", sty)))
                            stz = format_szt(str(lu.get("zt", stz)))
                            sdobs = format_sdobs(str(lu.get("stDevOneObservation", sdobs)))
                            # make sure we have at least one Ellipse, or we use NAs for Ellipse values
                        else:
                            f.write(make_row(
                                [oid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax, strike,
                                 sdepth, stime, conf, commid, lddate]) + '\n')
                            continue
                        if 'ellipses' in lu and lu.get('ellipses') is not None:
                            elp = lu.get("ellipses")
                            for el in elp:
                                # we found at least one Ellipse, get the values associated with the keys.
                                # if the keys don't exist or there are missing values, use the defaults.
                                smajax = format_smajax(str(el.get("majorAxisLength", smajax)))
                                sminax = format_sminax(str(el.get("minorAxisLength", sminax)))
                                strike = format_strike(str(el.get("majorAxisTrend", strike)))
                                sdepth = format_sdepth(str(el.get("depthUncertainty", sdepth)))
                                stime = format_stime(str(el.get("timeUncertainty", stime)))
                                conf = format_conf(str(el.get("confidenceLevel", conf)))
                                f.write(make_row(
                                    [oid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax,
                                     strike,
                                     sdepth, stime, conf, commid, lddate]) + '\n')
                        else:
                            f.write(make_row(
                                [oid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax, strike,
                                 sdepth, stime, conf, commid, lddate]) + '\n')
