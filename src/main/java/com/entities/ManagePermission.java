package com.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

/**
 * The persistent class for the manage_permissions database table.
 * 
 */
@Entity
@Table(name = "manage_permissions")
@NamedQuery(name = "ManagePermission.findAll", query = "SELECT m FROM ManagePermission m")
public class ManagePermission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //bi-directional many-to-one association to Permission
    @ManyToOne
    @JoinColumn(name = "permission_id")
    @JsonBackReference("permission-managePermissions")
    private Permission permission;

    //bi-directional many-to-one association to User
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-managePermissions")
    private User user;

    public ManagePermission() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
