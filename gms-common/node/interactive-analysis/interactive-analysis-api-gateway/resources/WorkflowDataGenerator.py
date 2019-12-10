import json
import argparse
import uuid
from pprint import pprint
from random import *

parser = argparse.ArgumentParser()
parser.add_argument("workflowFile", help="Path to the complete workflow JSON file")
parser.add_argument("workflowFileOut", help="Path to write data to")
args = parser.parse_args()

with open(args.workflowFile) as jsonFile:
    workflow = json.loads(jsonFile.read())
jsonFile.close()

workflowData = workflow[0]
i = 100
for activityInterval in workflowData['activityIntervals']:
    newActivityId = str(uuid.uuid4())

    for stageInterval in workflowData['stageIntervals']:
        newStageId = str(uuid.uuid4())
        for index, value in enumerate(stageInterval['activityIntervalIds']):
            if value == activityInterval['id']:
                stageInterval['activityIntervalIds'][index] = newActivityId
    activityInterval['id'] = newActivityId

for stageInterval in workflowData['stageIntervals']:
    newId = str(uuid.uuid4())
    for interval in workflowData['intervals']:
        for index, value in enumerate(interval['stageIntervalIds']):
            if value == stageInterval['id']:
                interval['stageIntervalIds'][index] = newId
    
    for activityInterval in workflowData['activityIntervals']:
        if activityInterval['stageIntervalId'] == stageInterval['id']:
            activityInterval['stageIntervalId'] = newId
    stageInterval['id'] = newId

#Write the file out again
with open(args.workflowFileOut, 'w') as file:
    file.write(json.dumps(workflow))
file.close() 
