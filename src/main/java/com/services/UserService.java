package com.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.entities.Product;
import com.entities.User;
import com.entities.UserRole;
import com.repositories.UserJPA;
import com.responsedto.ProductResponse;
import com.responsedto.UserResponse;
import com.utils.DateUtils;
import com.utils.UploadService;

@Service
public class UserService {
	@Autowired
	UserJPA userJPA;

	@Autowired
	UploadService uploadService;

	DateUtils dateUtil = new DateUtils();

	public User getUserByUsername(String username) {
		return userJPA.getUserByUsername(username);
	}

	public User createUser(User user) {
		return userJPA.save(user);
	}

	public PageImpl<UserResponse> getUserByKeyword(int page, int size, String keyword, byte status, int role) {
		Sort sort = Sort.by(Sort.Direction.DESC, "userId");
		Pageable pageable = PageRequest.of(page, size, sort);
		keyword = "%" + keyword + "%";

		Page<User> users = userJPA.getAllUserByKeyword(keyword, status,role, pageable);

		//Chỉ lấy quyền là staff
		List<UserResponse> userResponses = users.getContent().stream().map(this::createUserResponse).toList();

		PageImpl<UserResponse> result = new PageImpl<UserResponse>(userResponses, pageable, users.getTotalElements());

		return result;
	}

	public UserResponse createUserResponse(User user) {
		UserResponse response = new UserResponse();

		response.setBirthday(dateUtil.formatDateString(user.getBirthday(), "dd/MM/yyyy"));
		response.setEmail(user.getEmail());
		response.setFullname(user.getFullName());
		response.setGender(user.getGender());
		response.setImage(uploadService.getUrlImage(user.getImage()));
		response.setPhone(user.getPhone());
		response.setStatus(user.getStatus());
		response.setUserId(user.getUserId());
		response.setUsername(user.getUsername());

		return response;
	}
}
