package com.mengcraft.permission.manager;

import java.util.Date;

/**
 * Created on 16-6-20.
 */
public class Attach {
    private final String name;
    private final int type;
    private final long outdatedTime;

    private Attach(String name, int type, long outdatedTime) {
        this.name = name;
        this.type = type;
        this.outdatedTime = outdatedTime;
    }

    public boolean hasOutdatedTime() {
        return outdatedTime != 0;
    }

    public Date getOutdated() {
        return new Date(outdatedTime);
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public long getOutdatedTime() {
        return outdatedTime;
    }

    public static Attach of(String name, int type, Date outdated) {
        return new Attach(name, type, outdated.getTime());
    }
}
