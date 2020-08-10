package com.mockato.codeExecution;

import org.graalvm.polyglot.Context;
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
 *
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

    public static void main(String... args) {

        String body = "var a = params['param1'];\n" +
                "var b = params[\"param2\"];\n" +
                "var headers = {\n" +
                "    header1: a,\n" +
                "    header2: b\n" +
                "}; \n" +
                "var body = 'its ok';\n" +
                "//return statement\n" +
                "({ statusCode   : 200, body : params, headers  : headers })";

        System.out.println(body);

        JavasScriptEngine javasScriptEngine = new JavasScriptEngine();

        Map<String, Object> params = new HashMap<>();
        params.put("param1", "vxoxox");
        params.put("param2", "xdxdxdxd");

        Map map = javasScriptEngine.execute(params, body);
        System.out.println(map);
    }

    /**
     * Execute script code passed via {@param script} argument. {@param params} are passed into script execution context
     * so, script can access them. There is no limitations around time/script complexity.
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
        try (Context context = Context.newBuilder().build()) {
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
                 * that pretty bad xD fixme
                 */
                if (value instanceof Map) {
                    Map inner = new HashMap();
                    Iterator iteratorI = ((Map) value).keySet().iterator();
                    while (iteratorI.hasNext()) {
                        Object keyI = iteratorI.next();
                        Object valueI = ((Map) value).get(keyI);
                        inner.put(keyI, valueI);
                    }
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