package com.mengcraft.permission.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created on 15-10-26.
 */
@Data
@Entity
public class PermissionZone implements PermissionMXBean {

    @Id
    private int id;

    @Column
    private String name;

    @Column
    private String value;

    @Column
    private int type;

    @Override
    public String toString() {
        return "value='" + value + '\'';
    }
}
