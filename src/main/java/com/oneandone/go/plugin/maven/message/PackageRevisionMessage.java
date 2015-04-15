package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(of = { "revision", "timestamp", "user" })
public class PackageRevisionMessage {

    @Expose
    @Getter private String revision;

    @Expose
    @Getter private Date timestamp;

    @Expose
    @Getter private String user;

    @Expose
    @Getter private String revisionComment;

    @Expose
    @Getter private String trackbackUrl;

    @Expose
    @Getter private Map<String, String> data = new HashMap<String, String>();

    public PackageRevisionMessage(final String revision, final Date timestamp, final String user, final String revisionComment, final String trackbackUrl) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.user = user;
        this.revisionComment = revisionComment;
        this.trackbackUrl = trackbackUrl;
    }

    public void addData(String key, String value) {
        data.put(key, value);
    }

    public String getDataFor(String key) {
        return data.get(key);
    }
}
