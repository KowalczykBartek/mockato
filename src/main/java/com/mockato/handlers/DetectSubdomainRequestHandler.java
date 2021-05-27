package com.mockato.handlers;

import com.mockato.model.ResponseContainer;
import com.mockato.service.ConfigurationService;
import com.mockato.service.MockExecutionService;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectSubdomainRequestHandler implements Handler<RoutingContext> {

    private static Logger LOGGER = LoggerFactory.getLogger(DetectSubdomainRequestHandler.class);

    private final ConfigurationService configurationService;
    private final MockExecutionService executor;

    //domain (address) of application.
    private final String domain;
    private final String wwwHostCase;

    public DetectSubdomainRequestHandler(ConfigurationService configurationService, MockExecutionService executor) {
        this.configurationService = configurationService;
        this.executor = executor;
        this.domain = configurationService.getDomain();
        this.wwwHostCase = getWwwHost(domain);
    }

    @Override
    public void handle(RoutingContext event) {

        /*
         * If someone make request for subdomain (so, executing mock), we need to intercept the request,
         * ask database if we have mock registered for method/path/etc, execute it and return response to user.
         * In case request is performed on root domain (xxx-something.com), just forward request.
         */
        String host = event.request().getHeader("host");

        if (host == null) {
            LOGGER.error("header host is null");
            event.next();
            return;
        }
        LOGGER.info("Received host header {}", host);
        String plainHost = toPlainHost(host);
        LOGGER.info("After parsing host header = {}", host);

        if (host.contains(domain) && !host.contains(wwwHostCase)) {
            String subdomain = plainHost.split(domain)[0];
            LOGGER.info("Executing mock request against subdomain {}", host);

            /*
             * here, we are in case when someone calls us from subdomain, that means, this is mock
             * to be handled.
             */
            executor.respondToRequestWithMock(/*extract as object*/subdomain, event.request().method(), event.request().path())
                    .onComplete(res -> {
                        if (res.succeeded()) {
                            ResponseContainer responseContainer = res.result();

                            responseContainer.getHeaders().forEach((key, value) -> event.response().headers().add(key, value));

                            event.response().setStatusCode(responseContainer.getStatusCode())
                                    .end(responseContainer.getBody());
                        } else {
                            ResponseUtils.responseFromException(event.response(), res.cause());
                        }
                    });

        } else {
            event.next();
        }
    }

    /**
     * Remove prefix(all prefixes) from received domain, I expect that
     * <pre>
     *   https://subdomain.domain.com
     *   http://www.subdomain.domain.com
     *   subdomain.domain.com
     * </pre>
     * results in the same subdomain.domain.com string.
     */
    private String toPlainHost(String host) {
        return host.replaceAll("https://", "")
                .replaceAll("http://", "");
    }

    /*
     * We need to handle case when host = www.somedomain.com,
     */
    private String getWwwHost(String domain) {
        return "www" + domain;
    }
}
