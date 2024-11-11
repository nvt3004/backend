package com.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * The persistent class for the permissions database table.
 * 
 */
@Entity
@Table(name = "permissions")
@NamedQuery(name = "Permission.findAll", query = "SELECT p FROM Permission p")
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "permission_id")
    private int permissionId;

    @Column(name = "permission_name")
    private String permissionName;

    //bi-directional many-to-one association to ManagePermission
    @OneToMany(mappedBy = "permission")
    @JsonManagedReference("permission-managePermissions") 
    private List<ManagePermission> managePermissions;

    public Permission() {
    }

    public int getPermissionId() {
        return this.permissionId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public List<ManagePermission> getManagePermissions() {
        return this.managePermissions;
    }

    public void setManagePermissions(List<ManagePermission> managePermissions) {
        this.managePermissions = managePermissions;
    }

    public ManagePermission addManagePermission(ManagePermission managePermission) {
        getManagePermissions().add(managePermission);
        managePermission.setPermission(this);

        return managePermission;
    }

    public ManagePermission removeManagePermission(ManagePermission managePermission) {
        getManagePermissions().remove(managePermission);
        managePermission.setPermission(null);

        return managePermission;
    }
}
