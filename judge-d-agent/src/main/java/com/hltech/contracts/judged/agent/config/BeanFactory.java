package com.hltech.contracts.judged.agent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hltech.contracts.judged.agent.JudgeDPublisher;
import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.Target;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.support.ResponseEntityDecoder;
import org.springframework.cloud.netflix.feign.support.SpringDecoder;
import org.springframework.cloud.netflix.feign.support.SpringEncoder;
import org.springframework.cloud.netflix.feign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
public class BeanFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanFactory.class);

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JudgeDPublisher judgeDEnvironmentPublisher(Feign feign, @Value("${hltech.contracts.judge-d.baseUrl}") String baseUrl) {
        return feign.newInstance(new Target.HardCodedTarget<>(JudgeDPublisher.class, baseUrl));
    }

    @Bean
    public Feign feign(ObjectFactory<HttpMessageConverters> messageConverters, ObjectMapper objectMapper) throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, null);
        SSLSocketFactory trustingSSLSocketFactory = ctx.getSocketFactory();

        Client client = new Client.Default(
            trustingSSLSocketFactory,
            new NoopHostnameVerifier()
        );

        HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        ResponseEntityDecoder decoder = new ResponseEntityDecoder(new SpringDecoder(objectFactory));

        return Feign.builder()
            .client(client)
            .contract(new SpringMvcContract())
            .encoder(new SpringEncoder(messageConverters))
            .retryer(Retryer.NEVER_RETRY)
            .decoder(decoder)
            .build();
    }


    private static class TrustAllX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
