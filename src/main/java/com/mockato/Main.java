package com.mockato;

import com.mockato.handlers.*;
import com.mockato.repo.MockRepository;
import com.mockato.service.ConfigurationService;
import com.mockato.service.MockExecutionService;
import com.mockato.service.MockService;
import com.mockato.service.PathParserService;
import com.mockato.utils.CurlCommandParser;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;
import io.vertx.ext.web.templ.thymeleaf.ThymeleafTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starter class.
 */
public class Main {

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String... args) {

        LOGGER.info("Starting mockato ...");

        ConfigurationService configurationService = new ConfigurationService(System.getenv());

        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();

        MockRepository mockRepository = new MockRepository(configurationService);
        PathParserService pathParserService = new PathParserService();
        MockExecutionService executor = new MockExecutionService(vertx, mockRepository, pathParserService);
        MockService mockService = new MockService(mockRepository, pathParserService);

        CurlCommandParser curlCommandParser = new CurlCommandParser(configurationService.getDomain());

        Router router = Router.router(vertx);

        DetectSubdomainRequestHandler detectSubdomainRequestHandler = new DetectSubdomainRequestHandler(configurationService, executor);
        router.route().handler(detectSubdomainRequestHandler);

        BodyHandlerImpl bodyHandler = new BodyHandlerImpl();
        bodyHandler.setBodyLimit(1024); //1kb as limit, because why not

        //API
        GetAllMocks getAllMocksHandler = new GetAllMocks(mockService);
        router.get("/api/subdomains/:subdomain").handler(getAllMocksHandler);

        CreateMockDefinitionHandler mockDefinitionHandler = new CreateMockDefinitionHandler(mockService);
        router.post("/api/subdomains/:subdomain/mock/:mockId").handler(bodyHandler).handler(mockDefinitionHandler);

        DeleteMockHandler deleteMockHandler = new DeleteMockHandler(mockService);
        router.delete("/api/subdomains/:subdomain/mock/:mockId").handler(deleteMockHandler);

        //HTML/UI handler
        ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create(vertx);
        router.route("/static/*").handler(StaticHandler.create("webapp/static"));

        RenderCreateMockPage createMock = new RenderCreateMockPage(configurationService, mockService, engine);
        router.route(HttpMethod.GET, "/newMock/:subdomain/create").handler(createMock);

        GetMockDetailsAndRender getMockDetailsAndRender = new GetMockDetailsAndRender(configurationService, mockService, engine, curlCommandParser);
        router.route(HttpMethod.GET, "/:subdomain/:mockId").handler(getMockDetailsAndRender);

        RenderIndexPageHandler renderIndexPageHandler = new RenderIndexPageHandler(engine);
        router.route(HttpMethod.GET, "/").handler(renderIndexPageHandler);

        //FIXME it should work as deployed verticles to manage threads.
        server.requestHandler(router).listen(configurationService.getHttpListenPort());
    }
}
