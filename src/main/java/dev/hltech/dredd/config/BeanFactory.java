package dev.hltech.dredd.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import dev.hltech.dredd.domain.PactValidator;
import dev.hltech.dredd.domain.SwaggerValidator;
import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.KubernetesEnvironment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import dev.hltech.dredd.integration.pactbroker.PactBrokerClient;
import feign.Client;
import feign.Feign;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;

@Configuration
public class BeanFactory {

    @Bean
    public Environment hlEnvironment(KubernetesClient kubernetesClient,
                                     RestTemplate restTemplate,
                                     PactBrokerClient pactBrokerClient,
                                     ObjectMapper objectMapper) {
        return new KubernetesEnvironment(kubernetesClient, restTemplate, pactBrokerClient, objectMapper);
    }

    @Bean
    public PactValidator pactValidator(Environment environment){
        return new PactValidator(environment);
    }

    @Bean
    public SwaggerValidator swaggerValidator(Environment environment) {
        return new SwaggerValidator(environment);
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        restTemplate.setMessageConverters(Lists.newArrayList(converter));
        return restTemplate;
    }

    @Bean
    public Client feignClient() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }}, null);
            SSLSocketFactory trustingSSLSocketFactory = ctx.getSocketFactory();

            return new Client.Default(
                trustingSSLSocketFactory,
                new NoopHostnameVerifier()
            );
        } catch (Exception e) {
            throw new RuntimeException("Unable to create feign Client with ssl (trust-all) support", e);
        }
    }

    @Bean
    public PactBrokerClient pactBrokerClient(ObjectFactory<HttpMessageConverters> messageConverters, Client client, @Value("${pactbroker.url}") String pactBrokerUrl) {
        return Feign.builder()
            .client(client)
            .contract(new SpringMvcContract())
            .encoder(new SpringEncoder(messageConverters))
            .decoder(new SpringDecoder(messageConverters))
            .target(PactBrokerClient.class, pactBrokerUrl);
    }
}
