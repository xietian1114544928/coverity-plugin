/*******************************************************************************
 * Copyright (c) 2017 Synopsys, Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Synopsys, Inc - initial implementation and documentation
 *******************************************************************************/
package jenkins.plugins.coverity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract representation of a Coverity version number. Conventional version numbers, as well as current and past
 * codenames are all valid and comparable.
 */
public class CoverityVersion implements Comparable<CoverityVersion>, Serializable {
    public static final CoverityVersion VERSION_INDIO = new CoverityVersion(7, 7, 0, 0);
    public static final CoverityVersion VERSION_JASPER = new CoverityVersion(8, 0, 0, 0);

    public static final CoverityVersion MINIMUM_SUPPORTED_VERSION = VERSION_INDIO;

    // pattern to match format for <major.minor.patch.hotfix> or for <YYYY.MM> version numbers
    static final Pattern parseRegex = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?|(\\d\\d\\d\\d)\\.(\\d\\d)");

    final int major;
    final int minor;
    final int patch;
    final int hotfix;

    public CoverityVersion(int major, int minor, int patch, int hotfix) {
        this.hotfix = hotfix;
        this.minor = minor;
        this.patch = patch;
        this.major = major;
    }

    public CoverityVersion(int major, int minor) {
        this.hotfix = 0;
        this.patch = 0;
        this.major = major;
        this.minor = minor;
    }

    public static CoverityVersion parse(String s) {
        Matcher m = parseRegex.matcher(s);
        if(!m.find()) {
            return null;
        }

        if (m.group(5) != null && m.group(6) != null) {
            //srm number
            return new CoverityVersion(gi(m, 5), gi(m, 6));
        } else {
            //number
            return new CoverityVersion(gi(m, 1), gi(m, 2), gi(m, 3), gi(m, 4));
        }
    }

    /**
     * Shorthand method. Return an integer from the given group of the given {@link Matcher}
     */
    private static int gi(Matcher m, int group) {
        if(m.group(group) == null) {
            return 0;
        }
        return Integer.parseInt(m.group(group));
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (hotfix > 0 ? ("." + hotfix) : "");
    }

    public int compareTo(CoverityVersion o) {

        if(major == o.major) {
            if(minor == o.minor) {
                if(patch == o.patch) {
                    return cmp(hotfix, o.hotfix);
                } else {
                    return cmp(patch, o.patch);
                }
            } else {
                return cmp(minor, o.minor);
            }
        } else {
            return cmp(major, o.major);
        }
    }

    /**
     * The way that Compare Major Minor works is that the argument passed in is the analysis version.
     * Compares the version's major and minor version number. Returns true if the current version is greater than
     *  or equals to the passed in version. Else, the compare will return false.
     * @param version
     * @return
     */
    public boolean compareToAnalysis(CoverityVersion version){
        if(major == version.major){
            return minor >= version.minor;
        }else{
            return major > version.major;
        }
    }

    private int cmp(int a, int b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        CoverityVersion other = (CoverityVersion) o;

        if (major != other.major) {
            return false;
        }
        if (minor != other.minor) {
            return false;
        }
        if (patch != other.patch) {
            return false;
        }
        if (hotfix != other.hotfix) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 31;
        result = 31 * result + major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        result = 31 * result + hotfix;
        return result;
    }
}
