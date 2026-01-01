import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  FlatList,
  Image,
  ActivityIndicator,
  Platform,
  PanResponder,
  Button,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Picker } from '@react-native-picker/picker';
import type { RootStackScreenProps } from '../../App';
import { BASE_URL } from '../config';
import BottomNavBar from '../components/BottomNavBar';
import SideMenu from '../components/SideMenu';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'Home'>;

type Club = {
  id: string;
  clubName: string;
  provinceName: string;
  clubImg: string;
  clubDescription: string;
  memberCount: number;
};

type Category = {
  id: number;
  name: string;
};

const HomeScreen: React.FC<Props> = ({ navigation }) => {
  const [clubs, setClubs] = useState<Club[]>([]);
  const [categoryList, setCategoryList] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [showSideMenu, setShowSideMenu] = useState(false);
  const { token } = useUser();

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => false,
      onMoveShouldSetPanResponder: (evt, gestureState) => {
        return Math.abs(gestureState.dx) > 10;
      },
      onPanResponderRelease: (evt, gestureState) => {
        if (gestureState.dx > 30) {
          setShowSideMenu(true);
        }
      },
    })
  ).current;

  const fetchClubs = useCallback(() => {
    setLoading(true);
    setError(null);

    if (!token) {
      setError('로그인이 필요한 서비스입니다.');
      setLoading(false);
      return;
    }

    // Use selectedCategory and searchQuery in the API call
    const url = `${BASE_URL}/api/mobile/club/getClubs?category=${selectedCategory}&keyword=${searchQuery}`;

    fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
    })
    .then((res) => {
      if (!res.ok) {
        throw new Error('동아리 목록을 불러오는데 실패했습니다.');
      }
      return res.json();
    })
    .then((data) => {
      if (data.clubs) {
        const mappedClubs: Club[] = data.clubs.map((club: any) => ({
          id: club.id?.toString(), 
          clubName: club.clubName || '이름 없음',
          provinceName: club.provinceName || '지역 정보 없음',
          clubImg: club.clubImg || 'https://images.unsplash.com/photo-1511632765486-a01980e01a18?w=800',
          clubDescription: club.clubInfo || '소개 없음',
          memberCount: club.memberCount || 0,
        }));
        const uniqueClubs = mappedClubs.filter((club, index, self) =>
          index === self.findIndex((c) => c.id === club.id)
        );
        setClubs(uniqueClubs);
      } else {
        setClubs([]);
      }

      if (data.categoryList && Array.isArray(data.categoryList)) {
        setCategoryList(data.categoryList);
      }
    })
    .catch(err => {
      setError(err.message || '알 수 없는 오류가 발생했습니다.');
      console.error(err);
    })
    .finally(() => setLoading(false));
  }, [token, searchQuery, selectedCategory]);

  useEffect(() => {
    fetchClubs();
  }, [fetchClubs]);

  const renderItem = ({ item }: { item: Club }) => (
    <TouchableOpacity
      style={styles.clubCard}
      onPress={() => navigation.navigate('ClubDetail', { clubId: item.id })}
      activeOpacity={0.8}
    >
      <Image
        source={{ uri: item.clubImg || 'https://images.unsplash.com/photo-1511632765486-a01980e01a18?w=800' }}
        style={styles.cardImage}
      />
      <View style={styles.cardBody}>
        <Text style={styles.cardTitle} numberOfLines={1}>{item.clubName}</Text>
        <Text style={styles.cardDescription} numberOfLines={2}>{item.clubDescription}</Text>
        <View style={styles.cardFooter}>
          <View style={styles.metadataItem}>
            <Text style={styles.metadataIcon}>📍</Text>
            <Text style={styles.metadataText}>{item.provinceName}</Text>
          </View>
          <View style={styles.metadataItem}>
            <Text style={styles.metadataIcon}>👥</Text>
            <Text style={styles.metadataText}>{item.memberCount}명</Text>
          </View>
        </View>
      </View>
    </TouchableOpacity>
  );

  const renderContent = () => {
    if (loading) {
      return <ActivityIndicator size="large" color="#A67C52" style={styles.messageContainer} />;
    }
    if (error) {
      return (
        <View style={styles.messageContainer}>
          <Text style={styles.messageText}>{error}</Text>
          {token && <Button title="다시 시도" onPress={fetchClubs} color="#A67C52" />}
        </View>
      );
    }
    return (
      <FlatList
        data={clubs}
        renderItem={renderItem}
        keyExtractor={(item) => item.id.toString()}
        numColumns={2}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={
          <View style={styles.messageContainer}>
            <Text style={styles.messageText}>표시할 동아리가 없습니다.</Text>
          </View>
        }
      />
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.container} {...panResponder.panHandlers}>
        <View style={styles.header}>
          <Text style={styles.headerTitle}>동아리</Text>
          <View style={styles.headerPickerWrapper}>
            <Picker
              selectedValue={selectedCategory}
              onValueChange={(itemValue) => setSelectedCategory(itemValue)}
              style={styles.picker}
              dropdownIconColor="#A67C52"
            >
              <Picker.Item label="전체 카테고리" value={0} />
              {categoryList.map((cat) => (
                <Picker.Item key={cat.id} label={cat.name} value={cat.id} />
              ))}
            </Picker>
          </View>
        </View>

        <View style={styles.controlsContainer}>
          <View style={styles.searchInputWrapper}>
            <Text style={styles.searchIcon}>🔍</Text>
            <TextInput
              style={styles.searchInput}
              placeholder="관심있는 동아리를 검색해보세요..."
              placeholderTextColor="#8D6E63"
              value={searchQuery}
              onChangeText={setSearchQuery}
              onSubmitEditing={fetchClubs}
              returnKeyType="search"
            />
          </View>
        </View>

        {renderContent()}
      </View>

      <TouchableOpacity
        style={styles.fab}
        onPress={() => navigation.navigate('ClubCreate')}
        activeOpacity={0.8}
      >
        <Text style={styles.fabIcon}>+</Text>
      </TouchableOpacity>
      
      <BottomNavBar currentScreen="Home" />

      <SideMenu 
        visible={showSideMenu}
        onClose={() => setShowSideMenu(false)}
        navigation={navigation}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#FFF8F0' },
  container: { flex: 1 },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 8,
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: '800',
    color: '#5D4037',
  },
  headerPickerWrapper: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E8D7C3',
    width: 160,
    height: 55,
    justifyContent: 'center',
    overflow: 'hidden',
  },
  picker: {
    height: Platform.OS === 'ios' ? 150 : 55,
    width: '100%',
    color: '#5D4037',
  },
  controlsContainer: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#E8D7C3',
  },
  searchInputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    paddingHorizontal: 16,
    borderWidth: 1,
    borderColor: '#E8D7C3',
  },
  searchIcon: {
    fontSize: 18,
    color: '#A1887F',
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
    paddingVertical: 12,
    color: '#5D4037',
  },
  listContent: {
    paddingHorizontal: 8,
    paddingBottom: 100,
  },
  clubCard: {
    flex: 1,
    margin: 8,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    ...Platform.select({
      ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 8 },
      android: { elevation: 4 },
    }),
  },
  cardImage: {
    width: '100%',
    height: 110,
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
  },
  cardBody: {
    padding: 12,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#5D4037',
    marginBottom: 6,
  },
  cardDescription: {
    fontSize: 13,
    color: '#8D6E63',
    marginBottom: 10,
    lineHeight: 18,
  },
  cardFooter: {
    borderTopWidth: 1,
    borderTopColor: '#F5F5F5',
    paddingTop: 10,
  },
  metadataItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 5,
  },
  metadataIcon: {
    fontSize: 12,
    marginRight: 6,
  },
  metadataText: {
    fontSize: 12,
    color: '#A1887F',
  },
  messageContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 50,
  },
  messageText: {
    fontSize: 16,
    color: '#A1887F',
  },
  fab: {
    position: 'absolute',
    right: 25,
    bottom: 100,
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#A67C52',
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

export default HomeScreen;