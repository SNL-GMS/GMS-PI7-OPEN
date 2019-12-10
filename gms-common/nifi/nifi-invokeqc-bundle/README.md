# Overview
This project contains a nifi processor for invoking a control class for a given
time range and channel ids. It builds a Nifi Application Resource (NAR) file
to be included in a deployment of nifi in order for it to be available as a
processor in a sequence.

**NOTE:** Nifi currently only supports maven with plugins necessary to generate
a NAR file.