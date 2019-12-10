# Using the test data set create/upload scripts

These scripts provide the ability to build the test data set as a set of JSON files, and to upload that group of JSON files to the appropriate services for storage into GMS.

## The input data to create the test data set

See GMS wiki: Standard Test Data Set

## Creating a test data set

`create-test-data-set.sh` is a Bash script with one required argument indicating where the files used to create the data set are.  
It writes the output JSON files of the data set to a directory 'gms_test_data_set' under the input directory.  

Example usage:

```bash
./create-test-data-set.sh /some/directory
# writes files to /some/directory/gms_test_data_set/
```

The script builds the test data set by running various applications with `gradle` and passing arguments to those invocations 
that make them write their data to the specified output directory.  The script looks for environment variable `GMS_HOME`
to know where to run these gradle commands from (the top-level of the java code in the repo); if it isn't set, the script
will try running them from a location relative to the script itself (e.g. `../../../../`).

Running the script elsewhere from where it currently resides can be done like so:
```bash
GMS_HOME=/<your_path>/gms-common/java ./create-test-data-set.sh /<your_path>/standard_test_data_set
```

## Uploading a test data set to the OSD/databases

`upload-test-data-set.sh` is a Bash script with three required arguments: the directory with the GMS JSON files, the directory with the FKSpectra files, the directory with the binary waveform (.w) files.
It curls these files to particular GMS/OSD service endpoints for storage; it does require Gradle to run as loading `ChannelSegment`'s is done with a Java program.  
This script requires three environment variables to be set which indicate the URL's of COI services.  For local use, these can be set to something like `localhost:8080`.  The URL can omit port number if it's the standard HTTP port of 80.
  - `SD_URL` indicates the url of the signal detection COI service
  - `STATION_REF_URL` indicates the url of the station reference COI service
  - `WAVEFORMS_URL` indicates the url of the waveforms COI service

Example usage:
```bash
SD_URL=localhost:8080 STATION_REF_URL=localhost:8081 WAVEFORMS_URL=localhost:8082 ./upload-test-data-set.sh /<your_path>/standard_test_data_set/gms_test_data_set /<your_path>/standard_test_data_set/FkSpectra/ChanSeg /<your_path>/standard_test_data_set/w

```

### Individual loader scripts

`upload-test-data-set.sh` does it's work by calling several other small scripts that each load an individual file or related set of files to a COI service.  These are broken out like this for re-use and simplicity.  These scripts are:

  - `upload-channel-segments.sh`: takes two arguments and requires environment variable `WAVEFORMS_URL` to be set: 
    - a directory of JSON files with name containing 'segment' and each containing a `ChannelSegment[]` in JSON format 
    - a directory of the binary waveform (.w) files
  - `upload-events.sh`: takes one argument of a JSON file containing an `Event[]` and requires environment variable `SD_URL` to be set
  - `upload-fk.sh`: takes one argument of a directory containing FK spectra JSON files, requires environment variable `SD_URL` to be set
  - **TODO: add `upload-beam-definitions.sh` in here when complete - there is currently no storage endpoint for beams
  - `upload-qc-masks.sh` takes one argument of a JSON file containing `QcMask[]` and requires environment variable `SD_URL` to be set
  - `upload-signal-detections.sh` takes one argument of a JSON file containing `SignalDetection[]` and requires environment variable `SD_URL` to be set
  - `upload-station-reference.sh` requires environment variable `STATION_REF_URL` to be set and takes one argument of a directory which must contain files:
    - `channel.json`, a JSON file containing `ReferenceChannel[]`
    - `network.json`, a JSON file containing `ReferenceNetwork[]`
    - `network-memberships.json`, a JSON file containing `ReferenceNetworkMembership[]`
    - `site.json`, a JSON file containing `ReferenceSite[]`
    - `site-memberships.json`, a JSON file containing `ReferenceSiteMembership[]`
    - `station.json`, a JSON file containing `ReferenceStation[]`
    - `station-memberships.json`, a JSON file containing `ReferenceStationMembership[]`
