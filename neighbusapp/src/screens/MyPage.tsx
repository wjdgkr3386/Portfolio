import React, { useState, useEffect, useRef, useCallback } from 'react';
import type { FC } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Image,
  PanResponder,
  Animated,
  ActivityIndicator,
  Alert,
  TouchableOpacity,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { RootStackScreenProps } from '../../App';
import { useUser } from '../context/UserContext';
import SideMenu from '../components/SideMenu';
import BottomNavBar from '../components/BottomNavBar';
import { BASE_URL } from '../config';

type Props = RootStackScreenProps<'MyPage'>;

type MyInfo = {
  id: number;
  name: string;
  username: string;
  nickname: string;
  province: string;
  city: string;
  image: string;
  grade: string;
  userUuid: string;
};

type MyClub = {
  id: number;
  clubName: string;
  clubImg: string;
  role?: string; // Assume role if available, or default to '멤버'
};

type MyMeeting = {
  id: number;
  title: string;
  clubName: string;
  meetingDate: string;
};

type MyPageData = {
  myInfo: MyInfo | null;
  myPosts: any[];
  myComments: any[];
  myClubs: MyClub[];
  recruitmentList: MyMeeting[];
};

const MyPage: FC<Props> = ({ navigation }) => {
  const { token } = useUser();
  const [showSideMenu, setShowSideMenu] = useState(false);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'clubs' | 'meetings' | 'posts'>('clubs');
  const [data, setData] = useState<MyPageData>({
    myInfo: null,
    myPosts: [],
    myComments: [],
    myClubs: [],
    recruitmentList: [],
  });

  const hintAnimation = useRef(new Animated.Value(-50)).current;

  const fetchMyPageData = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/mypage/info`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      const result = await response.json();
      if (response.ok) {
        setData({
          myInfo: result.myInfo || null,
          myPosts: result.myPosts || [],
          myComments: result.myComments || [],
          myClubs: result.myClubs || [],
          recruitmentList: result.recruitmentList || [],
        });
      } else {
        console.error('Failed to fetch mypage info:', result.error);
      }
    } catch (error) {
      console.error('Error fetching mypage info:', error);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchMyPageData();
  }, [fetchMyPageData]);

  // Side menu hint animation
  useEffect(() => {
    const animation = Animated.sequence([
      Animated.timing(hintAnimation, {
        toValue: 0,
        duration: 500,
        delay: 1000,
        useNativeDriver: true,
      }),
      Animated.timing(hintAnimation, {
        toValue: -50,
        duration: 500,
        delay: 1500,
        useNativeDriver: true,
      }),
    ]);
    animation.start();
  }, [hintAnimation]);

  // 스와이프 제스처 감지
  const panResponder = React.useRef(
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

  if (loading) {
    return (
      <SafeAreaView style={[styles.safeArea, styles.centered]}>
        <ActivityIndicator size="large" color="#9B7E5C" />
      </SafeAreaView>
    );
  }

  const { myInfo, myClubs, recruitmentList, myPosts } = data;

  return (
    <SafeAreaView style={styles.safeArea}>
      <Animated.View style={[styles.sideMenuHint, { transform: [{ translateX: hintAnimation }] }]}>
        <Text style={styles.hintArrow}>›</Text>
      </Animated.View>

      <View style={styles.header}>
        <Text style={styles.headerTitle}>마이페이지</Text>
      </View>

      <ScrollView 
        style={styles.container} 
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.contentContainer}
        {...panResponder.panHandlers}
      >
        {/* 프로필 섹션 */}
        <View style={styles.profileSection}>
          <View style={styles.profileImageContainer}>
            <Image 
              source={{ uri: myInfo?.image || 'https://via.placeholder.com/150' }} 
              style={styles.profileImage} 
            />
          </View>

          <Text style={styles.nickname}>{myInfo?.nickname || myInfo?.name || '사용자'} 님</Text>
          <View style={styles.locationContainer}>
            <Text style={styles.locationIcon}>📍</Text>
            <Text style={styles.location}>{myInfo?.province} {myInfo?.city}</Text>
          </View>
        </View>

        {/* 통계 섹션 */}
        <View style={styles.statsContainer}>
          <View style={styles.statItem}>
            <Text style={styles.statLabel}>가입 동아리</Text>
            <Text style={styles.statValue}>{myClubs.length}</Text>
          </View>
          <View style={styles.statDivider} />
          <View style={styles.statItem}>
            <Text style={styles.statLabel}>참여 예정 모임</Text>
            <Text style={styles.statValue}>{recruitmentList.length}</Text>
          </View>
          <View style={styles.statDivider} />
          <View style={styles.statItem}>
            <Text style={styles.statLabel}>작성 글</Text>
            <Text style={styles.statValue}>{myPosts.length}</Text>
          </View>
        </View>

        {/* 탭 버튼 섹션 */}
        <View style={styles.tabBar}>
          <TouchableOpacity
            style={[styles.tabButton, activeTab === 'clubs' && styles.activeTabButton]}
            onPress={() => setActiveTab('clubs')}
          >
            <Text style={[styles.tabButtonText, activeTab === 'clubs' && styles.activeTabButtonText]}>내 동아리</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tabButton, activeTab === 'meetings' && styles.activeTabButton]}
            onPress={() => setActiveTab('meetings')}
          >
            <Text style={[styles.tabButtonText, activeTab === 'meetings' && styles.activeTabButtonText]}>내 모임</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tabButton, activeTab === 'posts' && styles.activeTabButton]}
            onPress={() => setActiveTab('posts')}
          >
            <Text style={[styles.tabButtonText, activeTab === 'posts' && styles.activeTabButtonText]}>작성 글</Text>
          </TouchableOpacity>
        </View>

        {/* 탭 콘텐츠 섹션 */}
        <View style={styles.tabContent}>
          {activeTab === 'clubs' && (
            <View style={styles.clubGrid}>
              {myClubs.length > 0 ? (
                myClubs.map((club) => (
                  <TouchableOpacity
                    key={club.id}
                    style={styles.gridClubCard}
                    onPress={() => navigation.navigate('ClubDetail', { clubId: club.id.toString() })}
                  >
                    <Image source={{ uri: club.clubImg || 'https://via.placeholder.com/100x80' }} style={styles.clubImage} />
                    <Text style={styles.clubName} numberOfLines={1}>{club.clubName}</Text>
                    <Text style={styles.clubStatus}>({club.role || '멤버'})</Text>
                  </TouchableOpacity>
                ))
              ) : (
                <Text style={styles.emptyText}>가입한 동아리가 없습니다.</Text>
              )}
            </View>
          )}

          {activeTab === 'meetings' && (
            <View style={styles.section}>
              {recruitmentList.length > 0 ? (
                recruitmentList.map(meeting => (
                  <TouchableOpacity
                    key={meeting.id}
                    style={styles.myActivityCard}
                    activeOpacity={0.7}
                    onPress={() => navigation.navigate('MeetingDetail', { meetingId: meeting.id.toString(), date: meeting.meetingDate.split(' ')[0] })}
                  >
                    <View style={styles.myActivityInfo}>
                      <Text style={styles.myActivitySubText}>{meeting.clubName}</Text>
                      <Text style={styles.myActivityTitle} numberOfLines={1}>{meeting.title}</Text>
                    </View>
                    <Text style={styles.myActivityDate}>{meeting.meetingDate.split(' ')[0]}</Text>
                  </TouchableOpacity>
                ))
              ) : (
                <Text style={[styles.emptyText, { paddingHorizontal: 20 }]}>참여 예정인 모임이 없습니다.</Text>
              )}
            </View>
          )}

          {activeTab === 'posts' && (
            <View style={styles.section}>
              {myPosts.length > 0 ? (
                myPosts.map((post, index) => (
                  <TouchableOpacity
                    key={`${post.postType}-${post.postId}-${index}`}
                    style={styles.myActivityCard}
                    activeOpacity={0.7}
                    onPress={() => {
                      if (post.postType === 'gallery') {
                        navigation.navigate('GalleryDetail', { postId: post.postId.toString() });
                      } else {
                        navigation.navigate('FreeBoardDetail', { postId: post.postId.toString() });
                      }
                    }}
                  >
                    <View style={styles.myActivityInfo}>
                      <Text style={styles.myActivitySubText}>
                        {post.postType === 'gallery' ? '갤러리' : '자유게시판'}
                      </Text>
                      <Text style={styles.myActivityTitle} numberOfLines={1}>{post.title}</Text>
                    </View>
                    <Text style={styles.myActivityDate}>{post.createdAt.split('T')[0]}</Text>
                  </TouchableOpacity>
                ))
              ) : (
                <Text style={[styles.emptyText, { paddingHorizontal: 20 }]}>작성한 게시글이 없습니다.</Text>
              )}
            </View>
          )}
        </View>

      </ScrollView>

      {/* 하단 네비게이션 */}
      <BottomNavBar currentScreen="MyPage" />

      {/* 사이드 메뉴 */}
      <SideMenu
        visible={showSideMenu}
        onClose={() => setShowSideMenu(false)}
        navigation={navigation}
      />
    </SafeAreaView>
  );
};

export default MyPage;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F5EDE4',
  },
  sideMenuHint: {
    position: 'absolute',
    left: 0,
    top: '45%',
    width: 50,
    height: 60,
    backgroundColor: 'rgba(155, 126, 92, 0.7)',
    borderTopRightRadius: 30,
    borderBottomRightRadius: 30,
    justifyContent: 'center',
    paddingLeft: 5,
    zIndex: 10,
  },
  hintArrow: {
    fontSize: 30,
    color: 'white',
    fontWeight: '200',
  },
  container: {
    // flex: 1 is removed
  },
  contentContainer: {
    paddingBottom: 100, // Added padding for BottomNavBar
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5E5',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#5C4A3A',
  },
  profileSection: {
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    paddingVertical: 32,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5E5',
  },
  profileImageContainer: {
    marginBottom: 16,
  },
  profileImage: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: '#E8DDD0',
    borderWidth: 2,
    borderColor: '#D8D0C8',
  },
  nickname: {
    fontSize: 20,
    fontWeight: '700',
    color: '#5C4A3A',
    marginBottom: 8,
  },
  locationContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  locationIcon: {
    fontSize: 14,
    marginRight: 4,
  },
  location: {
    fontSize: 14,
    color: '#8B7355',
  },
  statsContainer: {
    backgroundColor: '#FFFFFF',
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: 20,
    marginBottom: 12,
  },
  tabBar: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5E5',
  },
  tabButton: {
    flex: 1,
    paddingVertical: 16,
    alignItems: 'center',
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  activeTabButton: {
    borderBottomColor: '#9B7E5C',
  },
  tabButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#8B7355',
  },
  activeTabButtonText: {
    color: '#5C4A3A',
    fontWeight: '700',
  },
  tabContent: {
    backgroundColor: '#FFFFFF',
    minHeight: 300,
  },
  statItem: {
    flex: 1,
    alignItems: 'center',
  },
  statLabel: {
    fontSize: 12,
    color: '#8B7355',
    marginBottom: 6,
  },
  statValue: {
    fontSize: 24,
    fontWeight: '700',
    color: '#5C4A3A',
  },
  statDivider: {
    width: 1,
    backgroundColor: '#E5E5E5',
  },
  section: {
    backgroundColor: '#FFFFFF',
    paddingVertical: 20,
    marginBottom: 12,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 16,
  },
  sectionIcon: {
    fontSize: 18,
    marginRight: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#5C4A3A',
  },
  clubGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 15,
    paddingVertical: 20,
  },
  gridClubCard: {
    width: '33.33%',
    alignItems: 'center',
    marginBottom: 20,
    paddingHorizontal: 5,
  },
  clubImage: {
    width: '100%',
    height: 70,
    backgroundColor: '#F5EDE4',
    borderRadius: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: '#D8D0C8',
  },
  clubName: {
    fontSize: 12,
    fontWeight: '600',
    color: '#5C4A3A',
    textAlign: 'center',
    marginBottom: 2,
  },
  clubStatus: {
    fontSize: 10,
    color: '#8B7355',
  },
    myActivityCard: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      paddingVertical: 14,
      paddingHorizontal: 20,
      marginHorizontal: 20,
      marginBottom: 10,
      backgroundColor: '#FAFAFA',
      borderRadius: 12,
      borderWidth: 1,
      borderColor: '#E5E5E5',
    },
    myActivityInfo: {
      flex: 1,
    },
    myActivitySubText: {
      fontSize: 12,
      color: '#8B7355',
      marginBottom: 4,
    },
    myActivityTitle: {
      fontSize: 15,
      fontWeight: '600',
      color: '#5C4A3A',
    },
    myActivityDate: {
      fontSize: 13,
      fontWeight: '500',
      color: '#A67C52',
    },
    centered: {
  
      justifyContent: 'center',
      alignItems: 'center',
    },
    emptyText: {
      fontSize: 14,
      color: '#8B7355',
      textAlign: 'center',
      marginTop: 10,
      width: '100%',
    },
  });
  