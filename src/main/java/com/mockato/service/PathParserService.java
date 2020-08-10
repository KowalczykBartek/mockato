package com.mockato.service;

import com.google.common.collect.ImmutableMap;
import com.mockato.model.PatternDetails;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrap entire logic required to parse path received by user and extract path parameters.
 * This idea (and partially source code) is stolen from Vert.x web source code.
 */
public class PathParserService {
    // intersection of regex chars and https://tools.ietf.org/html/rfc3986#section-3.3
    private static final Pattern RE_OPERATORS_NO_STAR = Pattern.compile("([\\(\\)\\$\\+\\.])");
    // Pattern for :<token name> in path
    private static final Pattern RE_TOKEN_SEARCH = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");

    public static void main(String... args) {
        PathParserService pathParserService = new PathParserService();
        Pair<String, List<String>> val = pathParserService.pathToPattern("/something/:a1/:a2");

        String[] groups = new String[val.getRight().size()];
        val.getRight().toArray(groups);

        PatternDetails patternDetails = new PatternDetails(val.getLeft(), groups);

        System.out.println(pathParserService.extractPathParams(patternDetails, "/something/arg1/arg2"));
    }

    /**
     * Extract pattern for received {@param path}, this pattern can be later compiled by {@see java.util.regex.Pattern#compile()},
     * to check if received request uri match. Second value, return as right side, is list of names configured for
     * path by user. Lets take an example, user create mock for path "/hello/:a1/:a2", what we want to receive from
     * this method is:
     * <pre>
     *   pattern = /(?<p0>[^/]+)/(?<p1>[^/]+)
     *   path_params = [a1, a2]
     * </pre>
     *
     * @param path to be parsed
     * @return Pair.of(parsed pattern, list of path parameters ' names)
     */
    public Pair<String, List<String>> pathToPattern(String path) {
        // escape path from any regex special chars
        path = RE_OPERATORS_NO_STAR.matcher(path).replaceAll("\\\\$1");

        // We need to search for any :<token name> tokens in the String and replace them with named capture groups
        Matcher m = RE_TOKEN_SEARCH.matcher(path);
        StringBuffer sb = new StringBuffer();
        List<String> groups = new ArrayList<>();
        int index = 0;
        while (m.find()) {
            String param = "p" + index;
            String group = m.group().substring(1);
            if (groups.contains(group)) {
                throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
            }
            m.appendReplacement(sb, "(?<" + param + ">[^/]+)");
            groups.add(group);
            index++;
        }
        m.appendTail(sb);
        path = sb.toString();

        return Pair.of(path, groups);
    }

    /**
     * Extract values from received request uri, based on {@param patternDetails}.
     * Lets take an example, user defined dynamic mock for <pre>/something/:a1/:a2</pre>, and then makes request
     * with following uri <pre>/something/arg1/arg2</pre>, expected returned map if <pre>{a1=arg1, a2=arg2}</pre>
     *
     * @param patternDetails
     * @param path
     * @return map of values and corresponding path param name.
     */
    public Map<String, Object> extractPathParams(PatternDetails patternDetails, String path) {
        Map<String, Object> params;
        if (patternDetails.getGroups().length > 0) {
            Pattern compiledPattern = Pattern.compile(patternDetails.getPattern());
            Matcher match = compiledPattern.matcher(path);
            match.matches();

            params = new HashMap<>();
            for (int i = 0; i < patternDetails.getGroups().length; i++) {
                params.put(patternDetails.getGroups()[i], match.group("p" + i));
            }
        } else {
            params = ImmutableMap.of();
        }
        return params;
    }
}
