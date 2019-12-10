import sys

from gmsconfig.EtcdSystemConfigRepository import EtcdSystemConfigRepository
from gmsconfig.FileSystemConfigRepository import FileSystemConfigRepository

class GmsSystemConfig:
    """
    The GmsSystemConfig class provides an interface for accessing and
    managing GMS system configuration values.

    System configuraton key/value pairs are housed in a central system
    configuration repository (implemented with etcd).

    Keynames may be prefixed with dot-separated terms to provide
    scope. The value of the more-specific keyname will be preferred
    over the value of a more-generic keyname on retrieval. For
    example, looking for 'detector.port' will return the value of
    'detector.port' if present, otherwise it will fall back to the
    more generic keyname of 'port'.

    If an overrides file is present in the home directory, values from that
    file will be preferred over the central repository.  This
    file must be in the $HOME directory and be named
    configuration-overrides.properties.
    
    Methods that update system configuration only apply to the
    central repository.
    """

    def __init__(self, endpoints=None, username=None, password=None, verbose=False):
        self.central_repo = EtcdSystemConfigRepository(endpoints, username, password, verbose)
        self.overrides_file_repo = FileSystemConfigRepository(verbose=verbose)
        self.repos = [self.central_repo, self.overrides_file_repo]

    def get(self, key):
        """
        Get a value from GMS system configuration for a given key.

        Keynames may be prefixed with dot-separated terms to provide
        scope. The value of the more-specific keyname will be preferred
        over the value of a more-generic keyname on retrieval. For
        example, looking for 'detector.port' will return the value of
        'detector.port' if present, otherwise it will fall back to the
        more generic keyname of 'port'.

        If a key is overridden via the
        configuration-overrides.properties file, in overridden value
        will be returned instead of the value from the central
        repository.
        """
        def trim_parent(key):
            """
            Trim the immediate parent of a given, dot-separated key name.
            For example, for the key 'a.b.c.key' trimming the parent would
            remove the 'c' and return 'a.b.key'.
            """
            return '.'.join(key.split('.')[:-2] + key.split('.')[-1:])

        for repo in reversed(self.repos):
            searchKey = key
            value = repo.get(searchKey)
            while not value and '.' in searchKey:
                searchKey = trim_parent(searchKey)
                value = repo.get(searchKey)
            if value:
                return value
            
        return value

    def export(self):
        """
        Return a dictionary with all key/value pairs.

        If a key is overridden via the
        configuration-overrides.properties file, in overridden value
        will be returned in the dictionary instead of the value from
        the central repository.
        """
        values = {}

        # get all the unique keys across the central repo and overrides file
        unique_keys = set([key for d in filter(None, [self.central_repo.export(), self.overrides_file_repo.export()]) for key in d.keys()])

        # look up each key individually to ensure we get the appropriately overridden value
        for key in unique_keys:
            values[key] = self.get(key)
            
        return values
    

    def central_get(self, key):
        """
        Get a value from GMS central system configuration repository for a specific key.
        Ignore overrides and keyname scoping.
        """
        return self.central_repo.get(key)

    def central_set(self, key, value):
        """
        Set a key to given value in the GMS central system configuration repository.
        This will not affect an overridden value.
        """
        return self.central_repo.set(key, value)

    def central_delete(self, key):
        """
        Delete a key from the GMS central system configuration repository.
        This will not affect overridden keys.
        """
        return self.central_repo.delete(key)
    
    def central_load(self, config, clear=False):
        """
        Load the key/values from the provided dictionary into the central repository.

        All currently existing values in the central repository can be
        optionally cleared before loading the new values.

        All values currently in the central repository could be cleared.
        """
        return self.central_repo.load(config, clear)

    def __str__(self):
        return str(export())
    




        
