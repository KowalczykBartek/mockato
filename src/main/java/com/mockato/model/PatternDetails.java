package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatternDetails {
    private final String pattern;
    private final String[] groups;

    public PatternDetails(String pattern, String[] groups) {
        this.pattern = pattern;
        this.groups = groups;
    }

    public String getPattern() {
        return pattern;
    }

    public String[] getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternDetails that = (PatternDetails) o;
        return Objects.equals(pattern, that.pattern) &&
                Arrays.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(pattern);
        result = 31 * result + Arrays.hashCode(groups);
        return result;
    }

    @Override
    public String toString() {
        return "PatternDetails{" +
                "pattern='" + pattern + '\'' +
                ", groups=" + Arrays.toString(groups) +
                '}';
    }
}
