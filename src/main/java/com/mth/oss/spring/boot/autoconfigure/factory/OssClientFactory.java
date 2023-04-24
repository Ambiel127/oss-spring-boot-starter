package com.mth.oss.spring.boot.autoconfigure.factory;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.mth.oss.spring.boot.autoconfigure.OssProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * oss 客户端单例工厂
 *
 * @author <a href="mailto:ambiel127@163.com">Matianhao</a>
 * @since 1.3
 */
@Component
public class OssClientFactory {

    private static volatile AmazonS3 awsClient;

    private OssClientFactory() {}

    @Bean
    public static AmazonS3 getAwsClient(OssProperties properties) {
        if (awsClient == null) {
            synchronized (OssClientFactory.class) {
                if (awsClient == null) {
                    ClientConfiguration clientConfiguration = new ClientConfiguration();

                    AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                            new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(),
                                                                       properties.getRegion());

                    AWSCredentials awsCredentials = new BasicAWSCredentials(properties.getAccessKeyId(),
                                                                            properties.getAccessKeySecret());
                    AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

                    awsClient = AmazonS3Client.builder()
                            .withPathStyleAccessEnabled(properties.getPathStyleAccess())
                            .withClientConfiguration(clientConfiguration)
                            .withEndpointConfiguration(endpointConfiguration)
                            .withCredentials(credentialsProvider)
                            .disableChunkedEncoding().build();
                }
            }
        }
        return awsClient;
    }

    @PostConstruct
    public void shutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            awsClient.shutdown();
            System.out.println("shut down oss client");
        }));
    }

}
