package com.mengcraft.permission.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Created on 15-10-20.
 */
@Entity
public class PermissionUser implements PermissionMXBean {

    @Id
    private int id;

    @Column
    private String name;

    @Column
    private String value;

    @Column
    private Timestamp outdated;

    @CreatedTimestamp
    private Timestamp created;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Timestamp getOutdated() {
        return outdated;
    }

    public void setOutdated(Timestamp outdated) {
        this.outdated = outdated;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "value='" + getValue() + '\'' + ", outdated='" + getOutdated() + '\'';
    }

    public long getOutdatedTime() {
        return getOutdated().getTime();
    }

}
