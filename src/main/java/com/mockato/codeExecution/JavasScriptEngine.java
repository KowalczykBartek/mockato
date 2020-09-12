package com.mockato.codeExecution;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Wrap embedded GraalVM execution context.
 * You can read more about graal embedded https://www.graalvm.org/docs/reference-manual/embed/
 * <p>
 * Example script that can be executed, 'params' array is just holder for params passed altogether with script code.
 * <pre>
 *  var a = params['param1'];
 *  var b = params["param2"];
 *  var headers = {
 *      header1: a,
 *      header2: b
 *  };
 *  var body = 'its ok';
 *  //return statement
 *  ({ statusCode   : 200, body : params, headers  : headers })
 * </pre>
 */
public class JavasScriptEngine {

    private static Logger LOGGER = LoggerFactory.getLogger(JavasScriptEngine.class);
    private static final int STATEMENTS_HARD_LIMIT = 500;

    public static void main(String... args) throws InterruptedException {

        String body = "var a = params['param1'];\n" +
                "var b = params[\"param2\"];\n" +
                "var headers = {\n" +
                "    header1: a,\n" +
                "    header2: b\n" +
                "}; \n" +
                "\n" +
                "while(true) {}\n" +
                "\n" +
                "var body = 'its ok';\n" +
                "//return statement\n" +
                "({ statusCode   : 200, body : params, headers  : headers })";

        System.out.println(body);

        Thread thread = new Thread(() -> {
            try {
                JavasScriptEngine javasScriptEngine = new JavasScriptEngine();

                Map<String, Object> params = new HashMap<>();
                params.put("param1", "vxoxox");
                params.put("param2", "xdxdxdxd");

                Map map = javasScriptEngine.execute(params, body);
                System.out.println(map);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        });

        thread.start();

        System.out.println("XO");
    }

    /**
     * Execute script code passed via {@param script} argument. {@param params} are passed into script execution context
     * so, script can access them. Hard limit for number of statements in single execution is {@link JavasScriptEngine#STATEMENTS_HARD_LIMIT},
     * nonetheless, there is no limit for concurrent invocations, that is up to called to prevent limit on thread pool
     * resource.
     *
     * @param params
     * @param script
     * @return value returned from script or propagate exception, lets say you have following piece of script
     * the Map returned as response, you will receive java's version of
     * <pre>
     *     ({ statusCode   : 200, body : params, headers  : headers })
     * </pre>
     */
    public Map execute(Map<String, Object> params, String script) {

        //FIXME limits are very important ;)
        ResourceLimits limits = ResourceLimits.newBuilder()
                .statementLimit(STATEMENTS_HARD_LIMIT, null)
                .build();

        try (Context context = Context.newBuilder().resourceLimits(limits).build()) {
            context.getBindings("js").putMember("params", ProxyObject.fromMap(params));

            //script execution
            Value result = context.eval("js", script);

            Map output = result.as(Map.class);
            Map finOutput = new HashMap();
            Iterator iterator = output.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = output.get(key);

                /*
                 * todo that pretty bad xD
                 */
                if (value instanceof Map) {
                    Map inner = new HashMap();
                    Iterator iteratorI = ((Map) value).keySet().iterator();
                    while (iteratorI.hasNext()) {
                        Object keyI = iteratorI.next();
                        Object valueI = ((Map) value).get(keyI);
                        inner.put(keyI, valueI);
                    }
                    //fixme unchecked map operations
                    finOutput.put(key, inner);
                } else {
                    finOutput.put(key, value);
                }
            }
            return finOutput;
        } catch (Exception ex) {
            LOGGER.error("Error occurred during script execution", ex);
            throw ex;
        }
    }

}