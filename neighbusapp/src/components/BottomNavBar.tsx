import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';

// 더 구체적인 모양의 아이콘을 그리는 컴포넌트
const CustomIcon = ({ name, color }: { name: string; color: string }) => {
  const iconStyle = { backgroundColor: color };

  switch (name) {
    case 'Home': // 동아리 -> 집 모양
      return (
        <View style={styles.iconContainer}>
          <View style={[styles.houseRoof, { borderBottomColor: color }]} />
          <View style={[styles.houseBody, iconStyle]} />
        </View>
      );
    case 'Gallery': // 갤러리 -> 액자 속 풍경
      return (
        <View style={styles.iconContainer}>
          <View style={[styles.galleryFrame, { borderColor: color }]}>
            <View style={[styles.galleryMountain, { borderBottomColor: color }]} />
            <View style={[styles.gallerySun, iconStyle]} />
          </View>
        </View>
      );
    case 'Freeboard': // 게시판 -> 문서 모양
      return (
        <View style={styles.iconContainer}>
          <View style={[styles.boardPage, { borderColor: color }]}>
            <View style={[styles.boardLine, iconStyle]} />
            <View style={[styles.boardLine, iconStyle, { width: 10 }]} />
            <View style={[styles.boardLine, iconStyle, { width: 8 }]} />
          </View>
        </View>
      );
    case 'Chat': // 채팅 -> 말풍선
      return (
        <View style={styles.iconContainer}>
          <View style={[styles.chatBubble, iconStyle]}>
            <View style={[styles.chatBubbleTail, { borderTopColor: color }]} />
          </View>
        </View>
      );
    case 'MyPage': // 마이페이지 -> 사람 상반신
      return (
        <View style={styles.iconContainer}>
          <View style={[styles.myPageHead, iconStyle]} />
          <View style={[styles.myPageShoulder, iconStyle]} />
        </View>
      );
    default:
      return <View style={styles.iconContainer} />;
  }
};

const navItems = [
  { key: 'Home', route: 'Home', text: '동아리' },
  { key: 'Gallery', route: 'Gallery', text: '갤러리' },
  { key: 'Freeboard', route: 'FreeBoard', text: '게시판' },
  { key: 'Chat', route: 'Chat', text: '채팅' },
  { key: 'MyPage', route: 'MyPage', text: '마이' },
];

const BottomNavBar: React.FC<{ currentScreen: string }> = ({ currentScreen }) => {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<any>();

  return (
    <View style={[styles.wrapper, { bottom: insets.bottom > 0 ? insets.bottom - 5 : 15 }]}>
      <View style={styles.container}>
        {navItems.map((item) => {
          const isActive = currentScreen === item.key;
          const activeColor = '#5C4A3A';
          const inactiveColor = '#B8A99A';
          const color = isActive ? activeColor : inactiveColor;

          return (
            <TouchableOpacity
              key={item.key}
              style={styles.tabButton}
              onPress={() => navigation.navigate(item.route)}
              activeOpacity={0.7}
            >
              <CustomIcon name={item.key} color={color} />
              <Text style={[styles.label, { color, fontWeight: isActive ? '700' : '400' }]}>
                {item.text}
              </Text>
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  wrapper: {
    position: 'absolute',
    left: 20,
    right: 20,
  },
  container: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderRadius: 35,
    height: 70,
    width: '100%',
    ...Platform.select({
      ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 5 }, shadowOpacity: 0.12, shadowRadius: 12 },
      android: { elevation: 12 },
    }),
  },
  tabButton: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  label: {
    fontSize: 10,
    marginTop: 5,
  },
  iconContainer: {
    width: 28,
    height: 28,
    justifyContent: 'center',
    alignItems: 'center',
    overflow: 'hidden', // For complex shapes
  },
  // --- Icon Specific Styles ---
  houseRoof: {
    width: 0,
    height: 0,
    borderStyle: 'solid',
    borderLeftWidth: 11,
    borderRightWidth: 11,
    borderBottomWidth: 8,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    position: 'absolute',
    top: 4,
  },
  houseBody: {
    width: 16,
    height: 10,
    position: 'absolute',
    bottom: 6,
  },
  galleryFrame: {
    width: 22,
    height: 18,
    borderWidth: 2,
    borderRadius: 2,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  galleryMountain: {
    width: 0,
    height: 0,
    borderStyle: 'solid',
    borderLeftWidth: 7,
    borderRightWidth: 7,
    borderBottomWidth: 8,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    position: 'absolute',
    bottom: 0,
    left: 2,
  },
  gallerySun: {
    width: 5,
    height: 5,
    borderRadius: 2.5,
    position: 'absolute',
    top: 2,
    right: 2,
  },
  boardPage: {
    width: 20,
    height: 22,
    borderWidth: 2,
    borderRadius: 2,
    justifyContent: 'space-evenly',
    alignItems: 'center',
    paddingVertical: 3,
  },
  boardLine: {
    width: 12,
    height: 2,
    borderRadius: 1,
  },
  chatBubble: {
    width: 22,
    height: 18,
    borderRadius: 8,
  },
  chatBubbleTail: {
    width: 0,
    height: 0,
    borderStyle: 'solid',
    borderLeftWidth: 0,
    borderRightWidth: 8,
    borderTopWidth: 8,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    position: 'absolute',
    bottom: -4,
    left: 4,
    transform: [{ rotate: '-30deg' }],
  },
  myPageHead: {
    width: 10,
    height: 10,
    borderRadius: 5,
    position: 'absolute',
    top: 4,
  },
  myPageShoulder: {
    width: 20,
    height: 15,
    position: 'absolute',
    bottom: 0,
    borderTopLeftRadius: 10,
    borderTopRightRadius: 10,
  },
});

export default BottomNavBar;
