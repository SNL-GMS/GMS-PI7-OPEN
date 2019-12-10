package gms.shared.frameworks.systemconfig;

import com.ibm.etcd.client.EtcdClient;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdClientManager {

  private static final Logger logger = LoggerFactory.getLogger(EtcdClientManager.class);

  private KvClient keyValueClient;
  private final String endpoints;
  private final String username;
  private final String password;

  EtcdClientManager(String endpoints, String username, String password) {
    this.endpoints = endpoints;
    this.username = username;
    this.password = password;
  }

  KvClient getEtcdClient() {
    if (this.keyValueClient == null) {
      this.keyValueClient = initializeEtcdClient();
    }
    return this.keyValueClient;
  }

  private KvClient initializeEtcdClient() {
    // Note: IBM etcd-java will always return an Etcdclient, even if it can't connect.
    EtcdClient.Builder etcdBuilder = EtcdClient.forEndpoints(endpoints).withPlainText();
    if (null != username && null != password) {
      etcdBuilder = etcdBuilder.withCredentials(username, password);
    }
    KvStoreClient etcdClient = etcdBuilder.build();
    final KvClient client = etcdClient.getKvClient();
    logger.info("Created etcd connection with endpoints '{}'", endpoints);
    return client;
  }

  @Override
  public String toString() {
    return String.format("Etcd: endpoints=%s", this.endpoints);
  }

}
