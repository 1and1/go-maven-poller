package com.oneandone.go.plugin.maven.util;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.StringTokenizer;

@EqualsAndHashCode(of = "original")
public class MavenVersion implements Serializable, Comparable<MavenVersion> {

    private static final long serialVersionUID = 2L;

    @Getter private String original;
    private String version;
    private String qualifier;

    private String timestamp;
    private String buildNumber;

    private String[] digitStrings;
    private int[] digits;

    private char lastDelimiter;

    public MavenVersion(final String version) {
        Preconditions.checkNotNull(version, "version string may not be null");
        Preconditions.checkArgument(!version.isEmpty(), "version may not be empty");

        this.original = version.trim();
        final String versionStripped = this.stripVersion(this.original);
        if (versionStripped == null) {
            this.qualifier = this.original;
            this.version = "0.0.0.0";
        } else if (versionStripped.length() < this.original.length()) {
            this.qualifier = this.original.substring(versionStripped.length() + 1);
            this.version = this.original.substring(0, versionStripped.length());
        } else {
            this.version = this.original;
        }

        final StringTokenizer versionTokenizer = new StringTokenizer(this.version, ".");
        final int absoluteTokenCount = versionTokenizer.countTokens();
        this.digitStrings = new String[absoluteTokenCount];
        this.digits = new int[absoluteTokenCount];

        for (int i = 0; i < absoluteTokenCount; i++) {
            final String digit = versionTokenizer.nextToken();
            digitStrings[i] = digit;

            try {
                digits[i] = Integer.valueOf(digit);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("Invalid version string " + version);
            }
        }
    }

    private String stripVersion(final String ver) {
        int lastIndex = -1;
        int counter = 0;

        char delimiter = '.';
        final char[] version = ver.toCharArray();

        for (char c : version) {
            if (!Character.isDigit(c)) {
                lastIndex = counter;
                delimiter = c;
                if (!Character.isLetterOrDigit(c)) {
                    this.lastDelimiter = c;
                }
                break;
            }

            counter++;
        }

        String versionOnly = null;
        if (lastIndex != -1 && ver.length() > lastIndex) {
            final String currentVersionPart = ver.substring(0, lastIndex);

            if (currentVersionPart.matches("[0-9]+")) {
                if (lastIndex < ver.length() && delimiter == '.') {
                    final String nextVersionPart = this.stripVersion(ver.substring(lastIndex + 1));
                    if (nextVersionPart != null) {
                        versionOnly = currentVersionPart + "." + nextVersionPart;
                    } else {
                        versionOnly = currentVersionPart;
                    }
                } else {
                    versionOnly = currentVersionPart;
                }
            }
        } else {
            versionOnly = ver;
        }

        return versionOnly;
    }

    private Integer getValue(int index) {
        return index < this.digitStrings.length ? digits[index] : 0;
    }

    public int getMajor() {
        return 0 < this.digits.length ? this.digits[0] : 0;
    }

    public int getMinor() {
        return 1 < this.digits.length ? this.digits[1] : 0;
    }

    public int getBugfix() {
        return 2 < this.digits.length ? this.digits[2] : 0;
    }

    public int getHotfix() {
        return 3 < this.digits.length ? this.digits[3] : 0;
    }

    public String getQualifier() {
        return this.qualifier;
    }

    public String getVersion() {
        if (isSnapshot()) {
            return this.version + this.lastDelimiter + this.timestamp + "-" +  this.buildNumber;
        }

        return this.getQualifier() != null ? this.version + this.lastDelimiter + this.getQualifier() : this.version;
    }

    public boolean notNewerThan(final MavenVersion lastKnownVersion) {
        return this.compareTo(lastKnownVersion) <= 0;
    }

    public boolean lessThan(final MavenVersion version) {
        return this.compareTo(version) < 0;
    }

    public boolean greaterOrEqual(final MavenVersion version) {
        return this.compareTo(version) >= 0;
    }

    public boolean isSnapshot() {
        return qualifier == null ? false : "SNAPSHOT".equals(qualifier.toUpperCase());
    }

    public void setSnapshotInformation(final String timestamp, final String buildNumber) {
        this.timestamp = timestamp;
        this.buildNumber = buildNumber;
    }

    @Override
    public int compareTo(final MavenVersion otherVersion) {
        int result = 0;
        for (int i = 0; i < this.digits.length; ++i) {
            result = this.getValue(i).compareTo(otherVersion.getValue(i));
            if (result != 0) {
                break;
            }
        }
        if (result == 0 && this.qualifier != null && otherVersion.getQualifier() != null) {
            result = new NaturalOrderComparator().compare(this.qualifier, otherVersion.getQualifier());
        } else {
            if (result == 0 && this.qualifier == null && otherVersion.getQualifier() != null) {
                return 1;
            }

            if (result == 0 && this.qualifier != null && otherVersion.getQualifier() == null) {
                return -1;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return original;
    }

}
