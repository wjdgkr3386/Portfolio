// src/screens/MeetingDetailScreen.tsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import MapView, { PROVIDER_GOOGLE, Marker } from 'react-native-maps';
import type { RootStackScreenProps } from '../../App';
import { BASE_URL } from '../config';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'MeetingDetail'>;

type Recruitment = {
  id: number;
  clubId: number;
  title: string;
  content: string;
  writer: number;
  address: string;
  maxUser: number;
  created_at: string;
  meetingDate: string;
  nickname: string;
  status: string;
  latitude: number | null;
  longitude: number | null;
};

type MeetingDetailData = {
  status: number;
  message: string;
  recruitment: Recruitment | null;
  currentUserCount: number;
  chatRoomExists: boolean;
  isJoined: boolean;
};

const MeetingDetailScreen: React.FC<Props> = ({ route, navigation }) => {
  const { meetingId } = route.params;
  const { token } = useUser();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<MeetingDetailData | null>(null);

  const fetchMeetingDetail = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/recruitment/${meetingId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      const result = await response.json();
      if (result.status === 1) {
        setData(result);
      } else {
        Alert.alert('오류', result.message || '정보를 불러올 수 없습니다.');
        navigation.goBack();
      }
    } catch (error) {
      console.error('Fetch meeting detail error:', error);
      Alert.alert('오류', '네트워크 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [meetingId, token, navigation]);

  useEffect(() => {
    fetchMeetingDetail();
  }, [fetchMeetingDetail]);

  const handleJoin = async () => {
    if (!token) {
      Alert.alert('알림', '로그인이 필요합니다.');
      return;
    }

    try {
      const response = await fetch(`${BASE_URL}/api/mobile/recruitment/join`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          recruitmentId: String(meetingId),
        }),
      });

      const result = await response.json();

      if (result.status === 1) {
        Alert.alert('성공', result.message || '모임에 성공적으로 가입했습니다.', [
          { text: '확인', onPress: fetchMeetingDetail },
        ]);
      } else {
        Alert.alert('실패', result.message || '모임 가입에 실패했습니다.');
      }
    } catch (error) {
      console.error('Join meeting error:', error);
      Alert.alert('오류', '서버 통신 중 오류가 발생했습니다.');
    }
  };

  const handleOpenChat = () => {
    if (data?.chatRoomExists) {
      navigation.navigate('Chat', { roomId: String(meetingId) } as any);
    }
  };

  if (loading) {
    return (
      <SafeAreaView style={[styles.safeArea, styles.centered]}>
        <ActivityIndicator size="large" color="#9B7E5C" />
      </SafeAreaView>
    );
  }

  if (!data || !data.recruitment) return null;

  const { recruitment, currentUserCount, isJoined, chatRoomExists } = data;
  
  // 날짜와 시간 분리 (예: "2025-12-23 14:00:00" -> "2025-12-23", "14:00")
  const dateParts = recruitment.meetingDate.split(' ');
  const displayDate = dateParts[0];
  const displayTime = dateParts[1] ? dateParts[1].substring(0, 5) : '';

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Text style={styles.backButtonText}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>모임 상세</Text>
        {isJoined && chatRoomExists ? (
          <TouchableOpacity onPress={handleOpenChat} style={styles.chatButton}>
            <Text style={styles.chatButtonText}>채팅</Text>
          </TouchableOpacity>
        ) : (
          <View style={{ width: 40 }} />
        )}
      </View>

      <ScrollView style={styles.container}>
        <View style={styles.contentContainer}>
          <View style={styles.dateBadge}>
            <Text style={styles.dateBadgeText}>{new Date(displayDate).getDate()}</Text>
          </View>
          <Text style={styles.meetingSummary}>{recruitment.title}</Text>
          
          <View style={styles.infoGrid}>
            <View style={styles.infoBox}>
              <Text style={styles.infoIcon}>🗓️</Text>
              <Text style={styles.infoLabel}>날짜</Text>
              <Text style={styles.infoValue}>{displayDate}</Text>
            </View>
            <View style={styles.infoBox}>
              <Text style={styles.infoIcon}>⏰</Text>
              <Text style={styles.infoLabel}>시간</Text>
              <Text style={styles.infoValue}>{displayTime}</Text>
            </View>
            <View style={styles.infoBox}>
              <Text style={styles.infoIcon}>📍</Text>
              <Text style={styles.infoLabel}>장소</Text>
              <Text style={styles.infoValue}>{recruitment.address}</Text>
            </View>
          </View>

          {recruitment.latitude && recruitment.longitude && (
            <View style={styles.mapWrapper}>
              <MapView
                provider={PROVIDER_GOOGLE}
                style={styles.map}
                initialRegion={{
                  latitude: recruitment.latitude,
                  longitude: recruitment.longitude,
                  latitudeDelta: 0.005,
                  longitudeDelta: 0.005,
                }}
                scrollEnabled={false} // 상세 화면에서는 지도가 고정된 것이 조작하기 편함
                zoomEnabled={false}
                pitchEnabled={false}
                rotateEnabled={false}
              >
                <Marker
                  coordinate={{
                    latitude: recruitment.latitude,
                    longitude: recruitment.longitude,
                  }}
                  title={recruitment.title}
                  description={recruitment.address}
                />
              </MapView>
            </View>
          )}

          <View style={styles.section}>
            <Text style={styles.sectionTitle}>모임 설명</Text>
            <Text style={styles.description}>{recruitment.content}</Text>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionTitle}>참여 현황</Text>
            <View style={styles.statusRow}>
              <Text style={styles.statusLabel}>현재 인원</Text>
              <Text style={styles.statusValue}>{currentUserCount} / {recruitment.maxUser}명</Text>
            </View>
            <View style={styles.progressBar}>
              <View 
                style={[
                  styles.progressFill, 
                  { width: `${Math.min((currentUserCount / recruitment.maxUser) * 100, 100)}%` }
                ]} 
              />
            </View>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionTitle}>주최자</Text>
            <View style={styles.hostInfo}>
              <View style={styles.hostAvatar}>
                <Text style={styles.hostAvatarText}>{recruitment.nickname[0]}</Text>
              </View>
              <Text style={styles.hostName}>{recruitment.nickname}</Text>
            </View>
          </View>
        </View>
      </ScrollView>

      <View style={styles.footer}>
        <TouchableOpacity 
          style={[styles.rsvpButton, isJoined && styles.joinedButton]} 
          activeOpacity={0.8}
          onPress={isJoined ? undefined : handleJoin}
          disabled={isJoined}
        >
          <Text style={styles.rsvpButtonText}>
            {isJoined ? '참가 중인 모임' : '참석하기'}
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F8F7F5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5E5',
    backgroundColor: '#FFFFFF',
  },
  backButton: {
    width: 40,
  },
  backButtonText: {
    fontSize: 24,
    color: '#5C4A3A',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#5C4A3A',
  },
  chatButton: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    backgroundColor: '#F5EDE4',
    borderRadius: 8,
  },
  chatButtonText: {
    color: '#9B7E5C',
    fontWeight: 'bold',
    fontSize: 14,
  },
  container: {
    flex: 1,
  },
  contentContainer: {
    padding: 24,
  },
  centered: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  dateBadge: {
    width: 60,
    height: 60,
    borderRadius: 12,
    backgroundColor: '#9B7E5C',
    justifyContent: 'center',
    alignItems: 'center',
    alignSelf: 'center',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.15,
    shadowRadius: 4,
    elevation: 3,
  },
  dateBadgeText: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  meetingSummary: {
    fontSize: 26,
    fontWeight: 'bold',
    color: '#333',
    textAlign: 'center',
    marginBottom: 24,
  },
  infoGrid: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 2,
  },
  infoBox: {
    alignItems: 'center',
    flex: 1,
  },
  infoIcon: {
    fontSize: 24,
    marginBottom: 8,
  },
  infoLabel: {
    fontSize: 13,
    color: '#8B7355',
    marginBottom: 4,
  },
  infoValue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#5C4A3A',
  },
  mapWrapper: {
    height: 200,
    borderRadius: 16,
    overflow: 'hidden',
    marginBottom: 24,
    borderWidth: 1,
    borderColor: '#E5E5E5',
  },
  map: {
    flex: 1,
  },
  section: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#444',
    marginBottom: 12,
  },
  description: {
    fontSize: 15,
    lineHeight: 22,
    color: '#555',
  },
  statusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  statusLabel: {
    fontSize: 14,
    color: '#666',
  },
  statusValue: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
  },
  progressBar: {
    height: 10,
    backgroundColor: '#F0F0F0',
    borderRadius: 5,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#9B7E5C',
  },
  hostInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  hostAvatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#F5EDE4',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  hostAvatarText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#9B7E5C',
  },
  hostName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  footer: {
    padding: 20,
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#EEEEEE',
  },
  rsvpButton: {
    backgroundColor: '#9B7E5C',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  joinedButton: {
    backgroundColor: '#D8D0C8',
  },
  rsvpButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export default MeetingDetailScreen;