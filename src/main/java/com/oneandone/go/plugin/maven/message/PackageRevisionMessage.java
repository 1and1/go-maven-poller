package com.oneandone.go.plugin.maven.message;

import com.google.gson.annotations.Expose;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Representation of a package revision. */
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(of = { "revision", "timestamp", "user" })
public class PackageRevisionMessage {

    /**
     * The version or revision.
     *
     * @return the version
     */
    @Expose
    @Getter private String revision;

    /**
     * The revision timestamp.
     *
     * @return the revision timestampe
     */
    @Expose
    @Getter private Date timestamp;

    @Expose
    @Getter private String user;

    @Expose
    @Getter private String revisionComment;

    /**
     * The track back URL.
     *
     * @return the track back URL
     */
    @Expose
    @Getter private String trackbackUrl;

    /**
     * The map of additional properties.
     *
     * @return the map of additional properties.
     */
    @Expose
    @Getter private Map<String, String> data = new HashMap<>();

    /**
     * Constructs a package revision.
     *
     * @param revision the version
     * @param timestamp the timestamp
     * @param user the user
     * @param revisionComment the revision comment
     * @param trackbackUrl the track back URL
     */
    public PackageRevisionMessage(final String revision, final Date timestamp, final String user, final String revisionComment, final String trackbackUrl) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.user = user;
        this.revisionComment = revisionComment;
        this.trackbackUrl = trackbackUrl;
    }

    /**
     * Adds an additional property.
     *
     * @param key the property key
     * @param value the property value
     */
    public void addData(final String key, final String value) {
        data.put(key, value);
    }

    /**
     * Returns the value for the additional property key.
     *
     * @param key the property key
     * @return the value or {@code null}
     */
    public String getDataFor(final String key) {
        return data.get(key);
    }
}
