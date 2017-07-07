package com.mengcraft.permission.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Created on 15-10-20.
 */
@Data
@Entity
public class PermissionUser implements PermissionMXBean {

    @Id
    private int id;

    @Column
    private String name;

    @Column
    private String value;

    @Column
    private int type;

    @Column
    private Timestamp outdated;

    @CreatedTimestamp
    private Timestamp created;

    @Override
    public String toString() {
        return "value='" + getValue() + '\'' + ", outdated='" + getOutdated() + '\'';
    }

    public long getOutdatedTime() {
        return getOutdated().getTime();
    }
}
