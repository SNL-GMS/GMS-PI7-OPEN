import os, subprocess
from os.path import join
import shutil


def get_nms_projects_home_dir():
    nms_projects_home_dir = os.environ['NMS_PROJECTS_HOME']
    return __check_directory(nms_projects_home_dir)

def get_nms_home_dir():
    nms_home_dir = os.environ['NMS_HOME']
    return __check_directory(nms_home_dir)
        
def __check_directory(directory):
    try:
        if os.path.exists(directory) and os.path.isdir(directory):
            return directory
        else:
            raise Exception("No such Directory/Not a directory %s" % directory)
    except KeyError, ke:
        raise Exception("Environment variable NMS_HOME is not set")
    
def get_nms_client_dir():
    cur_dir = os.path.abspath(os.curdir)
    nms_server_dir = os.path.join(cur_dir[0:cur_dir.rfind("nms_client")], "nms_client")
    return nms_server_dir

def get_nms_client_lib_dir():
    nms_lib_dir = os.path.join(get_nms_client_dir(), "lib")
    return nms_lib_dir
            
def __install_distribution(distribution, home_dir, nms_lib_dir):
    """Install distribution and dependecies, using easy_install script from home_dir,
     and reading distributions from the nms_lib_dir directory."""
    print "installing %s" % distribution
    subprocess.call([join(home_dir, 'bin', 'easy_install'),
                 '-H', 'None', '-f', nms_lib_dir, distribution])

def __update_distribution(distribution, home_dir, nms_lib_dir):
    """Install distribution and dependecies, using easy_install script from home_dir,
     and reading distributions from the nms_lib_dir directory."""
    print "updating %s" % distribution
    subprocess.call([join(home_dir, 'bin', 'easy_install'),
                 '-H', 'None', '-U', '-f', nms_lib_dir, distribution])

def __force_upgrade_distribution(distribution,  home_dir, nms_lib_dir):
    """Install distribution and dependecies, using easy_install script from home_dir,
     and reading distributions from the nms_lib_dir directory."""
    print "updating %s" % distribution
    subprocess.call([join(home_dir, 'bin', 'easy_install'),
                 '-H', 'None', '-a', '-f', nms_lib_dir, distribution])
                       
def after_install(options, home_dir):
    update_nms_client_distribution(home_dir,'NMSClient')

def update_nms_local_apache(home_dir):
    nms_projects = get_nms_projects_home_dir()
    

def update_nms_client_distribution(home_dir, distribution='NMSClient'):
    nms_lib_dir = get_nms_client_lib_dir()
    print nms_lib_dir
    __force_upgrade_distribution(distribution, home_dir, nms_lib_dir)
