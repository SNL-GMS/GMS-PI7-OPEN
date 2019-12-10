#!/bin/bash

./gen_mailrc.sh

/usr/lib/sendmail -bD -X /proc/self/fd/1 > /dev/null 2>&1 &

./email-if-not-storing-data.sh

