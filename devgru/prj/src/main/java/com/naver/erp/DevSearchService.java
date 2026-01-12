package com.naver.erp;

import java.util.Map;


public interface DevSearchService {
	 
	DevDTO getDev( int d_no);
	DevDTO getDevDetail(String dev_uid) throws Exception; 
}









