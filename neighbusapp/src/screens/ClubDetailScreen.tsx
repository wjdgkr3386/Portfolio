// src/screens/ClubDetailScreen.tsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Image,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
  Button,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Calendar, LocaleConfig } from 'react-native-calendars';
import type { RootStackScreenProps } from '../../App';
import theme from '../styles/theme';
import { useUser } from '../context/UserContext';
import { BASE_URL } from '../config';

LocaleConfig.locales['ko'] = {
  monthNames: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
  monthNamesShort: ['1.', '2.', '3.', '4.', '5.', '6.', '7.', '8.', '9.', '10.', '11.', '12.'],
  dayNames: ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일'],
  dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
  today: "오늘"
};
LocaleConfig.defaultLocale = 'ko';

type Props = RootStackScreenProps<'ClubDetail'>;

type ClubDetailData = {
  id: string;
  name: string;
  category: string;
  location: string;
  imageUrl: string;
  description: string;
};

const ClubDetailScreen: React.FC<Props> = ({ route, navigation }) => {
  const { clubId } = route.params;
  const { token } = useUser();

  const [clubDetail, setClubDetail] = useState<ClubDetailData | null>(null);
  const [isMember, setIsMember] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedDate, setSelectedDate] = useState('');
  const [meetings, setMeetings] = useState<Record<string, { id: string; summary: string }[]>>({});

  const markedDates = Object.keys(meetings).reduce((acc, date) => {
    acc[date] = { 
      selected: true, 
      selectedColor: '#F5EDE4', 
      selectedTextColor: '#5C4A3A',
      marked: true,
      dotColor: theme.colors.primary,
    };
    return acc;
  }, {} as Record<string, any>);

  if (selectedDate) {
    markedDates[selectedDate] = {
      ...markedDates[selectedDate],
      selected: true,
      selectedColor: theme.colors.primary,
      selectedTextColor: '#FFFFFF',
    };
  }

  const fetchClubDetail = useCallback(async () => {
    setLoading(true);
    setError(null);

    if (!token) {
      setError('로그인이 필요한 서비스입니다.');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${BASE_URL}/api/mobile/club/${clubId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('동아리 정보를 불러오는데 실패했습니다.');
      }

      const data = await response.json();

      if (data.success && data.club) {
        const backendClub = data.club;
        setClubDetail({
          id: backendClub.id?.toString() || '',
          name: backendClub.clubName || '이름 없음',
          category: backendClub.categoryName || '미분류', // Use categoryName directly
          location: backendClub.cityName || '지역 미정',
          imageUrl: backendClub.clubImg || 'https://picsum.photos/seed/picsum/400/200',
          description: backendClub.clubInfo || '소개 없음',
        });

        if (data.recruitments && Array.isArray(data.recruitments)) {
          const newMeetings: Record<string, { id: string; summary: string }[]> = {};
          data.recruitments.forEach((r: any) => {
            if (r.meetingDate) {
              const datePart = r.meetingDate.includes(' ') ? r.meetingDate.split(' ')[0] : r.meetingDate;
              if (!newMeetings[datePart]) {
                newMeetings[datePart] = [];
              }
              newMeetings[datePart].push({
                id: r.id.toString(),
                summary: r.title || '제목 없음',
              });
            }
          });
          setMeetings(newMeetings);
        }

        setIsMember(data.isMember || false);
      } else {
        setError(data.message || '동아리 정보를 찾을 수 없습니다.');
      }
    } catch (err: any) {
      setError(err.message || '네트워크 오류가 발생했습니다.');
      console.error('Error fetching club detail:', err);
    } finally {
      setLoading(false);
    }
  }, [clubId, token]);

  useEffect(() => {
    fetchClubDetail();
  }, [fetchClubDetail]);

  const handleJoinClub = async () => {
    if (!token) {
      Alert.alert('오류', '로그인이 필요합니다.');
      return;
    }

    try {
      const response = await fetch(
        `${BASE_URL}/api/mobile/club/join/${clubId}`,
        {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }
      );

      const data = await response.json();

      if (!response.ok) {
        Alert.alert('실패', data.message || '가입에 실패했습니다.');
        return;
      }

      Alert.alert('성공', data.message || '동아리 가입이 완료되었습니다.');
      setIsMember(true); // ✅ 서버 성공 후에만 상태 변경

    } catch (error) {
      console.error(error);
      Alert.alert('오류', '서버와 통신 중 문제가 발생했습니다.');
    }
  };

  
  if (loading) {
    return (
      <SafeAreaView style={[styles.safeArea, styles.centered]}>
        <ActivityIndicator size="large" color={theme.colors.primary} />
        <Text style={styles.loadingText}>동아리 정보를 불러오는 중...</Text>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView style={[styles.safeArea, styles.centered]}>
        <Text style={styles.errorText}>{error}</Text>
        <Button title="다시 시도" onPress={fetchClubDetail} color={theme.colors.primary} />
      </SafeAreaView>
    );
  }

  if (!clubDetail) {
    return (
      <SafeAreaView style={[styles.safeArea, styles.centered]}>
        <Text style={styles.errorText}>동아리 정보를 찾을 수 없습니다.</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView style={styles.container}>
        <View style={styles.header}>
          <TouchableOpacity
            style={styles.backButton}
            onPress={() => navigation.goBack()}
          >
            <Text style={styles.backButtonText}>←</Text>
          </TouchableOpacity>
        </View>

        <Image source={{ uri: clubDetail.imageUrl }} style={styles.headerImage} />

        <View style={styles.contentContainer}>
          <Text style={styles.clubName}>{clubDetail.name}</Text>
          <View style={styles.infoRow}>
            <Text style={styles.infoChip}>🏷️ {clubDetail.category}</Text>
            <Text style={styles.infoChip}>📍 {clubDetail.location}</Text>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionTitle}>동아리 소개</Text>
            <Text style={styles.description}>{clubDetail.description}</Text>
          </View>

          {/* 모임 일정 섹션 (Placeholder data) */}
          <View style={styles.section}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>모임 일정</Text>
              <TouchableOpacity
                style={styles.createMeetingButton}
                onPress={() => navigation.navigate('CreateMeeting', { clubId })}
              >
                <Text style={styles.createMeetingButtonText}>+ 모임 생성</Text>
              </TouchableOpacity>
            </View>
            <Calendar
              current={'2025-12-15'}
              onDayPress={(day) => {
                setSelectedDate(day.dateString);
              }}
              markedDates={markedDates}
              theme={{
                arrowColor: theme.colors.primary,
                todayTextColor: theme.colors.primary,
                selectedDayBackgroundColor: '#D8D0C8',
                dotColor: theme.colors.primary,
              }}
            />
            {selectedDate && meetings[selectedDate] && meetings[selectedDate].map((meeting, index) => (
              <TouchableOpacity
                key={`${selectedDate}-${index}`}
                style={styles.meetingInfo}
                activeOpacity={0.7}
                onPress={() => navigation.navigate('MeetingDetail', {
                  meetingId: meeting.id,
                  date: selectedDate,
                })}
              >
                <View>
                  <Text style={styles.meetingDate}>{selectedDate}</Text>
                  <Text style={styles.meetingSummary}>{meeting.summary}</Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      </ScrollView>
      <View style={styles.footer}>
        <TouchableOpacity
          style={[styles.joinButton, isMember && styles.disabledButton]}
          activeOpacity={0.8}
          onPress={handleJoinClub}
          disabled={isMember}
        >
          <Text style={styles.joinButtonText}>{isMember ? '가입된 동아리' : '동아리 가입하기'}</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  centered: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: theme.colors.textSecondary,
  },
  errorText: {
    fontSize: 16,
    color: theme.colors.danger,
    textAlign: 'center',
    marginBottom: 20,
  },
  container: {
    flex: 1,
  },
  header: {
    position: 'absolute',
    top: 10,
    left: 10,
    zIndex: 1,
  },
  backButton: {
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  backButtonText: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: 'bold',
  },
  headerImage: {
    width: '100%',
    height: 250,
  },
  contentContainer: {
    padding: 20,
  },
  clubName: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#333333',
    marginBottom: 12,
  },
  infoRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 24,
  },
  infoChip: {
    backgroundColor: '#F5EDE4',
    color: '#9B7E5C',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
    fontSize: 13,
    fontWeight: '600',
    overflow: 'hidden', // for borderRadius to work on iOS
  },
  section: {
    marginBottom: 24,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: '#EEEEEE',
    paddingBottom: 8,
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#444444',
  },
  createMeetingButton: {
    backgroundColor: '#F5EDE4',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
  },
  createMeetingButtonText: {
    color: '#9B7E5C',
    fontSize: 14,
    fontWeight: '600',
  },
  description: {
    fontSize: 16,
    lineHeight: 24,
    color: '#555555',
  },
  meetingInfo: {
    marginTop: 16,
    padding: 16,
    backgroundColor: '#F5EDE4',
    borderRadius: 8,
  },
  meetingDate: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#5C4A3A',
  },
  meetingSummary: {
    fontSize: 14,
    color: '#8B7355',
    marginTop: 4,
  },
  footer: {
    padding: 20,
    borderTopWidth: 1,
    borderTopColor: '#EEEEEE',
    backgroundColor: '#FFFFFF',
  },
  joinButton: {
    backgroundColor: '#9B7E5C',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#9B7E5C',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.4,
    shadowRadius: 4,
    elevation: 5,
  },
  disabledButton: {
    backgroundColor: '#D8D0C8',
    shadowOpacity: 0,
    elevation: 0,
  },
  joinButtonText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
});

export default ClubDetailScreen;
