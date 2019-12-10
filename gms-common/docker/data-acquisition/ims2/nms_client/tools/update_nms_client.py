#!/usr/bin/env python2.5
import os
from nms_install_utils import update_nms_client_distribution, get_nms_home_dir

os.environ["NMS_HOME"] = "/tmp/virtualenv/test-env"

nms_home_dir = get_nms_home_dir()

update_nms_client_distribution(nms_home_dir)
