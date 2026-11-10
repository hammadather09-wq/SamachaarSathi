package android.net;

import android.os.Parcel;
import java.util.Collections;
import java.util.List;

public class TestUri extends Uri {
    private final String str;

    public TestUri(String str) {
        this.str = str;
    }

    @Override public boolean isHierarchical() { return true; }
    @Override public boolean isRelative() { return false; }
    @Override public String getScheme() { return "content"; }
    @Override public String getSchemeSpecificPart() { return "//fake/test"; }
    @Override public String getEncodedSchemeSpecificPart() { return "//fake/test"; }
    @Override public String getAuthority() { return "fake"; }
    @Override public String getEncodedAuthority() { return "fake"; }
    @Override public String getUserInfo() { return null; }
    @Override public String getEncodedUserInfo() { return null; }
    @Override public String getHost() { return "fake"; }
    @Override public int getPort() { return -1; }
    @Override public String getPath() { return "/test"; }
    @Override public String getEncodedPath() { return "/test"; }
    @Override public String getQuery() { return null; }
    @Override public String getEncodedQuery() { return null; }
    @Override public String getFragment() { return null; }
    @Override public String getEncodedFragment() { return null; }
    @Override public List<String> getPathSegments() { return Collections.singletonList("test"); }
    @Override public String getLastPathSegment() { return "test"; }
    @Override public Builder buildUpon() { throw new UnsupportedOperationException(); }
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {}
    @Override public String toString() { return str; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Uri)) return false;
        return toString().equals(o.toString());
    }
    @Override public int hashCode() { return toString().hashCode(); }
    @Override public int compareTo(Uri other) { return toString().compareTo(other.toString()); }
}
