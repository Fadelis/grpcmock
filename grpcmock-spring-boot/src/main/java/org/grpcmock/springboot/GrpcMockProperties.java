package org.grpcmock.springboot;

import io.grpc.ServerInterceptor;
import java.util.Arrays;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Fadelis
 */
@ConfigurationProperties("grpcmock")
public class GrpcMockProperties {

  private Server server = new Server();

  public Server getServer() {
    return this.server;
  }

  public void setServer(Server server) {
    this.server = server;
  }

  public static class Server {

    private int port = 0;
    private Class<? extends ServerInterceptor>[] interceptors = new Class[0];
    private int executorThreadCount = 1;
    private String executorBeanName;
    private String certChainFile;
    private String privateKeyFile;
    private boolean portDynamic = false;

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public Class<? extends ServerInterceptor>[] getInterceptors() {
      return Arrays.copyOf(this.interceptors, this.interceptors.length);
    }

    public void setInterceptors(Class<? extends ServerInterceptor>[] interceptors) {
      this.interceptors = Arrays.copyOf(interceptors, interceptors.length);
    }

    public int getExecutorThreadCount() {
      return executorThreadCount;
    }

    public void setExecutorThreadCount(int executorThreadCount) {
      this.executorThreadCount = executorThreadCount;
    }

    public String getExecutorBeanName() {
      return executorBeanName;
    }

    public void setExecutorBeanName(String executorBeanName) {
      this.executorBeanName = executorBeanName;
    }

    public String getCertChainFile() {
      return certChainFile;
    }

    public void setCertChainFile(String certChainFile) {
      this.certChainFile = certChainFile;
    }

    public String getPrivateKeyFile() {
      return privateKeyFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
      this.privateKeyFile = privateKeyFile;
    }

    public boolean isPortDynamic() {
      return portDynamic;
    }

    public void setPortDynamic(boolean portDynamic) {
      this.portDynamic = portDynamic;
    }
  }
}
