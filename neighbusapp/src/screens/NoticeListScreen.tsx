// src/screens/NoticeListScreen.tsx
import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { RootStackScreenProps } from '../../App';

type Props = RootStackScreenProps<'NoticeList'>;

type Notice = {
  id: string;
  title: string;
  date: string;
};

const DUMMY_NOTICES: Notice[] = [
  {
    id: '1',
    title: '공지사항',
    date: '2025.12.15',
  },
  {
    id: '2',
    title: '서비스 점검 안내',
    date: '2025.12.14',
  },
  {
    id: '3',
    title: '이용약관 변경 안내',
    date: '2025.12.13',
  },
  {
    id: '4',
    title: '새로운 기능 업데이트',
    date: '2025.12.12',
  },
];

const NoticeListScreen: React.FC<Props> = ({ navigation }) => {
  const handleNoticePress = (noticeId: string) => {
    navigation.navigate('NoticeDetail', { noticeId });
  };

  const handleBack = () => {
    navigation.goBack();
  };

  const renderNotice = ({ item }: { item: Notice }) => (
    <TouchableOpacity
      style={styles.noticeItem}
      onPress={() => handleNoticePress(item.id)}
      activeOpacity={0.7}
    >
      <View style={styles.noticeContent}>
        <View style={styles.noticeBadge}>
          <Text style={styles.noticeBadgeText}>공지</Text>
        </View>
        <Text style={styles.noticeTitle}>{item.title}</Text>
      </View>
      <Text style={styles.noticeDate}>{item.date}</Text>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container}>
        {/* 헤더 */}
        <View style={styles.header}>
          <TouchableOpacity onPress={handleBack} style={styles.backButton}>
            <Text style={styles.backIcon}>←</Text>
          </TouchableOpacity>
          <Text style={styles.headerTitle}>공지사항</Text>
          <View style={styles.headerRight} />
        </View>

        {/* 공지사항 리스트 */}
        <FlatList
          data={DUMMY_NOTICES}
          renderItem={renderNotice}
          keyExtractor={(item) => item.id}
          contentContainerStyle={styles.listContainer}
          showsVerticalScrollIndicator={false}
        />
      </View>
    </SafeAreaView>
  );
};

export default NoticeListScreen;

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
  listContainer: {
    padding: 20,
  },
  noticeItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 12,
    shadowColor: '#5C4A3A',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  noticeContent: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  noticeBadge: {
    backgroundColor: '#9B7E5C',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginRight: 12,
  },
  noticeBadgeText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  noticeTitle: {
    fontSize: 16,
    fontWeight: '500',
    color: '#5C4A3A',
    flex: 1,
  },
  noticeDate: {
    fontSize: 13,
    color: '#8B7355',
    marginLeft: 12,
  },
});
