package com.mockato.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchPattern {
    private final String mockId;
    private final String path;
    private final String pattern;
    private final String[] groups;

    public MatchPattern(String mockId, String path, String pattern, String[] groups) {
        this.mockId = mockId;
        this.path = path;
        this.pattern = pattern;
        this.groups = groups;
    }

    public String getMockId() {
        return mockId;
    }

    public String getPath() {
        return path;
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
        MatchPattern that = (MatchPattern) o;
        return Objects.equals(mockId, that.mockId) &&
                Objects.equals(path, that.path) &&
                Objects.equals(pattern, that.pattern) &&
                Arrays.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mockId, path, pattern);
        result = 31 * result + Arrays.hashCode(groups);
        return result;
    }

    @Override
    public String toString() {
        return "MatchPattern{" +
                ", mockId='" + mockId + '\'' +
                ", path='" + path + '\'' +
                ", pattern='" + pattern + '\'' +
                ", groups=" + Arrays.toString(groups) +
                '}';
    }
}
