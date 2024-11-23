package com.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entities.User;

public interface UserJPA extends JpaRepository<User, Integer> {

	@Query("SELECT o FROM User o WHERE o.username =:username")
	public User getUserByUsername(@Param("username") String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);

	Optional<User> findByPhoneAndProvider(String phone, String provider);

	Optional<User> findByUsername(String username);

	Optional<User> findByEmailAndProvider(String email, String provider);

	@Query("SELECT o FROM User o WHERE o.email =:email AND o.provider =:provider AND o.userId !=:idUserUpdate")
	Optional<User> findByEmailAndProviderUpdate(@Param("email") String email, @Param("provider") String provider,
			@Param("idUserUpdate") int idUserUpdate);

	@Query("SELECT o FROM User o WHERE o.phone = :phone AND o.provider LIKE:provider AND o.userId !=:idUserUpdate")
	Optional<User> findByPhoneAndProviderUpdate(@Param("phone") String phone, @Param("provider") String provider,
			@Param("idUserUpdate") int idUserUpdate);

	@Query("SELECT o FROM User o WHERE o.username = :username AND o.userId !=:idUserUpdate")
	Optional<User> findUsernameUpdate(@Param("username") String username, @Param("idUserUpdate") int idUserUpdate);

	Optional<User> findByUsernameAndProvider(String username, String provider);

	@Query("SELECT o FROM User o " +
		       "JOIN o.userRoles ur " + // Giả sử `userRoles` là danh sách các `Role` trong `User`
		       "JOIN ur.role r " + // Liên kết với bảng `Role`
		       "WHERE (o.username LIKE :keyword " +
		       "OR o.fullName LIKE :keyword " +
		       "OR o.email LIKE :keyword " +
		       "OR o.phone LIKE :keyword) " +
		       "AND o.status = :isStatus " +
		       "AND r.id = :roleId") // Điều kiện lọc theo `roleId`
		Page<User> getAllUserByKeyword(@Param("keyword") String keyword, 
		                                      @Param("isStatus") byte status, 
		                                      @Param("roleId") int roleId, 
		                                      Pageable pageable);


	@Query("SELECT o FROM User o " + "WHERE " + "o.status =:status")
	Page<User> getAllUserByKeywordAndStatus(@Param("status") byte status, Pageable pageable);
}
