package dev.ioexception.community.config;

import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BulkIngestListener<Context> implements BulkListener<Context> {

    private static final Logger log = LoggerFactory.getLogger(BulkIngestListener.class);

    @Override
    public void beforeBulk(long executionId, BulkRequest request, List<Context> contexts) {
        log.info("number of requests: {}", request.operations().size());
    }

    @Override
    public void afterBulk(long executionId, BulkRequest request, List<Context> contexts, BulkResponse response) {

    }

    @Override
    public void afterBulk(long executionId, BulkRequest request, List<Context> contexts, Throwable failure) {

    }
}