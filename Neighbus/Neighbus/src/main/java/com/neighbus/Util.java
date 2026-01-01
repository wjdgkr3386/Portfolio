package com.neighbus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import com.neighbus.gallery.GalleryDTO;

public class Util {

	//파일 재입력 메소드
	public static int saveFileToDirectory(GalleryDTO gelleryDTO, String folderPath) {
		System.out.println("Util - saveFileToDirectory");
		List<MultipartFile> fileList = gelleryDTO.getFileList();
		List<String> fileNameList = gelleryDTO.getFileNameList();
		int status = 0;
		try {
			//이미지 저장
			status = fileSave(folderPath, fileList, fileNameList);
			//DTO에 이미지 이름 저장
			gelleryDTO.setFileNameList(fileNameList);
		}catch(Exception e) {
			System.out.println(e);
		}
		return status;
	}
	
	//지정된 경로에 파일 저장하는 메소드
	public static int fileSave( String folderPath, List<MultipartFile> fileList, List<String> fileNameList) {
		System.out.println("Util - fileSave");
		//폴더가 없으면 생성
		File folder = new File(folderPath);
		if ( !folder.exists() ) { folder.mkdirs(); }
		
		if( fileList!=null && !fileList.isEmpty() ) {
			for( MultipartFile file : fileList ) {
				String originalFileName = file.getOriginalFilename();
	            if(extensionCheck(originalFileName)==-13) { return -13; }
	            
	            //업로드된 파일을 지정된 경로에 저장
	            String uuid = UUID.randomUUID().toString()+".png";
	            File dest = new File(folderPath, uuid);
	
	            try {
	            	file.transferTo(dest);
	            	fileNameList.add(uuid);
	            }catch(IOException e) {
	    	        System.err.println("Exception occurred at: " + e.getStackTrace()[0]);
	    	        e.printStackTrace();
	            }
			}
		}
		return 1;
	}

	//확장자 체크
	public static int extensionCheck( String originalFileName) {
		System.out.println("Util - extensionCheck");
		String[] allowedExtensions = {"jpg", "jpeg", "jfif", "png"};
		String extension = "";
		
        //확장자 확인
        if (originalFileName != null && originalFileName.lastIndexOf(".") != -1) {
        	extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        }
        
		boolean checkExtension = Arrays.asList(allowedExtensions).contains(extension.toLowerCase());
        if(!checkExtension) {
        	return -13;
        }
		return 1;
	}
	
