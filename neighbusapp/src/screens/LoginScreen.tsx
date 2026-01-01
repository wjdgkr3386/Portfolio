import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { RootStackScreenProps } from '../../App';
import { BASE_URL } from '../config'; // config 파일 위치 확인 필요
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'Login'>;

const LoginScreen: React.FC<Props> = ({ navigation }) => {
  const [id, setId] = useState('');
  const [pwd, setPwd] = useState('');
  const { setUser, setToken } = useUser();
  

  // ★ 서버 통신 로그인 함수
  const handleLogin = async () => {
    // 유효성 검사
    if (!id || !pwd) {
      Alert.alert('입력 오류', '아이디와 비밀번호를 모두 입력해 주세요.');
      return;
    }
    fetch(`${BASE_URL}/api/mobile/account/mobileLogin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: id,  // 서버: username
        password: pwd, // 서버: password
      }),
    })
      .then((response) => response.json()) // 응답을 JSON으로 파싱
      .then((result) => {
        if (result.status === 1) {
          // 성공: Context에 유저 정보 저장 후 홈으로 이동
          setUser(result.user);
          setToken(result.token);
          
          Alert.alert('로그인 성공', `${result.user.name}님 환영합니다!`, [
            { text: '확인', onPress: () => navigation.navigate('Home') },
          ]);
        } else {
          // 실패: 서버 메시지 출력
          Alert.alert('로그인 실패', result.message || '아이디 또는 비밀번호를 확인해주세요.');
        }
      })
      .catch((error) => {
        // 에러 처리
        console.error("Login Error:", error);
        Alert.alert('오류', '서버와 통신 중 문제가 발생했습니다.');
      });
  };

  const handleGoogleLogin = () => {
    Alert.alert('구글 로그인', '구글 로그인 기능은 준비 중입니다.');
  };

  const handleFindAccount = () => {
    Alert.alert('안내', '아이디/비밀번호 찾기 기능은 준비 중입니다.');
  };

  const handleSignup = () => {
    navigation.navigate('Signup');
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.container}
      >
        <View style={styles.innerContainer}>
          {/* 헤더 영역 */}
          <View style={styles.header}>
            <Text style={styles.titleKorean}>함께하는 이웃</Text>
            <Text style={styles.titleEnglish}>NEIGHBUS</Text>
            <Text style={styles.subtitle}>로그인하고 동네 친구들을 만나보세요</Text>
          </View>

          {/* 로그인 폼 */}
          <View style={styles.formContainer}>
            {/* 아이디 입력 */}
            <View style={styles.inputGroup}>
              <Text style={styles.label}>아이디</Text>
              <TextInput
                style={styles.input}
                placeholder="아이디를 입력하세요"
                placeholderTextColor="#B8B8B8"
                value={id}
                onChangeText={setId}
                autoCapitalize="none"
              />
            </View>

            {/* 비밀번호 입력 */}
            <View style={styles.inputGroup}>
              <Text style={styles.label}>비밀번호</Text>
              <TextInput
                style={styles.input}
                placeholder="비밀번호를 입력하세요"
                placeholderTextColor="#B8B8B8"
                value={pwd}
                onChangeText={setPwd}
                secureTextEntry
              />
            </View>

            {/* 로그인 버튼 */}
            <TouchableOpacity
              style={styles.loginButton}
              onPress={handleLogin}
              activeOpacity={0.8}
            >
              <Text style={styles.loginButtonText}>로그인</Text>
            </TouchableOpacity>

            {/* 구글 로그인 버튼 */}
            <TouchableOpacity
              style={styles.googleButton}
              onPress={handleGoogleLogin}
              activeOpacity={0.8}
            >
              <Text style={styles.googleIcon}>G</Text>
              <Text style={styles.googleButtonText}>구글 아이디로 로그인</Text>
            </TouchableOpacity>
          </View>

          {/* 하단 링크 */}
          <View style={styles.footer}>
            <TouchableOpacity onPress={handleFindAccount}>
              <Text style={styles.footerText}>아이디 / 비밀번호 찾기</Text>
            </TouchableOpacity>
            <Text style={styles.divider}>|</Text>
            <TouchableOpacity onPress={handleSignup}>
              <Text style={styles.footerText}>회원가입</Text>
            </TouchableOpacity>
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

export default LoginScreen;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F5EDE4',
  },
  container: {
    flex: 1,
  },
  innerContainer: {
    flex: 1,
    paddingHorizontal: 24,
    justifyContent: 'center',
    maxWidth: 500,
    width: '100%',
    alignSelf: 'center',
  },
  header: {
    alignItems: 'center',
    marginBottom: 40,
    paddingTop: 20,
  },
  titleKorean: {
    fontSize: 28,
    fontWeight: '700',
    color: '#5C4A3A',
    marginBottom: 2,
  },
  titleEnglish: {
    fontSize: 36,
    fontWeight: '700',
    color: '#5C4A3A',
    marginBottom: 12,
    letterSpacing: 1,
  },
  subtitle: {
    fontSize: 13,
    color: '#8B7355',
    marginTop: 6,
  },
  formContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 28,
    shadowColor: '#5C4A3A',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 5,
  },
  inputGroup: {
    marginBottom: 18,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#5C4A3A',
    marginBottom: 6,
  },
  input: {
    backgroundColor: '#FAFAFA',
    borderWidth: 1.5,
    borderColor: '#D8D0C8',
    borderRadius: 10,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 15,
    color: '#333333',
  },
  loginButton: {
    backgroundColor: '#9B7E5C',
    borderRadius: 10,
    paddingVertical: 15,
    alignItems: 'center',
    marginTop: 8,
    marginBottom: 14,
    shadowColor: '#9B7E5C',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 3,
  },
  loginButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 0.5,
  },
  googleButton: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#D8D0C8',
    borderRadius: 10,
    paddingVertical: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  googleIcon: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#4285F4',
    marginRight: 8,
  },
  googleButtonText: {
    color: '#5C4A3A',
    fontSize: 14,
    fontWeight: '500',
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 20,
    paddingBottom: 20,
  },
  footerText: {
    fontSize: 13,
    color: '#8B7355',
  },
  divider: {
    fontSize: 13,
    color: '#8B7355',
    marginHorizontal: 10,
  },
});