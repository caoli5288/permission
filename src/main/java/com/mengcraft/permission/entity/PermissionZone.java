package com.mengcraft.permission.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created on 15-10-26.
 */
@Data
@Entity
@ToString(of = "value")
public class PermissionZone implements PermissionMXBean {

    @Id
    private int id;
    private String name;
    private String value;
    private boolean type;
}
