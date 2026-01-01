package com.neighbus.account;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface AccountService{

	int insertSignup(AccountDTO accountDTO);
	
	UserDetails loadUserByUsername(String username) throws Exception;

	List<Map<String, Object>> getProvince();

	List<Map<String, Object>> getCity();
	
	Map<String,Object> findAccountByEmail(AccountFindDTO accountFindDTO);
	
	void sendTempPassword(String email);
	
	void updatePassword(String password, String email);
	
	String findUsernameByEmail(String email);

	Map<String,Object> findAccountByPhone(AccountFindDTO accountFindDTO);
	
	void sendTempPasswordByPhone(String phone, String sendMessage);
	
	void updatePasswordByPhone(String phone);
	
	int updatePwd(Map<String, Object> map);
	
	void updateGrade(AccountDTO accountDTO);
}