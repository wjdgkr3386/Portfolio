// src/screens/FreeBoardScreen.tsx
import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  FlatList,
  Platform,
  PanResponder,
  ActivityIndicator,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Picker } from '@react-native-picker/picker';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import type { RootStackScreenProps } from '../../App';
import SideMenu from '../components/SideMenu';
import BottomNavBar from '../components/BottomNavBar';
import theme from '../styles/theme';
import { useUser } from '../context/UserContext';
import { BASE_URL } from '../config';

type Props = RootStackScreenProps<'FreeBoard'>;

type Post = {
  id: string;
  category: string;
  title: string;
  content: string; // API에서 내용을 주지 않으므로 제목으로 대체하거나 비워둠
  author: string;
  date: string;
  views: number;
  comments: number; // API 미제공
  likes: number;    // API 미제공
};

type Club = {
  id: number;
  clubName: string;
};

const FreeBoardScreen: React.FC<Props> = ({ navigation }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showSideMenu, setShowSideMenu] = useState(false);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [myClubs, setMyClubs] = useState<Club[]>([]);
  const [selectedClubId, setSelectedClubId] = useState<number>(0);
  const { token } = useUser();

  const fetchPosts = useCallback(async () => {
    setLoading(true);
    try {
      const keywordParam = searchQuery.trim() ? `&keyword=${encodeURIComponent(searchQuery.trim())}` : '';
      const clubParam = `&clubId=${selectedClubId}`;
      const url = `${BASE_URL}/api/mobile/freeboard/list?${keywordParam}${clubParam}`;
      
      const response = await fetch(url, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await response.json();

      if (data.success) {
        if (Array.isArray(data.posts)) {
          const mappedPosts: Post[] = data.posts.map((item: any) => ({
            id: item.id.toString(),
            category: item.clubName || '자유',
            title: item.title,
            content: '', 
            author: item.writerNickname || '익명',
            date: item.createdAt ? item.createdAt.split('T')[0] : '', 
            views: item.viewCount || 0,
            comments: item.commentCount || 0,
            likes: item.likeCount || 0,
          }));
          setPosts(mappedPosts);
        }
        if (Array.isArray(data.myClubList)) {
          setMyClubs(data.myClubList);
        }
      }
    } catch (error) {
      console.error('Failed to fetch freeboard posts:', error);
    } finally {
      setLoading(false);
    }
  }, [token, searchQuery, selectedClubId]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => false,
      onMoveShouldSetPanResponder: (evt, gestureState) => Math.abs(gestureState.dx) > 10,
      onPanResponderRelease: (evt, gestureState) => {
        if (gestureState.dx > 30) setShowSideMenu(true);
      },
    })
  ).current;

  const handleWritePost = () => navigation.navigate('FreeBoardWrite');
  const handlePostClick = (post: Post) => navigation.navigate('FreeBoardDetail', { postId: post.id });

  const renderPostItem = ({ item }: { item: Post }) => (
    <TouchableOpacity
      style={styles.postCard}
      onPress={() => handlePostClick(item)}
      activeOpacity={0.8}
    >
      <View style={styles.categoryBadge}>
        <Text style={styles.categoryBadgeText}>{item.category}</Text>
      </View>
      <Text style={styles.postTitle} numberOfLines={2}>{item.title}</Text>
      {item.content ? (
        <Text style={styles.postSnippet} numberOfLines={2}>{item.content}</Text>
      ) : null}
      <View style={styles.postCardFooter}>
        <View style={styles.authorContainer}>
          <Icon name="account" size={16} color={theme.colors.textLight} style={styles.authorIcon} />
          <Text style={styles.authorText}>{item.author} · {item.date}</Text>
        </View>
        <View style={styles.statsContainer}>
          <Text style={styles.statText}>❤️ {item.likes}</Text>
          <Text style={styles.statText}>💬 {item.comments}</Text>
          <Text style={styles.statText}>👁️ {item.views}</Text>
        </View>
      </View>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container} {...panResponder.panHandlers}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>자유게시판</Text>
          <View style={styles.headerPickerWrapper}>
            <Picker
              selectedValue={selectedClubId}
              onValueChange={(itemValue) => setSelectedClubId(itemValue)}
              style={styles.picker}
              dropdownIconColor={theme.colors.primary}
            >
              <Picker.Item label="전체 동아리" value={0} />
              {myClubs.map((club) => (
                <Picker.Item key={club.id} label={club.clubName} value={club.id} />
              ))}
            </Picker>
          </View>
        </View>

        <View style={styles.controlsContainer}>
          <View style={styles.searchInputWrapper}>
            <Text style={styles.searchIcon}>🔍</Text>
            <TextInput
              style={styles.searchInput}
              placeholder="관심있는 글을 검색해보세요..."
              placeholderTextColor={theme.colors.textLight}
              value={searchQuery}
              onChangeText={setSearchQuery}
              onSubmitEditing={fetchPosts}
              returnKeyType="search"
            />
          </View>
        </View>

        {loading ? (
          <ActivityIndicator size="large" color={theme.colors.primary} style={{ marginTop: 20 }} />
        ) : (
          <FlatList
            data={posts}
            renderItem={renderPostItem}
            keyExtractor={item => item.id}
            contentContainerStyle={styles.listContainer}
            showsVerticalScrollIndicator={false}
            ItemSeparatorComponent={() => <View style={styles.separator} />}
            ListEmptyComponent={
              <View style={{ padding: 20, alignItems: 'center' }}>
                <Text style={{ color: theme.colors.textSecondary }}>게시글이 없습니다.</Text>
              </View>
            }
          />
        )}
      </View>

      <TouchableOpacity
        style={styles.fab}
        onPress={handleWritePost}
        activeOpacity={0.8}
      >
        <Text style={styles.fabIcon}>+</Text>
      </TouchableOpacity>

      <BottomNavBar currentScreen="Freeboard" />

      <SideMenu visible={showSideMenu} onClose={() => setShowSideMenu(false)} navigation={navigation} />
    </SafeAreaView>
  );
};

