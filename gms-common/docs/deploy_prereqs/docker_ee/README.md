# Docker EE Deployment Prerequisites

These are the hardware and software requirements necessary to run GMS.

## Hardware Requirements

* Cluster of at least 3 machines (or VMs) with at least these specs each:
  * 12 CPU
  * 32GB
  * 600GB

## Software Requirements

* Centos 7.1, RHEL 7.4 or Ubuntu >= 18.04
* Docker EE >= 18.09

## Docker EE Installation

To install Docker EE, follow the Docker guide according to your linux distribution:

[Install Docker EE on **Centos**](https://docs.docker.com/install/linux/docker-ee/centos/)

[Install Docker EE on **RHEL**](https://docs.docker.com/install/linux/docker-ee/rhel/)

[Install Docker EE on **Ubuntu**](https://docs.docker.com/install/linux/docker-ee/ubuntu/)

### Post-Install

For linux installs, it is recommended that you review these [post-install instructions](https://docs.docker.com/install/linux/linux-postinstall/).
At a minimum, follow the "[Starting on Boot section](https://docs.docker.com/install/linux/linux-postinstall/#configure-docker-to-start-on-boot)".

## UCP

Docker Universal Control Plane (UCP) is the enterprise-grade cluster management
solution from Docker.

### Installation

Each cluster needs up to 1 manager node and up to 5 depending on cluster size
and redundancy requirements. Manager count should always be an odd number (1, 3,
5, etc.)

Installing UCP:

1. SSH into the machine where you wish to install UCP
2. Run the following command (substituting <ip_addr> with the IP of the target)
  * `docker container run --rm -it --name ucp -v /var/run/docker.sock:/var/run/docker.sock docker/ucp:latest install --host-address <IP-address> --interactive`
  * You will then be prompted for information:
    * Admin User: gmsadmin
    * Admin Pass: Something you will remember
    * Question about additional hostname: Leave blank
3. Once install finishes, you should be able to go to the http address.  You will need to force the browser to go to the address since the SSL cert is self signed currently (There is a step later on getting an SSL cert)
4. Login using the admin user and password
5. Add the license file.  This is either the GMS license from docker hub, or the free trial license.

### Configuring LDAP

Example configuration:

1. Setup LDAP
  1. Expand your name on the left menu → Admin Setting → Authentication & Authorization
  2. LDAP Enabled → Yes
  3. LDAP Server: ldap://<ldap_host>
  4. Reader DN: cn=<cn>,ou=local config,dc=<dc>
  5. Reader Password: <reader_pass>
  6. Just in time user provisioning: Checked
  7. Click Add LDAP User Search Configuration
  8. Base DN: ou=<OU>,dc=<DC1>,dc=<DC2>,dc=<DC3>
  9. Username Attribute: uid
  10. Full name Attriubute: cn
  11. Filter: (&(objectClass=person)(memberof=cn=<meta_group>,ou=Groups,ou=<OU>,dc=<DC1>,dc=<DC2>,dc=<DC3>))
  12. Search subtree instead of just one level: Checked
  13. Click Confirm
  14. Enter your SRN username and password and click Test.  It should be successful.
  15. LDAP Sync Interval: 6
  16. Enable sync of admin users: Checked
  17. Match Search Results
  18. Base DN: ou=<ou>,dc=<dc1>,dc=<dc2>,dc=<dc3>
  19. Search Filter: (&(objectClass=person)(memberof=cn=<meta_group>,ou=Groups,ou=<OU>,dc=<DC1>,dc=<DC2>,dc=<DC3>))
  20. Search subtree instead of just one level → Checked
  21. Click Save
  22. Click Sync Now and watch the logs
  23. You should be able to log out of the gmsadmin account, and log back into UCP as yourself.  Members of <meta_group> should be admins.

## Adding worker nodes

1. Login to UCP
2. Dashboard → Scroll down to bottom → Add Nodes
3. Select Linux & Worker
4. Copy the command shown
    The command will likely look like: *docker swarm join ...*
5. Log in via a terminal window to each additional worker node and execute the command copied from the previous step. 
    **Do not** run this step on the server where you installed UCP.
6. After a few minutes the worker will join and should show as Ready inside the UCP (Web)

## Generate SSL and add to UCP

Generate private key and certificate signing request (CSR)
**Note:** In the command shown below, change the <hostname> to be the name of your server

```
openssl req -new -newkey rsa:2048 -nodes -keyout <hostname>.key -out <hostname>.csr
```
**Definitions:**
*Private Key - The unencrypted private key of UCP. This key must correspond to the public key used in the server certificate

Server Certificate - The public key certificate of UCP followed by the certificates of any intermediate certificate authorities which establishes a chain of trust up to the root CA certificate

CA Certificate - The public key certificate of the root certificate authority that issued the UCP server certificate. If you don't have one, use the top-most intermediate certificate instead*

Get the CSR signed with your certificate authority.
**Note:** This is likely performed by your system administrator

Download the server certificate and intermediates.
* Copy the server certificate into a new file followed by the intermediates.
* Save as `<hostname>_bundle.pem`

Create a new cert file that has all the intermediates and Insert the contents of your new server cert at the end. And save as `CA.pem`.  **NOTE:** Ensure you do not have windows endlines in this file.

Navigate to the UCP Certificate Admin Screen (User Name → Admin Settings → Certificates)
* Paste in the contents of the files or use the upload file feature
  * Private Key: <hostname>.key
  * Server Certificate: `<hostname>_bundle.pem`
  * CA Certificate: `CA.pem`
* Click Save and UCP should be updated

Navigate to the HTTPS URL and check that the certificate is valid.

## Collections

Collections are groupings of resources that can be given access control restrictions.
**NOTE:** *Avoid the use of underscores in collection names as this may cause problems with URLs*

1. Go to Shared Resources → Collections → Swarm → View children
2. Create Collection
3. Create one for each testbed that will be deployed (i.e. Sandbox, Release, Validation)

## Node Constraints

Some of our services need to pinned to the same node in order to share storage space or to
gaurantee consistent volume configuration. To accomplish this, the following node constraint
groupings need to be applied to nodes of your choice.

1. In Docker EE, navigate to Shared Resources > Nodes
2. Click on the node that you'd like to make a constraint for
3. In the top right, click on the Edit Node button (gear icon)
4. At the bottom under Labels, click on "Add Label"
5. Once you are finished adding the labels, click Save at the bottom right

*\<env> is your environment name (i.e. "sandbox", "release")*  

**Node1**
```
<env>.zoo1 : true
<env>.zoo2 : true
<env>.zoo3 : true
<env>.cassandra : true
<env>.wiremock : true
```
**Node2**
```
<env>.nifi-data-acq : true
<env>.cd11-data-acq : true
```
**Node3**
```
<env>.nifi-registry : true
<env>.postgresql-stationreceiver : true
<env>.interactive-analysis-api-gateway : true
```

## Miscellaneous Settings

On the left side menu, expand your name:

* Admin Settings → Authentication & Authorization → Default Role For All Private Collections → View Only
* Admin Settings → Audit Logs → Metadata
* Admin Settings → Layer 7 → off
* Admin Settings → Scheduler → only "Allow administrators to deploy containers on UCP managers or nodes running DTR" checked
* Admin Settings → Swarm → Task History Limit = 2

## Wildcard DNS

Setting up a wildcard DNS is necessary to expose the docker host for user access and
send HTTP requests to the services using an easily formatted URL configured by traefik.
The alternative to a wildcard DNS solution would be to expose the ports in the docker-compose file. HTTP access to the services
would then be granted through `<host>:<port>` and traefik would be bypassed. Since traefik is deployed with the GMS system and comes
pre-configured, instructions here are for a wilcard DNS solution. For more help on how to configure a `<host>:<port>` solution in
docker-compose, follow [this Docker guide](https://docs.docker.com/compose/compose-file/#ports).

Using the example below, log in to your DNS server and create a new zone file:

#### Example zone file for wildcard DNS
```
$TTL 3600       ; 1 hour
@       IN      SOA     taurus.example.com. dnsadmin.example.com. (
                                2       ; serial
                                1800    ; refresh (30 minutes)
                                900     ; retry (15 minutes)
                                604800  ; expire (1 week)
                                3600    ; minimum (1 hour)
                                )
@               NS      ns3.example.com.
                NS      ns4.example.com.
```

#### Section to add to enable wildcard DNS
Change the below ips appropriately:
```
; zone name
                A       192.0.2.4
; wildcard
*               A       192.0.2.4
```
