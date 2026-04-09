package com.javaweb.component;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.javaweb.dto.UserDTO;

//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class NewAPI {
	@GetMapping(value = "/api/test")
	public UserDTO getUser1(@RequestBody UserDTO User) {
		///solving database
		System.out.println("Hello World");
		UserDTO temp = new UserDTO();
		return temp;
	}
	
	@GetMapping(value = "/api/test-users")
	public Object getUser2(@RequestBody UserDTO User) {	
		UserDTO temp = new UserDTO();
		return temp;
	}
	
}
