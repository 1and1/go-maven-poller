package com.oneandone.go.plugin.maven.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringTokenizer;

/** The representation of a maven version. */
@EqualsAndHashCode(of = "original")
public class MavenVersion implements Serializable, Comparable<MavenVersion> {

    /** The serialization version of this class. */
    private static final long serialVersionUID = 2L;

    /**
     * The original version.
     *
     * @return the original version
     */
    @Getter private final String original;

    /** The version without the qualifier. */
    private final String version;

    /** The qualifier. */
    private String qualifier;

    /** The SNAPSHOT timestamp. */
    private String timestamp;

    /** The SNAPSHOT build number. */
    private String buildNumber;

    /** The version digits as String. */
    private final String[] digitStrings;

    /** The version digits. */
    private final int[] digits;

    /** The qualifier delimiter. */
    private char lastDelimiter;

    /**
     * Constructs a version representation for the specified version.
     *
     * @param version the version
     * @throws NullPointerException if the specified version is {@code null}
     * @throws IllegalArgumentException if the specified version is empty or the version digits could not be parsed
     */
    public MavenVersion(final String version) {
        Objects.requireNonNull(version, "version string may not be null");
        if (version.isEmpty()) {
            throw new IllegalArgumentException("version may not be empty");
        }

        // check if version ends with timestamp and buildnumber
        final String trimmedVersion = version.trim();
        if (trimmedVersion.contains("SNAPSHOT") && trimmedVersion.matches(".* \\([0-9]{8}\\.[0-9]{6}-[0-9]+\\)")) {
            int startOfSpecification = trimmedVersion.lastIndexOf(" (");
            this.original = trimmedVersion.substring(0, startOfSpecification);

            this.timestamp = trimmedVersion.substring(startOfSpecification + 2, startOfSpecification + 17);
            this.buildNumber = trimmedVersion.substring(startOfSpecification + 18, trimmedVersion.lastIndexOf(")"));
        } else {
            this.original = trimmedVersion;
        }

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
                digits[i] = Integer.parseInt(digit);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("invalid version string " + version);
            }
        }
    }

    /**
     * Strips the version from the specified String.
     *
     * @param ver the version
     * @return the stripped version
     */
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

    /**
     * Returns the value for the specified version digit index or 0 if the index is out of bounds.
     *
     * @param index the digit index
     * @return the value for the specified version digit index or 0 if the index is out of bounds
     */
    private Integer getValue(int index) {
        return index < this.digitStrings.length ? digits[index] : 0;
    }

    /**
     * Returns the major version ({@code MAJOR.MINOR.BUGFIX.HOTFIX)}.
     *
     * @return the major version
     */
    public int getMajor() {
        return 0 < this.digits.length ? this.digits[0] : 0;
    }

    /**
     * Returns the minor version ({@code MAJOR.MINOR.BUGFIX.HOTFIX)}.
     *
     * @return the minor version
     */
    public int getMinor() {
        return 1 < this.digits.length ? this.digits[1] : 0;
    }

    /**
     * Returns the bugfix version ({@code MAJOR.MINOR.BUGFIX.HOTFIX)}.
     *
     * @return the bugfix version
     */
    public int getBugfix() {
        return 2 < this.digits.length ? this.digits[2] : 0;
    }

    /**
     * Returns the hotfix version ({@code MAJOR.MINOR.BUGFIX.HOTFIX)}.
     *
     * @return the hotfix version
     */
    public int getHotfix() {
        return 3 < this.digits.length ? this.digits[3] : 0;
    }

    /**
     * Returns the qualifier.
     *
     * @return the qualifier
     */
    public String getQualifier() {
        return this.qualifier;
    }

    /**
     * Returns the version with qualifier.
     * <p />
     * Snapshot versions will be resolved to their actual artifact verison with timestamp and build number.
     *
     * @return the version with qualifier
     */
    public String getVersion() {
        if (isSnapshot()) {
            return this.version + this.lastDelimiter + this.timestamp + "-" +  this.buildNumber;
        }

        return this.getQualifier() != null ? this.version + this.lastDelimiter + this.getQualifier() : this.version;
    }

    /**
     * Returns the specific version with qualifier and - if this version is a {@code SNAPSHOT} - timestamp and build number.
     *
     * @return the specific version
     */
    public String getVersionSpecific() {
        if (isSnapshot() && this.timestamp != null && this.buildNumber != null) {
            return getOriginal() + " (" + this.timestamp + "-" +  this.buildNumber + ")";
        }

        return getOriginal();
    }

    /**
     * Returns {@code true} if {@code this} version is older or equal to the specified {@code version}, otherwise {@code false}.
     *
     * @param version the version to compare with
     * @return {@code true} if {@code this} version is older or equal to the specified {@code version}, otherwise {@code false}
     */
    public boolean notNewerThan(final MavenVersion version) {
        return this.compareTo(version) <= 0;
    }

    /**
     * Returns {@code true} if {@code this} version is older than the specified {@code version}, otherwise {@code false}.
     *
     * @param version the version to compare with
     * @return {@code true} if {@code this} version is older than the specified {@code version}, otherwise {@code false}
     */
    public boolean lessThan(final MavenVersion version) {
        return this.compareTo(version) < 0;
    }

    /**
     * Returns {@code true} if {@code this} version is newer or equal to the specified {@code version}, otherwise {@code false}.
     *
     * @param version the version to compare with
     * @return {@code true} if {@code this} version is newer or equal to the specified {@code version}, otherwise {@code false}
     */
    public boolean greaterOrEqual(final MavenVersion version) {
        return this.compareTo(version) >= 0;
    }

    /**
     * Returns {@code true} if {@code this} version is equal to the specified {@code version}, otherwise {@code false}.
     *
     * @param version the version to compare with
     * @return {@code true} if {@code this} version is equal to the specified {@code version}, otherwise {@code false}
     */
    public boolean equal(final MavenVersion version) {
        return this.compareTo(version) == 0;
    }

    /**
     * Returns {@code true} if the qualifier equals {@code SNAPSHOT} (ignore case), otherwise {@code false}.
     *
     * @return {@code true} if the qualifier equals {@code SNAPSHOT} (ignore case), otherwise {@code false}
     */
    public boolean isSnapshot() {
        return qualifier != null && "SNAPSHOT".equals(qualifier.toUpperCase());
    }

    /**
     * Sets the snapshot information for {@code this version}.
     *
     * @param timestamp the timestamp to set
     * @param buildNumber the build number to set
     */
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

            if ("SNAPSHOT".equalsIgnoreCase(this.qualifier) && "SNAPSHOT".equalsIgnoreCase(otherVersion.getQualifier())) {
                if (result == 0 && this.timestamp != null && otherVersion.timestamp != null) {
                    result = new NaturalOrderComparator().compare(this.timestamp, otherVersion.timestamp);
                }

                if (result == 0 && this.buildNumber != null && otherVersion.buildNumber != null) {
                    result = new NaturalOrderComparator().compare(this.buildNumber, otherVersion.buildNumber);
                }

                if (this.timestamp == null && this.buildNumber == null && otherVersion.timestamp == null && otherVersion.buildNumber == null) {
                    result = 0;
                } else {
                    if (otherVersion.timestamp == null && otherVersion.buildNumber == null) {
                        result = 1;
                    }
                    if (this.timestamp == null && this.buildNumber == null) {
                        result = -1;
                    }
                }
            }
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