	//경로에 있는 파일 삭제 메서드
	public static void fileDelete(String folderPath, String fileName) {
		System.out.println("Util - fileDelete");
		File file = new File(folderPath, fileName);
	    
		if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println(fileName + " 파일이 삭제되었습니다.");
            } else {
                System.out.println(fileName + " 파일 삭제에 실패했습니다.");
            }
        } else {
            System.out.println(fileName + " 파일이 존재하지 않습니다.");
        }
	}
	
	//보기 좋게 출력
	public static void print(Object obj) {
        print(obj, 0);
        System.out.println(); // 마지막에 줄바꿈
    }
	private static void print(Object obj, int indent) {
        String indentStr = "    ".repeat(indent);

        if (obj == null) {
            System.out.print("null");
        } 
        else if (obj instanceof Map<?, ?> map) {
            System.out.println(indentStr+"{");
            int count = 0;
            int size = map.size();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                System.out.print(indentStr + "  " + entry.getKey() + ": ");
                print(entry.getValue(), indent + 1);
                count++;
                if (count < size) System.out.println(",");
                else System.out.println();
            }
            System.out.print(indentStr + "}");
        } 
        else if (obj instanceof List<?> list) {
            System.out.println("[");
            for (int i = 0; i < list.size(); i++) {
                print(list.get(i), indent + 1);
                if (i < list.size() - 1) System.out.println(",");
                else System.out.println();
            }
            System.out.print(indentStr + "]");
        }
        else if (obj.getClass().isArray()) {
            System.out.println("[");
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                print(Array.get(obj, i), indent + 1);
                if (i < length - 1) System.out.println(",");
                else System.out.println();
            }
            System.out.print(indentStr + "]");
        } 
        else if (obj instanceof String str) {
            System.out.print("\"" + str + "\"");
        } 
        else {
            System.out.print(obj);
        }
    }
	
	
	// 검색 결과                                         게시글 전체 개수,      선택된 페이지 번호,    한번에 보여질 행의 개수
	public static Map<String, Integer> searchUtil(int searchCnt, int selectPageNo, int rowCnt) {
	    Map<String ,Integer> map = new HashMap<String, Integer>();

	    try {
	        int beginPageNo;                                        //보여줄 시작 페이지 번호
	        int endPageNo;                                          //보여줄 끝 페이지 번호
	        int beginRowNo;                                         //보여줄 시작 행 번호
	        int endRowNo;                                           //보여줄 끝 행 번호
	        int pageAllCnt;                                         //페이지 전체 개수
	        int pageBlock = 10;                                     //한번에 보여줄 페이지 번호 개수
	    
	        if(rowCnt <= 0) { rowCnt = 10; }
	        
	        if(searchCnt > 0) {
	            pageAllCnt = (int)Math.ceil((double)searchCnt / rowCnt);
	            if(selectPageNo < 1) { selectPageNo = 1; }
	            if(selectPageNo > pageAllCnt) { selectPageNo = pageAllCnt; }
	            beginRowNo = (selectPageNo - 1) * rowCnt;
	            endRowNo = selectPageNo*rowCnt;
	            if(endRowNo > searchCnt) { endRowNo = searchCnt; }
	            beginPageNo = selectPageNo - (pageBlock/2);
	            endPageNo = beginPageNo + pageBlock;
	            if(beginPageNo<1) { beginPageNo = 1; }
	            if(endPageNo > pageAllCnt) { endPageNo = pageAllCnt; }

	            map.put("searchCnt", searchCnt);
	            map.put("selectPageNo", selectPageNo);
	            map.put("rowCnt", rowCnt);
	            map.put("beginPageNo", beginPageNo);
	            map.put("endPageNo", endPageNo);
	            map.put("beginRowNo", beginRowNo);
	            map.put("endRowNo", endRowNo);
	            map.put("pageAllCnt", pageAllCnt); // ★ 추가됨: 전체 페이지 수 저장
	        } else {
	            map.put("searchCnt", searchCnt);
	            map.put("selectPageNo", 1);
	            map.put("rowCnt", rowCnt);
	            map.put("beginPageNo", 1);
	            map.put("endPageNo", 1);
	            map.put("beginRowNo", 0);
	            map.put("endRowNo", 1);
	            map.put("pageAllCnt", 1); // ★ 추가됨: 데이터 없으면 전체 페이지 1
	        }
	        return map;
	    } catch(Exception e) {
	        System.out.println("Util - searchUtil 오류 발생");
	        System.out.println(e);
	        return new HashMap<String, Integer>();
	    }
	}
	
    //맵을 받아서 안에 있는 내용 중에 < , > , <br> 을 html에서 사용할 수 있게 변환하여 저장하고 반환
	//사용법 : 매개변수로 Map<String,Object> 형태의 객체와 "<br>" 거나 "\n" 인 문자열을 받는다. 
    public static void convertAngleBracketsMap(Map<String, Object> convertMap, String keyword){
        if(keyword.equals("<br>")) {
            for (Map.Entry<String, Object> entry : convertMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    String sanitizedValue = value.toString()
                            .replaceAll(" ", "&nbsp;")
                            .replaceAll("<", "&lt;")
                            .replaceAll(">", "&gt;");
                    sanitizedValue = sanitizedValue.replaceAll("\n", "<br>");
                    entry.setValue(sanitizedValue);
                }
            }
        }else if(keyword.equals("\n")) {
            for (Map.Entry<String, Object> entry : convertMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    String sanitizedValue = value.toString()
                        .replaceAll("<", "&lt;")
                        .replaceAll(">", "&gt;");
                    entry.setValue(sanitizedValue);
                }
            }
        }
    }

    //String을 받아서 안에 있는 내용 중에 < , > , <br> 을 html에서 사용할 수 있게 변환하고 반환
	//사용법 : 매개변수로 String 형태의 객체와 "<br>" 거나 "\n" 인 문자열을 받는다.
    public static String convertAngleBracketsString(String value, String keyword) {
        if(value == null) return null;
        String sanitizedValue = value;
        if(keyword.equals("<br>")) {
            sanitizedValue = sanitizedValue.replaceAll(" ", "&nbsp;")
                                           .replaceAll("<", "&lt;")
                                           .replaceAll(">", "&gt;")
                                           .replaceAll("\n", "<br>");
        } else if(keyword.equals("\n")) {
            sanitizedValue = sanitizedValue.replaceAll("<", "&lt;")
                                           .replaceAll(">", "&gt;");
        }
        return sanitizedValue;
    }
    
	//메일 보내기
	public static void sendVerificationCode(String to, String title, String content, JavaMailSender mailSender) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(title);
        message.setText(content);
        mailSender.send(message);
	}
	
	//랜덤 숫자 6자리 코드를 문자열로 출력
    public static String generate6DigitCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10); // 0 ~ 9
            sb.append(digit);
        }

        return sb.toString();
    }
    
    public static String rCode(int length) {
        final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        final Random RANDOM = new Random();
        
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = ALPHABET[RANDOM.nextInt(ALPHABET.length)];
            code.append(c);
        }
        return code.toString();
    }
    
    public static String s3Key() {
    	return "images/" + UUID.randomUUID() + "_" + Util.rCode(10);
    }
}
