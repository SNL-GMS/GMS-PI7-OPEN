from jproperties import Properties

import os

class FileSystemConfigRepository:
    "File GMS System Configuration Repository"

    def __init__(self, filename="$HOME/configuration-overrides.properties", verbose=False):
        self.verbose = verbose
        self.filename = os.path.expandvars(filename)
        try:
            self.properties = Properties()
            with open(self.filename, "rb") as f:
                self.properties.load(f, "utf-8")
        except Exception as e:
            if self.verbose:
                print(f"Overrides '{filename}' not present...")
            self.properties = None

            
    def get(self, key):
        "Return the value from the file for the specified key, or None if key is not present."
        if not self.properties:
            return None
            
        if self.verbose:
            print(f"... [file override] looking for {key}")
            
        if key in self.properties:
            value = self.properties[key].data
            if self.verbose:
                print(f"... [file override] found {key} = {value}")
            return value
        else:
            return None
        

    def export(self):
        "Export key-value pairs from the file to a dictionary"
        if not self.properties:
            return {}
        
        values = {}
        for key in self.properties:
            values[key] = self.properties[key].data
            if self.verbose:
                print(f"... [file override] found '{key}' = '{values[key]}'")
        return values
