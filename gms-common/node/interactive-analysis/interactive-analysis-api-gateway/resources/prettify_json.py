import argparse
import json
import os

parser = argparse.ArgumentParser()
parser.add_argument("directoryToPrettify", help="Directory with the json files to prettify")
args = parser.parse_args()

files = os.listdir(args.directoryToPrettify)
print(len(files))
for directoryFile in files:
    if directoryFile.split('.')[-1] == 'json':
        # Found a JSON file
        with open(args.directoryToPrettify + directoryFile) as jsonfile:
            data = json.load(jsonfile)
        jsonfile.close()

        with open(args.directoryToPrettify + directoryFile, "w") as outFile:
            outFile.write(json.dumps(data, indent=3))
        outFile.close()
