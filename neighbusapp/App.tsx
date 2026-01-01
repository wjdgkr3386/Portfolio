// App.tsx
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import {
  createNativeStackNavigator,
  NativeStackScreenProps,
} from '@react-navigation/native-stack';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { UserProvider } from './src/context/UserContext';

import WelcomeScreen from './src/screens/WelcomeScreen';
import SignupScreen from './src/screens/SignupScreen';
import LoginScreen from './src/screens/LoginScreen';
import MyPage from './src/screens/MyPage';
import ChatScreen from './src/screens/ChatScreen';
import HomeScreen from './src/screens/HomeScreen';
import ClubDetailScreen from './src/screens/ClubDetailScreen';
import MeetingDetailScreen from './src/screens/MeetingDetailScreen';
import CreateMeetingScreen from './src/screens/CreateMeetingScreen';
import NoticeListScreen from './src/screens/NoticeListScreen';
import NoticeDetailScreen from './src/screens/NoticeDetailScreen';
import FreeBoardScreen from './src/screens/FreeBoardScreen';
import GalleryScreen from './src/screens/GalleryScreen';
import FreeBoardDetailScreen from './src/screens/FreeBoardDetailScreen';
import FreeBoardWriteScreen from './src/screens/FreeBoardWriteScreen';
import GalleryDetailScreen from './src/screens/GalleryDetailScreen';
import GalleryWriteScreen from './src/screens/GalleryWriteScreen';
import ClubCreateScreen from './src/screens/ClubCreateScreen';

// 네비게이션 스택에 포함될 화면들의 타입을 정의합니다.
export type RootStackParamList = {
  Welcome: undefined;
  Signup: undefined;
  Login: undefined;
  MyPage: undefined;
  Chat: undefined;
  Home: undefined;
  FreeBoard: undefined;
  Gallery: undefined;
  FreeBoardWrite: undefined;
  GalleryWrite: undefined;
  ClubCreate: undefined;
  ClubDetail: { clubId: string };
  MeetingDetail: { meetingId: string; date: string };
  CreateMeeting: { clubId: string };
  FreeBoardDetail: { postId: string };
  GalleryDetail: { postId: string };
  NoticeList: undefined;
  NoticeDetail: { noticeId: string };
};

// 각 화면 컴포넌트에서 navigation, route prop의 타입을 지정하기 위한 헬퍼 타입을 만듭니다.
export type RootStackScreenProps<T extends keyof RootStackParamList> =
  NativeStackScreenProps<RootStackParamList, T>;

const Stack = createNativeStackNavigator<RootStackParamList>();

function App(): React.JSX.Element {
  return (
    <SafeAreaProvider>
      <UserProvider>
        <NavigationContainer>
          <Stack.Navigator initialRouteName="Login">
          <Stack.Screen
            name="Login"
            component={LoginScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="Home"
            component={HomeScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="FreeBoard"
            component={FreeBoardScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="Gallery"
            component={GalleryScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="FreeBoardWrite"
            component={FreeBoardWriteScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="GalleryWrite"
            component={GalleryWriteScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="ClubCreate"
            component={ClubCreateScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="Welcome"
            component={WelcomeScreen}
            options={{ title: '환영합니다' }}
          />
          <Stack.Screen
            name="Signup"
            component={SignupScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="MyPage"
            component={MyPage}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="Chat"
            component={ChatScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="ClubDetail"
            component={ClubDetailScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="MeetingDetail"
            component={MeetingDetailScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="CreateMeeting"
            component={CreateMeetingScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="FreeBoardDetail"
            component={FreeBoardDetailScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="GalleryDetail"
            component={GalleryDetailScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="NoticeList"
            component={NoticeListScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="NoticeDetail"
            component={NoticeDetailScreen}
            options={{ headerShown: false }}
          />
          </Stack.Navigator>
        </NavigationContainer>
      </UserProvider>
    </SafeAreaProvider>
  );
}

export default App;