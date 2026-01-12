package com.naver.erp;


public interface FreelancerSearchService {
	 
	FreelancerDTO getFreelancer( int f_no);
	FreelancerDTO getFreeDetail(String free_uid) throws Exception;
	 
}









