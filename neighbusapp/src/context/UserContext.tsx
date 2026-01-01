import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

type User = {
  id: string;
  uuid: string;
  nickname: string;
  username: string;
  name: string;
  image: string;
  grade: number;
} | null;

type UserContextType = {
  user: User;
  token: string | null;
  setUser: (user: User) => void;
  setToken: (token: string | null) => void;
};

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User>(null);
  const [token, setToken] = useState<string | null>(null);

  // 1. 앱 켰을 때: 저장소에서 데이터 복구 (로그인 유지)
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = await AsyncStorage.getItem('userToken');
      const storedUser = await AsyncStorage.getItem('userData');
      if (storedToken) setToken(storedToken);
      if (storedUser) setUser(JSON.parse(storedUser || 'null'));
    };
    initAuth();
  }, []);

  // 2. 로그인 성공 시 호출: 유저와 토큰을 한 번에 저장
  const login = async (userData: User, tokenData: string) => {
    setUser(userData);
    setToken(tokenData);
    await AsyncStorage.setItem('userData', JSON.stringify(userData));
    await AsyncStorage.setItem('userToken', tokenData);
  };

  // 3. 로그아웃 시 호출: 모든 정보 삭제
  const logout = async () => {
    setUser(null);
    setToken(null);
    await AsyncStorage.removeItem('userData');
    await AsyncStorage.removeItem('userToken');
  };

  return (
    <UserContext.Provider value={{ user, token, setUser, setToken }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const ctx = useContext(UserContext);
  if (!ctx) throw new Error('useUser는 UserProvider 안에서만 사용해야 합니다.');
  return ctx;
};