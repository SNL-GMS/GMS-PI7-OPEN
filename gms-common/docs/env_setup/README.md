# Setup your Environment

1. Set the **GMS_STACK** variable:  
      Run `export GMS_STACK=<stack name>` where stack name represents your environment, prepended by "gms_" (i.e. gms_sandbox)
2. Set the **VERSION** and **DOCKERTREE_VERSION** variables with the version of the software you're deploying    
      Run `export VERSION=<version>` and `export DOCKERTREE_VERSION=<docker tree version>`  
      **Example** *VERSION* : 
        8.5.0 *or latest*
      
      **Example** *DOCKERTREE_VERSION* :
        8.5.0 *or latest*
        
3. Set the **CI_DOCKER_REGISTRY** variable:
      Run `export CI_DOCKER_REGISTRY=<docker-registry>` where your docker environment (ie. localhost)
4. In a terminal window, depending on how you obtained the docker-compose files, do either of the following:  
    a) If you got a docker-compose tarball, run `tar -xvf <gms-docker-compose-files>.tar.gz` and navigate to the folder you untarred.  
    b) If you have the GMS source code, navigate to ${GMS_INSTALL_DIR}/gms-common/docker-compose-swarm and find the compose files there.
    c) copy the template-env.sh to release-env.sh
5. Open up `release-env.sh` in a text editor
6. Override the following variables with the proper settings pertaining to your specific environment.  
    **Note:** A single-network scenario is when the data acquisition services and the GMS services reside on the same network. A
    multi-network scenario is when the two sets of services are on different networks.  
    **IMPORTANT:** If in a single-network scenario, it is vital that you leave the multi-network variables declared, if the variables exist, in the file you are editing and
    that you make them empty strings (i.e. `CD11_RSYNC_JUMP_HOSTS: `)
  * Single-network:  
     **BASE_DOMAIN:** represents the base domain of the wildcard DNS you have configured (i.e. foo.bar.com, localhost)  
     **CI_DOCKER_REGISTRY:** represents the docker registry from which the images came from (i.e. localhost)  
     **COLLECTION:** represents the collection that Docker UCP (web) uses to group the docker objects together in the UI  (leave as is)
     **SUBDOMAIN:** represents the subdomain which is prepended to the BASE_DOMAIN when accessing services externally (i.e. sandbox, release)  
  * Multi-network:  
     **CD11_RSYNC_JUMP_HOSTS:** represents the jump host the cd11 rsync service needs  
     **CD11_RSYNC_REMOTE_HOST:** represents the remote host from which the cd11 data is sent  
     **SEEDLINK_RSYNC_JUMP_HOSTS:** represents the jump host the seedlink service needs  
     **SEEDLINK_RSYNC_REMOTE_HOST:** represents the remote host from which the seedlink data is sent  
7. After saving the file, source the variables by running `source release-env.sh`
