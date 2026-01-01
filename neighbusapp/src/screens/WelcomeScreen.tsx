// src/screens/WelcomeScreen.tsx
import React from 'react';
import { View, Text, Button, StyleSheet } from 'react-native';
import type { RootStackScreenProps } from '../../App';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'Welcome'>;

const WelcomeScreen: React.FC<Props> = ({ navigation }) => {
  const { user } = useUser();

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome 페이지</Text>

      {user ? (
        <Text style={styles.message}>{user.name}님, 환영합니다!</Text>
      ) : (
        <Text style={styles.message}>
          로그인 또는 회원가입을 진행해 주세요.
        </Text>
      )}

      <View style={styles.buttonContainer}>
        {/* 로그인 페이지로 이동 */}
        <Button
          title="로그인 하러 가기"
          onPress={() => navigation.navigate('Login')}
        />
      </View>

      <View style={styles.buttonContainer}>
        {/* 회원가입 페이지로 이동 */}
        <Button
          title="회원가입 하러 가기"
          onPress={() => navigation.navigate('Signup')}
        />
      </View>

      <View style={styles.buttonContainer}>
        {/* 메인 페이지로 이동 */}
        <Button
          title="메인 페이지로 이동"
          onPress={() => navigation.navigate('Home')}
        />
      </View>

      {user && (
        <View style={styles.section}>
          <Text style={styles.info}>
            (가입된 사용자: {user.id} / {user.name})
          </Text>
        </View>
      )}
    </View>
  );
};

export default WelcomeScreen;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 24,
    justifyContent: 'flex-start',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 24,
  },
  message: {
    fontSize: 18,
    marginBottom: 24,
    textAlign: 'center',
  },
  buttonContainer: {
    marginBottom: 12,
  },
  section: {
    marginTop: 32,
  },
  info: {
    marginTop: 8,
    fontSize: 13,
    color: '#555',
  },
});
