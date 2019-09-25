package org.grpcmock.springboot;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.grpcmock.GrpcMock;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.context.annotation.Import;

/**
 * Annotation for test classes that want to start a gRPC Mock server as part of the Spring
 * Application Context.
 *
 * @author Fadelis
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(GrpcMockConfiguration.class)
@PropertyMapping("grpcmock.server")
@Inherited
public @interface AutoConfigureGrpcMock {

  /**
   * Defines the port value for the gRPC Mock server. If set to <code>0</code> a random free port
   * will be picked.
   */
  int port() default GrpcMock.DEFAULT_PORT;

  /**
   * Defines {@link ServerInterceptor} for the gRPC Mock server. Interceptors defined here must have
   * a default constructor without any arguments.
   */
  Class<? extends ServerInterceptor>[] interceptors() default {};

  /**
   * <p>Defines executor thread count to be used for the server, which will create a {@link
   * Executor} via {@link Executors#newFixedThreadPool(int)}.
   * <p>If {@link #executorBeanName()} is defined it will take priority over this. If none of these
   * are defined a default {@link Executor} will be used from {@link Server}.
   */
  int executorThreadCount() default -1;

  /**
   * <p>Defines executor bean to be used for the gRPC server.
   * <p>This will take priority over {@link #executorThreadCount()}. If none of these
   * are defined a default {@link Executor} will be used from {@link ServerBuilder}.
   */
  String executorBeanName() default "";

  /**
   * Defines the file path for the cert chain. Both this and {@link #privateKeyFile()} must be
   * defined in order to configure server security via {@link ServerBuilder#useTransportSecurity}.
   */
  String certChainFile() default "";

  /**
   * Defines the file path for the private key. Both this and {@link #certChainFile()} must be
   * defined in order to configure server security via {@link ServerBuilder#useTransportSecurity}.
   */
  String privateKeyFile() default "";
}
