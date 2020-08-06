package com.mockato.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JavasScriptEngine {

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

    public Map execute(Map<String, Object> params, String script) {
        try (Context context = Context.newBuilder()
                .build()) {

            context.getBindings("js").putMember("params", ProxyObject.fromMap(params));

            Value result = context.eval("js", script);
            Map output = result.as(Map.class);

            Map finOutput = new HashMap();
            Iterator iterator = output.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = output.get(key);

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
            System.out.println(ex);
            return null;
        }
    }

}