// src/screens/NoticeDetailScreen.tsx
import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { RootStackScreenProps } from '../../App';

type Props = RootStackScreenProps<'NoticeDetail'>;

type NoticeDetail = {
  id: string;
  title: string;
  author: string;
  date: string;
  content: string;
};

const DUMMY_NOTICE_DETAILS: { [key: string]: NoticeDetail } = {
  '1': {
    id: '1',
    title: '공지사항',
    author: '관리자',
    date: '2025.12.16',
    content: '1번',
  },
  '2': {
    id: '2',
    title: '서비스 점검 안내',
    author: '관리자',
    date: '2025.12.14',
    content: '안녕하세요.\n\n다음 주 월요일 새벽 2시부터 4시까지 서비스 점검이 있을 예정입니다.\n\n점검 시간 동안에는 서비스 이용이 불가능하오니 참고 부탁드립니다.\n\n감사합니다.',
  },
  '3': {
    id: '3',
    title: '이용약관 변경 안내',
    author: '관리자',
    date: '2025.12.13',
    content: '이용약관이 변경되었습니다.\n\n주요 변경 사항은 다음과 같습니다:\n- 개인정보 처리방침 업데이트\n- 서비스 이용 정책 개선\n\n자세한 내용은 앱 설정 > 이용약관에서 확인하실 수 있습니다.',
  },
  '4': {
    id: '4',
    title: '새로운 기능 업데이트',
    author: '관리자',
    date: '2025.12.12',
    content: '새로운 기능이 추가되었습니다!\n\n- 친구 추가 기능 개선\n- 알림 설정 기능 추가\n- UI/UX 개선\n\n많은 이용 부탁드립니다.',
  },
};

const NoticeDetailScreen: React.FC<Props> = ({ route, navigation }) => {
  const { noticeId } = route.params;
  const notice = DUMMY_NOTICE_DETAILS[noticeId] || DUMMY_NOTICE_DETAILS['1'];

  const handleBackToList = () => {
    navigation.goBack();
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        {/* 헤더 */}
        <View style={styles.header}>
          <TouchableOpacity onPress={handleBackToList} style={styles.backButton}>
            <Text style={styles.backIcon}>←</Text>
          </TouchableOpacity>
          <Text style={styles.headerTitle}>공지사항</Text>
          <View style={styles.headerRight} />
        </View>

        <ScrollView style={styles.contentContainer} showsVerticalScrollIndicator={false}>
          {/* 공지사항 상세 */}
          <View style={styles.noticeCard}>
            {/* 제목 */}
            <Text style={styles.noticeTitle}>{notice.title}</Text>

            {/* 작성자와 날짜 */}
            <View style={styles.noticeInfo}>
              <Text style={styles.noticeInfoText}>
                작성자: <Text style={styles.noticeInfoValue}>{notice.author}</Text>
              </Text>
              <Text style={styles.noticeInfoText}>
                작성일: <Text style={styles.noticeInfoValue}>{notice.date}</Text>
              </Text>
            </View>

            {/* 구분선 */}
            <View style={styles.divider} />

            {/* 내용 */}
            <View style={styles.contentSection}>
              <Text style={styles.contentText}>{notice.content}</Text>
            </View>

            {/* 목록으로 버튼 */}
            <TouchableOpacity
              style={styles.listButton}
              onPress={handleBackToList}
              activeOpacity={0.8}
            >
              <Text style={styles.listButtonText}>목록으로</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
};

export default NoticeDetailScreen;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F5EDE4',
  },
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#F5EDE4',
  },
  backButton: {
    padding: 4,
    width: 40,
  },
  backIcon: {
    fontSize: 24,
    color: '#5C4A3A',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#5C4A3A',
    flex: 1,
    textAlign: 'center',
  },
  headerRight: {
    width: 40,
  },
  contentContainer: {
    flex: 1,
    padding: 20,
  },
  noticeCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 24,
    shadowColor: '#5C4A3A',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  noticeTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#5C4A3A',
    marginBottom: 16,
  },
  noticeInfo: {
    marginBottom: 16,
  },
  noticeInfoText: {
    fontSize: 13,
    color: '#8B7355',
    marginBottom: 6,
  },
  noticeInfoValue: {
    fontWeight: '600',
    color: '#5C4A3A',
  },
  divider: {
    height: 1,
    backgroundColor: '#E5E5E5',
    marginVertical: 20,
  },
  contentSection: {
    marginBottom: 32,
  },
  contentText: {
    fontSize: 15,
    color: '#5C4A3A',
    lineHeight: 24,
  },
  listButton: {
    backgroundColor: '#9B7E5C',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 32,
    alignItems: 'center',
    alignSelf: 'flex-start',
    shadowColor: '#9B7E5C',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 3,
  },
  listButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
