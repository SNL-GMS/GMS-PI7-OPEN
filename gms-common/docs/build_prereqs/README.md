# Build Prerequisites

## Build Server Pre-requisites
In order to Build GMS, you will need to have a minimum server requirement.
**Note:** the build requires a working internet connection.

[Docker CE : Server Requirements](../docs/deploy_prereqs/docker_ce/#Hardware Requirements)
[Docker EE : Server Requirements](../docs/deploy_prereqs/docker_ee/#Hardware Requirements)

In order to Build GMS, you will need:

* Centos7 or Ubuntu 18.04
* Docker 18.09 or higher
* Docker-compose 1.24.0 or higher
* gradle 4.10
* java 9 (Note the GMS code base may not build with Java 11, java 9 is a
  requirement rather than a suggested lower bound)
* python3.6 (Note python 2 will not work)
* maven (mvn 3.6.0)
* node 8 or node 10 (Note node 11 will not work)
* Chrome (version 75.+)
* Repos:
  1. gms-common
  1. docker-tree
  1. xdc_golden_layout

## Ensure Credentials
Ensure that the user that is performing the build server installation has the appropriate permissions, such as being able to run "sudo" commands. 
This typically is done by adding the user to the sudoers list by running the `visudo` command.

## Install Chrome
In order to exercise the full functionality of GMS, you will need to load Chrome on your machine.
 [Download Chrome](https://www.google.com/chrome/)

## Required Repos
In order to Build GMS, please colocate the 3 repos (listed above) into one directory.  In order to simplify instructions we will now set a "GMS_INSTALL_DIR"
environment variable that is the full path to that directory.

For example :
    `export GMS_INSTALL_DIR=/usr/jdoe/gms/install/`

Change to the full path on your machine to this folder.
    `cd $GMS_INSTALL_DIR`

## Centos7

#### Installing Docker
1. `sudo yum update`
1. `sudo yum -y install yum-utils device-mapper-persistent-data lvm2 wget unzip bzip2`
1. `sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo`
1. `sudo yum -y install docker-ce`
1. `sudo systemctl enable docker`
1. `sudo systemctl start docker`
1. `sudo usermod -aG docker $USER`

##### Verify Docker Version 
1. `docker --version`

#### Installing Docker-compose
1. `sudo curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose`
1. `sudo chmod +x /usr/local/bin/docker-compose`

##### Verify Docker-compose Version 
1. `docker-compose --version`

#### Installing Java
1. `wget https://download.java.net/java/GA/jdk9/9.0.4/binaries/openjdk-9.0.4_linux-x64_bin.tar.gz`
1. `sudo tar xf openjdk-9.0.4_linux-x64_bin.tar.gz -C /opt`
1. `rm -f openjdk-9.0.4_linux-x64_bin.tar.gz`
1. `sudo ln -s /opt/jdk-9.0.4/bin/java /usr/local/bin`
1. `sudo ln -s /opt/jdk-9.0.4/bin/javac /usr/local/bin`

##### Verify Java Version 
1. `java --version`

#### Installing Gradle
1. `wget https://services.gradle.org/distributions/gradle-4.10-bin.zip`
1. `sudo unzip -d /opt gradle-4.10-bin.zip`
1. `rm -f gradle-4.10-bin.zip`
1. `sudo ln -s /opt/gradle-4.10/bin/gradle /usr/local/bin`

##### Verify Gradle Version 
1. `gradle --version`

#### Installing Python
1. `sudo yum install -y https://centos7.iuscommunity.org/ius-release.rpm`
1. `sudo yum update -y`
1. `sudo yum install -y python36u python36u-libs python36u-devel python36-setuptools`
1. `sudo ln -s /usr/bin/python3.6 /usr/local/bin/python`
1. `sudo ln -s /usr/bin/python3.6 /usr/local/bin/python3`

##### Verify Python Version 
1. `python3 --version`

#### Installing Maven
1. `curl -OL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.6.0/apache-maven-3.6.0-bin.tar.gz`
1. `sudo tar -xzf apache-maven-3.6.0-bin.tar.gz -C /opt`
1. `rm apache-maven-3.6.0-bin.tar.gz`
1. `sudo ln -s /opt/apache-maven-3.6.0 /opt/maven`
1. `sudo vi /etc/profile.d/maven.sh`
1. add the following:
   ```
   # Apache Maven Environmental Variables
   export JAVA_HOME=/opt/jdk-9.0.4
   export M2_HOME=/opt/maven
   export MAVEN_HOME=/opt/maven
   export PATH=${M2_HOME}/bin:${PATH}
   ```
1. `sudo chmod +x /etc/profile.d/maven.sh`
1. `source /etc/profile.d/maven.sh`

##### Verify Maven Version 
1. `mvn --version`

#### Installing Node
1. `curl -sL https://rpm.nodesource.com/setup_10.x | sudo bash -`
1. `sudo yum clean all && sudo yum makecache fast`
1. `sudo yum install -y gcc-c++ make`
1. `sudo yum install -y nodejs`

##### Verify Node Version 
1. `node --version`

## Ubuntu 18.04 LTS

#### Installing Docker
1. `sudo apt -y install apt-transport-https ca-certificates curl software-properties-common wget unzip bzip2`
1. `curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add --`
1. `sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"`
1. `sudo apt update`
1. `sudo apt -y install docker-ce`
1. `sudo usermod -aG docker $USER`

##### Verify Docker Version 
1. `docker --version`

#### Installing Docker-compose
1. `sudo curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose`
1. `sudo chmod +x /usr/local/bin/docker-compose`

##### Verify Docker-compose Version 
1. `docker-compose --version`

#### Installing Java
1. `wget https://download.java.net/java/GA/jdk9/9.0.4/binaries/openjdk-9.0.4_linux-x64_bin.tar.gz`
1. `sudo tar xf openjdk-9.0.4_linux-x64_bin.tar.gz -C /opt`
1. `rm -f openjdk-9.0.4_linux-x64_bin.tar.gz`
1. `sudo ln -s /opt/jdk-9.0.4/bin/java /usr/local/bin`
1. `sudo ln -s /opt/jdk-9.0.4/bin/javac /usr/local/bin`

##### Verify Java Version
1. `java --version`

#### Installing Gradle
1. `wget https://services.gradle.org/distributions/gradle-4.10-bin.zip`
1. `sudo unzip -d /opt gradle-4.10-bin.zip`
1. `rm -f gradle-4.10-bin.zip`
1. `sudo ln -s /opt/gradle-4.10/bin/gradle /usr/local/bin`

##### Verify Gradle Version
1. `gradle --version`

#### Installing Python
Python 3.6 is included in Ubuntu 18.04 by default. Only the setuptools package needs to be installed:
* `sudo apt -y install python3-setuptools`

##### Verify Python Version 
1. `python --version`

#### Installing Maven
1. `curl -OL https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.6.0/apache-maven-3.6.0-bin.tar.gz`
1. `sudo tar -xzf apache-maven-3.6.0-bin.tar.gz -C /opt`
1. `rm apache-maven-3.6.0-bin.tar.gz`
1. `sudo ln -s /opt/apache-maven-3.6.0 /opt/maven`
1. `sudo vi /etc/profile.d/maven.sh`
1. add the following:
   ```
   # Apache Maven Environmental Variables
   export JAVA_HOME=/opt/jdk-9.0.4
   export M2_HOME=/opt/maven
   export MAVEN_HOME=/opt/maven
   export PATH=${M2_HOME}/bin:${PATH}
   ```
1. `sudo chmod +x /etc/profile.d/maven.sh`
1. `source /etc/profile.d/maven.sh`

##### Verify Maven Version
1. `mvn --version`

#### Installing Node
1. `curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -`
1. `sudo apt -y install gcc g++ make`
1. `sudo apt -y install nodejs`

##### Verify Node Version 
1. `node --version`
