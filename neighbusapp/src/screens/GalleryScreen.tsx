import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  PanResponder,
  ImageBackground,
  FlatList,
  Platform,
  ActivityIndicator,
  Button,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Picker } from '@react-native-picker/picker';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import type { RootStackScreenProps } from '../../App';
import SideMenu from '../components/SideMenu';
import BottomNavBar from '../components/BottomNavBar';
import theme from '../styles/theme';
import { BASE_URL } from '../config';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'Gallery'>;

type Post = {
  id: string;
  title: string;
  author: string;
  imageUrl: string;
  height: number;
};

type Club = {
  id: number;
  clubName: string;
};

const GalleryScreen: React.FC<Props> = ({ navigation }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showSideMenu, setShowSideMenu] = useState(false);
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [myClubs, setMyClubs] = useState<Club[]>([]);
  const [selectedClubId, setSelectedClubId] = useState<number>(0);
  const { token } = useUser();

  const fetchGalleryData = useCallback(() => {
    setError(null);
    setLoading(true);

    if (!token) {
      setError('로그인이 필요한 서비스입니다.');
      setLoading(false);
      return;
    }

    const keywordParam = searchQuery.trim() ? `&keyword=${encodeURIComponent(searchQuery.trim())}` : '';
    const clubParam = `&clubId=${selectedClubId}`;
    const url = `${BASE_URL}/api/mobile/gallery/getGallery?${keywordParam}${clubParam}`;

    fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then(response => {
        if (!response.ok) {
          throw new Error('데이터를 불러오는데 실패했습니다. 다시 시도해주세요.');
        }
        return response.text();
      })
      .then(text => {
        try {
          const data = JSON.parse(text);
          if (data.status === 'success' && data.galleryMapList) {
            const fetchedPosts: Post[] = data.galleryMapList.map((item: any) => ({
              id: item.ID.toString(),
              title: (item.TITLE || '').replace(/&nbsp;/g, ' '),
              author: item.WRITER || 'Unknown',
              imageUrl: (item.IMAGES && item.IMAGES.length > 0 && item.IMAGES[0].IMG) || `https://images.unsplash.com/photo-1528493366314-e264e78b4BFd?q=80&w=800`,
              height: Math.floor(Math.random() * 100) + 250,
            }));
            setPosts(fetchedPosts);
            if (Array.isArray(data.myClubList)) {
              setMyClubs(data.myClubList);
            }
          } else {
            throw new Error('갤러리 데이터를 가져오는데 실패했습니다.');
          }
        } catch (jsonError) {
          throw new Error('서버 응답을 처리하는 중 오류가 발생했습니다 (JSON Parse Error).');
        }
      })
      .catch(err => {
        setError(err.message || '알 수 없는 오류가 발생했습니다.');
        console.error('Error fetching gallery data:', err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [token, searchQuery, selectedClubId]);

  useEffect(() => {
    fetchGalleryData();
  }, [fetchGalleryData]);

  const panResponder = React.useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => false,
      onMoveShouldSetPanResponder: (evt, gestureState) => Math.abs(gestureState.dx) > 10,
      onPanResponderRelease: (evt, gestureState) => {
        if (gestureState.dx > 30) setShowSideMenu(true);
      },
    })
  ).current;

  const handleWritePost = () => navigation.navigate('GalleryWrite');
  const handlePostClick = (post: Post) => navigation.navigate('GalleryDetail', { postId: post.id });

  const renderGalleryItem = ({ item }: { item: Post }) => (
    <TouchableOpacity
      style={[styles.galleryItem, { height: item.height }]}
      onPress={() => handlePostClick(item)}
      activeOpacity={0.9}
    >
      <ImageBackground source={{ uri: item.imageUrl }} style={styles.imageBackground}>
        <View style={styles.textOverlay}>
          <Text style={styles.galleryItemTitle} numberOfLines={2}>{item.title}</Text>
          <View style={styles.authorContainer}>
            <Icon name="account" size={12} color="rgba(255, 255, 255, 0.9)" style={styles.authorIcon} />
            <Text style={styles.galleryItemAuthor}>{item.author}</Text>
          </View>
        </View>
      </ImageBackground>
    </TouchableOpacity>
  );

  const renderContent = () => {
    if (loading) {
      return <ActivityIndicator size="large" color={theme.colors.primary} style={styles.messageContainer} />;
    }
    if (error) {
      return (
        <View style={styles.messageContainer}>
          <Text style={styles.messageText}>{error}</Text>
          {token && <Button title="다시 시도" onPress={fetchGalleryData} color={theme.colors.primary} />}
        </View>
      );
    }
    if (posts.length === 0) {
      return (
        <View style={styles.messageContainer}>
          <Text style={styles.messageText}>게시물이 없습니다.</Text>
        </View>
      );
    }
    return (
      <FlatList
        data={posts}
        renderItem={renderGalleryItem}
        keyExtractor={item => item.id}
        numColumns={2}
        contentContainerStyle={styles.galleryList}
        showsVerticalScrollIndicator={false}
      />
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>갤러리</Text>
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

      {/* Controls Section */}
      <View style={styles.controlsContainer}>
        <View style={styles.searchInputWrapper}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={styles.searchInput}
            placeholder="제목, 작성자 검색..."
            placeholderTextColor={theme.colors.textLight}
            value={searchQuery}
            onChangeText={setSearchQuery}
            onSubmitEditing={fetchGalleryData}
            returnKeyType="search"
          />
        </View>
      </View>

      {renderContent()}

      <TouchableOpacity
        style={styles.fab}
        onPress={handleWritePost}
        activeOpacity={0.8}
      >
        <Text style={styles.fabIcon}>+</Text>
      </TouchableOpacity>

      <BottomNavBar currentScreen="Gallery" />

<SideMenu visible={showSideMenu} onClose={() => setShowSideMenu(false)} navigation={navigation} />
    </SafeAreaView>
  );
};

export default GalleryScreen;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFF8F0',
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
    height: 55,
    justifyContent: 'center',
    overflow: 'hidden',
  },
  picker: {
    height: Platform.OS === 'ios' ? 150 : 55,
    width: '100%',
    color: theme.colors.textPrimary,
  },
  controlsContainer: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingVertical: 12,
    alignItems: 'center',
    gap: 12,
    backgroundColor: '#FFF8F0',
  },
  searchInputWrapper: {
    flex: 1,
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
  galleryList: {
    padding: 8,
    paddingBottom: 100,
  },
  galleryItem: {
    flex: 1,
    margin: 8,
    borderRadius: 16,
    overflow: 'hidden',
    backgroundColor: theme.colors.white,
    ...Platform.select({
      ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.15, shadowRadius: 8 },
      android: { elevation: 5 },
    }),
  },
  imageBackground: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  textOverlay: {
    padding: 14,
    backgroundColor: 'rgba(0,0,0,0.45)',
  },
  galleryItemTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: theme.colors.white,
    textShadowColor: 'rgba(0, 0, 0, 0.5)',
    textShadowOffset: { width: 1, height: 1 },
    textShadowRadius: 2,
    marginBottom: 4,
  },
  authorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  authorIcon: {
    marginRight: 4,
  },
  galleryItemAuthor: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.9)',
    fontWeight: '500',
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
      ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 4 },
      android: { elevation: 8 },
    }),
  },
  fabIcon: {
    fontSize: 30,
    color: theme.colors.white,
    lineHeight: 30,
  },
  messageContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  messageText: {
    fontSize: 16,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    marginBottom: 10,
  },
});