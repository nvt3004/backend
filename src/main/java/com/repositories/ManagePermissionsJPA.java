package com.repositories;

import com.entities.ManagePermission;
import com.entities.Permission;
import com.entities.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagePermissionsJPA extends JpaRepository<ManagePermission, Integer> {

	@Query("SELECT p.permissionName FROM ManagePermission mp JOIN mp.permission p WHERE mp.user.userId = ?1")
	List<String> findPermissionsByUserId(int userId);

	@Modifying
	@Query("DELETE FROM ManagePermission mp WHERE mp.user = :user")
	void deleteByUser(User user);

	@Query("SELECT p FROM ManagePermission mp JOIN mp.permission p WHERE mp.user.userId = ?1")
	List<Permission> findPermissionsByUser(int userId);

	@Query("SELECT o FROM ManagePermission o WHERE o.user.userId=:userId AND o.permission.permissionId=:perId")
	ManagePermission findOneManagePermission(@Param("userId") int userId, @Param("perId") int perId);
}
