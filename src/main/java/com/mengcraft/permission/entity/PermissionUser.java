package com.mengcraft.permission.entity;

import com.avaje.ebean.annotation.CreatedTimestamp;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

/**
 * Created on 15-10-20.
 */
@Data
@Entity
@ToString(of = {"value", "outdated"})
public class PermissionUser implements PermissionMXBean {

    @Id
    private int id;
    private String name;
    private String value;
    private boolean type;

    @CreatedTimestamp
    private Timestamp created;
    private Timestamp outdated;
}
