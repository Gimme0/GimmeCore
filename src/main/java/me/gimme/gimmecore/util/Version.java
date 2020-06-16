package me.gimme.gimmecore.util;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class for comparing version Strings.
 * The version String has to contain between one and three integers separated by periods and nothing more
 * (e.g. "1.14.4", "1", "14.00").
 * For example, "14.0" > "1.14.4" > "1" > "0.014" == "0.14.00".
 * https://stackoverflow.com/a/11024200
 */
public class Version implements Comparable<Version> {

    private static final String JOIN = ".";
    private static final String SPLIT = "\\.";
    private static final String REGEX = "[0-9]+(" + SPLIT + "[0-9]+){0,2}";

    private final String version;

    public final String get() {
        return this.version;
    }

    /**
     * @return an array of all the parts in the version string.
     */
    public String[] getParts() {
        return getParts(-1);
    }

    /**
     * @param n amount of parts to get, or < 0 if all parts
     * @return an array of the first n numbers in the version string, or all parts if n >= amount of parts.
     */
    public final String[] getParts(int n) {
        String[] strings = get().split(SPLIT);
        if (n < 0 || n >= strings.length) return strings;
        String[] result = new String[n];
        for (int i = 0; i < result.length; i++) {
            result[i] = strings[i];
        }
        return result;
    }

    /**
     * The version string should be checked in {@link #isVersion(String)} first
     * to be sure that it has a valid version format.
     *
     * @param version the version string
     */
    public Version(@NotNull String version) {
        if (!version.matches(REGEX))
            throw new IllegalArgumentException("Invalid version format");
        this.version = version;
    }

    /**
     * Checks if the specified string has a valid version format and can be represented as a Version object.
     *
     * @param string the string to check
     * @return if the string has a valid version format
     */
    public static boolean isVersion(String string) {
        return string.matches(REGEX);
    }

    /**
     * @param parent version to check if this is a sub version to
     * @return if this is the same version as or is a sub version to the specified parent version
     */
    public boolean isSubVersionTo(Version parent) {
        return new Version(String.join(JOIN, getParts(parent.getParts().length))).compareTo(parent) == 0;
    }

    /**
     * @param other version to compare to
     * @return 1 if this is newer, -1 if this is older, 0 if same version (e.g. 1 and 1.0)
     */
    @Override
    public int compareTo(@NotNull Version other) {
        String[] thisParts = this.getParts();
        String[] thatParts = other.getParts();
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (this.getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

    @Override
    public final int hashCode() {
        final int[] PRIME = {2, 3, 5};
        final String[] parts = this.get().split(SPLIT);
        int hashCode = 0;
        for (int i = 0; i < parts.length; i++) {
            final int part = Integer.parseInt(parts[i]);
            if (part > 0) {
                hashCode += PRIME[i] ^ part;
            }
        }
        return hashCode;
    }

}
