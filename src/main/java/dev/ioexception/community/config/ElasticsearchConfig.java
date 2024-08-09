package dev.ioexception.community.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${spring.elasticsearch.uris}")
    private String[] exHost;

    @Value("${caPath}")
    private String certificationPath;

    private final int FLUSH_INTERVAL = 1;
    private final int MAX_OPERATION = 10000;

    @Bean
    public RestClient buildClient() throws Exception {
        RestClientBuilder restClientBuilder = RestClient.builder(
                new HttpHost("127.0.0.1", 9203, "https")
        );

        restClientBuilder.setDefaultHeaders(
                new BasicHeader[]{new BasicHeader("my-header", "my-value")}
        );

        restClientBuilder.setRequestConfigCallback(
                requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(70000)
        );

        String caPath = certificationPath;
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate trustedCa;

        try (FileInputStream fis = new FileInputStream(caPath)) {
            trustedCa = (X509Certificate) certificateFactory.generateCertificate(fis);
        }

        KeyStore trustStore = KeyStore.getInstance("pkcs12");
        trustStore.load(null,null);
        trustStore.setCertificateEntry("ca", trustedCa);

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null)
                .build();

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder
                .setSSLContext(sslContext)
                .setDefaultCredentialsProvider(credentialsProvider))
        ;

        return restClientBuilder.build();
    }

    @Bean
    RestClientTransport restClientTransport(RestClient restClient, ObjectProvider<RestClientOptions> restClientOptions) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return new RestClientTransport(restClient, new JacksonJsonpMapper(mapper), restClientOptions.getIfAvailable());
    }

    @Bean
    public BulkIngester<BulkOperation> bulkIngester (ElasticsearchClient client, BulkIngestListener<BulkOperation> listener) {
        return BulkIngester.of(b -> b
                .client(client)
                .flushInterval(FLUSH_INTERVAL, TimeUnit.SECONDS)
                .maxOperations(MAX_OPERATION)
                .listener(listener));
    }

    @Bean
    public BulkIngestListener<BulkOperation> bulkIngestListener() {
        return new BulkIngestListener<>();
    }
}