export default FreeBoardScreen;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFF8F0',
  },
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 8,
    backgroundColor: '#FFF8F0',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: '800',
    color: theme.colors.textPrimary,
  },
  headerPickerWrapper: {
    backgroundColor: theme.colors.white,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: theme.colors.borderColor,
    width: 160,
    height: 55, // Increased height
    justifyContent: 'center',
    overflow: 'hidden',
  },
  controlsContainer: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFF8F0',
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.borderColor,
  },
  picker: {
    height: Platform.OS === 'ios' ? 150 : 55, // Match wrapper height
    width: '100%',
    color: theme.colors.textPrimary,
  },
  searchInputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
    borderRadius: 10,
    paddingHorizontal: 12,
    borderWidth: 1,
    borderColor: theme.colors.borderColor,
  },
  searchIcon: {
    fontSize: 16,
    color: theme.colors.textLight,
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
    paddingVertical: 10,
    paddingHorizontal: 8,
    color: theme.colors.textPrimary,
  },
  sortButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: theme.colors.white,
    paddingHorizontal: 12,
    paddingVertical: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: theme.colors.borderColor,
  },
  sortButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: theme.colors.textPrimary,
  },
  sortButtonIcon: {
    fontSize: 10,
    marginLeft: 6,
    color: theme.colors.textSecondary,
  },
  listContainer: {
    paddingBottom: 100,
  },
  postCard: {
    backgroundColor: theme.colors.white,
    padding: 20, // Increased padding
  },
  categoryBadge: {
    backgroundColor: '#F5EDE4', // 연한 브라운 배경
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6, // 뭉툭한 모서리
    alignSelf: 'flex-start', // 텍스트 크기에 맞춤
    marginBottom: 10,
  },
  categoryBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: theme.colors.primary,
  },
  postTitle: {
    fontSize: 18, // Increased from 16
    fontWeight: '800', // Made bolder
    color: theme.colors.textPrimary,
    marginBottom: 8,
    lineHeight: 24,
  },
  postSnippet: {
    fontSize: 14,
    color: theme.colors.textSecondary,
    lineHeight: 20,
    marginBottom: 14,
  },
  postCardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
  },
  authorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  authorIcon: {
    marginRight: 4,
  },
  authorText: {
    fontSize: 12,
    fontWeight: '500',
    color: theme.colors.textLight,
  },
  statsContainer: {
    flexDirection: 'row',
    gap: 10,
  },
  statText: {
    fontSize: 12,
    color: theme.colors.textLight,
  },
  separator: {
    height: 1, // Thinner separator for a cleaner list
    backgroundColor: '#E8D7C3',
    marginHorizontal: 16,
  },
  fab: {
    position: 'absolute',
    right: 25,
    bottom: 100,
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: theme.colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    ...Platform.select({
      ios: {
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
      },
      android: { elevation: 8 },
    }),
  },
  fabIcon: {
    fontSize: 36,
    color: '#FFFFFF',
    fontWeight: '300',
    marginTop: -4,
  },
});
