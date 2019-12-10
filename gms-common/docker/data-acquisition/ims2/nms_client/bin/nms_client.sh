#!/bin/sh
#set -x

# activate debug
#export NMS_CLIENT_DEBUG="TRUE"


#################################
## nms_client necessary information
#################################
echo "INFO - ******************** Check Env variables ********************"

#default python bin
DEFAULT_PYTHON_BIN=python

# default NMS_CLI_HOME
DEFAULT_NMS_CLI_HOME=..

if [ -z "$NMS_CLI_HOME" ]; then
   echo "WARNING - NMS_CLI_HOME was not set, setting it to the default: $DEFAULT_NMS_CLI_HOME"
   export NMS_CLI_HOME=$DEFAULT_NMS_CLI_HOME
fi

# build the absolute path
D=`dirname "$NMS_CLI_HOME"`
B=`basename "$NMS_CLI_HOME"`
NMS_CLI_HOME="`cd \"$D\" 2>/dev/null && pwd || echo \"$D\"`/$B"

DEFAULT_CONF_FILE=$NMS_CLI_HOME/conf/nms_client.conf

DEFAULT_NMS_USER_DIR=$HOME/.nms_client


#################################
## Apply Default Settings if globally not defined
#################################
if [ -z "$PYTHON_BIN" ]; then
   echo "INFO - PYTHON_BIN is undefined. I am setting it to default=($DEFAULT_PYTHON_BIN)."
   PYTHON_BIN=$DEFAULT_PYTHON_BIN
fi

if [ -z "$NMS_USER_DIR" ]; then
   echo "INFO - NMS_USER_DIR is undefined. I am setting it to default=($DEFAULT_NMS_USER_DIR)."
   export NMS_USER_DIR=$DEFAULT_NMS_USER_DIR
fi

if [ ! -z "$CONF_FILE" ]; then
    echo "WARNING: CONF_FILE is not empty, temporarily overwriting it"
fi
echo "INFO - Setting CONF_FILE to: $DEFAULT_CONF_FILE"
export CONF_FILE=$DEFAULT_CONF_FILE

if [ ! -d "$NMS_CLI_HOME/lib" ]; then
   echo "ERROR - Cannot find lib directory under $NMS_CLI_HOME"
   echo "Please check your environment variable NMS_CLI_HOME is correctly set. It should point to the nms client directory"
   exit 1
fi

if [ ! $(ls $NMS_CLI_HOME/lib/NMSClient-*.egg) ]; then
   echo "ERROR - Cannot find the NMS client under the lib directory: $NMS_CLI_HOME/lib."
   echo "Please check your environment variable NMS_CLI_HOME is correctly set. It should point to the nms client directory"
   exit 1
fi

## Set the PYTHONPATH
## Add lib dir 
STARTERPATH="$NMS_CLI_HOME/lib"
## Add all eggs in PYTHONPATH
for n in `find $NMS_CLI_HOME/lib/*`; do
      STARTERPATH=$STARTERPATH":"$n
done

export PYTHONPATH=$STARTERPATH:$PYTHONPATH 

###################################
## create bootstrap and launch program
## the bootstrap is always under 
## /tmp/nms_client.bootstrap.$$
###################################


# keep args in variables
ARGS="$@"

cat > /tmp/nms_client.bootstrap.$$ << EOF
import nms_client.batch.batchclient
nms_client.batch.batchclient.bootstrap_run()
EOF

echo "INFO - *************************************************************"
echo ""

$PYTHON_BIN /tmp/nms_client.bootstrap.$$ $ARGS
res="$?"

rm -f /tmp/nms_client.bootstrap.$$

exit $res

