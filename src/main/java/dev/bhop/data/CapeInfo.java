package dev.bhop.data;

public final class CapeInfo {

    private final String id;
    private final String alias;
    private final String url;
    private final CapeState state;

    public CapeInfo(String id, String alias, String url, CapeState state) {
        this.id = id;
        this.alias = alias;
        this.url = url;
        this.state = state;
    }

    public String getId() { return id; }
    public String getAlias() { return alias; }
    public String getUrl() { return url; }
    public CapeState getState() { return state; }
}
