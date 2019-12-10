import time

from util.conversions import *

jdate_today = iso8601_to_jdate(epoch_to_iso(time.time())).ljust(8)[:8]
jdate = '-1'.ljust(8)[:8]
lddate = iso8601_to_regular_datetime(datetime.datetime.now().isoformat()).ljust(17)[
         :17]  # get the current date and time in ISO8601
NA_LONG_AND_LAT = '-999.0'  # NA values for latitude and longitude
INSTRUMENTPRIMARYKEYSTART = 601
CHANPRIMARYKEYSTART = 401
ORIDPRIMARYKEYSTART = 1000
EVIDPRIMARYKEYSTART = 10
ARIDPRIMARYKEYSTART = 6000

STATYPE_DEFAULT = '    '
NA_ENDTIME = '9999999999.999'  # NA value for endtime
NA_ENDTIME_POS = '+9999999999.999'
NA_GENERAL_NEG = '-1'
NA_GENERAL_POS = '1'
NA_DASH = '-'
NA_ZERO = '0'
NA_LOGAT = '-999.0'
NA_GENERAL_NEG_FLOAT = '-1.0'
LOCAL_ID_NAME = "_id"
NA_CONF = '0.9'
NA_DTYPE = 'g'


def make_row(args):
    return ' '.join(args)
