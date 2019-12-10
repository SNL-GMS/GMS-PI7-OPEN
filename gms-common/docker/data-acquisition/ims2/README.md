# IMS 2.0 Docker image README

The IMS 2.0 Docker image contains the IDC's NMS client for requesting IMS 2.0 data. This is used to request data 
segements for the IMS auxiliary stations. 

The client has been wrapped in a Flask server that has an endpoint that accepts the following inputs:
* station
* start time to end time

Those inputs are formatted into a request file that is then sent to the IDC via the NMS client. 
The client writes the result of that request to a file. The endpoint reads that file's data and 
returns it. 

Why use a Flask Server? We are treating the NMS client as a black box and using it as is. Using Flask allows us to 
encapsulate all client set up in a single place and allow the NiFi that calls the Flask endpoint to handle the
configuration related to acquisition (e.g. calling frequency, start time, end time, station, SOH requests, etc.)

A Python based microframework was chosen over something similar in Java because this needs to run in a Docker 
container (again, for encapsulation of functionality), and the client requires Python. By using Flask, we can leverage
the Python installation that is already part of the Docker image being used for IMS 2.0 acquisition.

## Building Docker image
```
docker build --build-arg CTBTO_USER=<user> --build-arg CTBTO_PASS=<password> -t gms-son-registry.<domain name>/gms-common/ims2-client:latest .
```

## Running Docker container
This container will be part of the docker-compose environment on the SON, but the line below will start 
up the container for testing purposes.
```
docker run -d --name ims2-client -p 5000:5000 gms-son-registry.<domain name>/gms-common/ims2-client:latest
```
