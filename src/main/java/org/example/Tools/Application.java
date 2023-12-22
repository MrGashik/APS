package org.example.Tools;

public class Application {
    private final Integer id_source;
    private final Integer id_application;
    private final String content_application;
    private final Long create_time;

    public Application(Integer id_s, Integer id_a, String content, Long time) {
        this.id_source = id_s;
        this.id_application = id_a;
        this.content_application = content;
        this.create_time = time;
    }

    public Integer getIdSource() {
        return this.id_source;
    }

    @Override
    public String toString() {
        return "{Source #" + this.id_source + ": App #" + this.id_application + "}";
    }

    public Long getCreate_time() {
        return create_time;
    }
}
