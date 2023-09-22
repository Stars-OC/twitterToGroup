package xyz.starsoc.object;

public class UserToGroup {
    private String username;
    private long group;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserToGroup UserToGroup = (UserToGroup) obj;
        return hashCode() == UserToGroup.hashCode();
    }

    @Override
    public int hashCode() {
        return (int) (username.hashCode()+group);
    }

    public UserToGroup() {
    }

    public UserToGroup(String username, long group) {
        this.username = username;
        this.group = group;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getGroup() {
        return group;
    }

    public void setGroup(long group) {
        this.group = group;
    }
}
