package com.neighbus.account;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AccountMapper {

	//지역 꺼내오기
	List<Map<String, Object>> getProvince();
	List<Map<String, Object>> getCity();
	
	//회원가입
	int checkUsername(AccountDTO accountDTO);
	int checkPhone(AccountDTO accountDTO);
	int checkEmail(AccountDTO accountDTO);
	int insertSignup(AccountDTO accountDTO);
	
	//로그인
	AccountDTO getUser(String username);

	//계정찾기
	int findAccountByEmail(AccountFindDTO accountFindDTO);
    int findAccountByPhone(AccountFindDTO accountFindDTO);
    String getEmailByPhone(String phone);
	
	//비밀번호 변경
    @Update("UPDATE users SET password = #{password} WHERE email = #{email}")
    void updatePassword(@Param("password") String password, @Param("email") String email);
    @Select("SELECT username FROM users WHERE email = #{email}")
    String findUsernameByEmail(String email);
    
    
	// 통계
	int countUsers();
	int countViews();
	int countHistory();
	
	
	void updateSocialInfo(AccountDTO AccountDTO);

	//사용자 정지
	void blockUser(Map<String, Object> map);
	//사용자 정지 해제
	void unblockUser();

	@Select("SELECT * FROM users WHERE phone = #{phone}")
	AccountDTO getAccountByPhone(@Param("phone") String phone);

	@Update("UPDATE users SET grade = #{grade} WHERE id = #{id}")
	void updateGrade(@Param("id") Integer id, @Param("grade") Integer grade);

	@Select("SELECT id FROM users WHERE username = #{username}")
    int findIdByUsername(String username);
	
	void updatePwd(Map<String, Object> map);
	
	AccountDTO findByEmail(String email);
}
