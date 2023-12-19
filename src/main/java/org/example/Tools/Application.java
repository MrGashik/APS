package org.example.Tools;

public class Application {
    private final Integer id_source;
    private final Integer id_application;
    private final String content_application;

    public Application(Integer id_s, Integer id_a, String content) {
        this.id_source = id_s;
        this.id_application = id_a;
        this.content_application = content;
    }

    public Integer getIdSource() {
        return this.id_source;
    }

    @Override
    public String toString() {
        return "{Source #" + this.id_source + ": App #" + this.id_application + "}";
    }
}
