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

	public PageImpl<UserResponse> getUserByKeyword(int page, int size, String keyword) {
		Sort sort = Sort.by(Sort.Direction.DESC, "userId");
		Pageable pageable = PageRequest.of(page, size, sort);

		Boolean isActive = keyword.equalsIgnoreCase("active") ? Boolean.TRUE
				: keyword.equalsIgnoreCase("inactive") ? Boolean.FALSE : null;

		byte statusNum = Byte.valueOf(Boolean.TRUE.equals(isActive) || isActive == null ? "1" : "0");

		Page<User> users = null;
		keyword = "%" + keyword + "%";

		if (isActive != null) {
			users = userJPA.getAllUserByKeywordAndStatus(statusNum, pageable);
		} else {
			users = userJPA.getAllUserByKeyword(keyword, pageable);
		}

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
