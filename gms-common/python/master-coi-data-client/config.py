import yaml


def get_config():
    """
    Load the configuration variables for the client.
    """
    with open('config.yml') as f:
        config = yaml.load(f)
    f.close()

    STATION_REFERENCE_SERVICE_SUFFIX = config['STATION_REFERENCE_SERVICE_SUFFIX']
    WAVEFORMS_SERVICE_SUFFIX = config['WAVEFORMS_SERVICE_SUFFIX']
    SIGNAL_DETECTION_SERVICE_SUFFIX = config['SIGNAL_DETECTION_SERVICE_SUFFIX']
    return STATION_REFERENCE_SERVICE_SUFFIX, \
           WAVEFORMS_SERVICE_SUFFIX, SIGNAL_DETECTION_SERVICE_SUFFIX



def station_reference_service_suffix():
    with open('config.yml') as f:
        config = yaml.load(f)
    f.close()
    return config['STATION_REFERENCE_SERVICE_SUFFIX']


def waveforms_service_suffix():
    with open('config.yml') as f:
        config = yaml.load(f)
    f.close()
    return config['WAVEFORMS_SERVICE_SUFFIX']


def signal_detection_service_suffix():
    with open('config.yml') as f:
        config = yaml.load(f)
    f.close()
    return config['SIGNAL_DETECTION_SERVICE_SUFFIX']
