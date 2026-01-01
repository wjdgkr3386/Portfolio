import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  Alert,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import DatePicker from 'react-native-date-picker';
import MapView, { PROVIDER_GOOGLE, Marker, MapPressEvent } from 'react-native-maps';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import Config from 'react-native-config';
import type { RootStackScreenProps } from '../../App';
import { BASE_URL } from '../config';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'CreateMeeting'>;

const CreateMeetingScreen: React.FC<Props> = ({ route, navigation }) => {
  const { clubId } = route.params;
  const { user, token } = useUser();
  const [summary, setSummary] = useState('');
  
  // 날짜와 시간을 하나의 Date 객체로 관리
  const [date, setDate] = useState(new Date());
  const [open, setOpen] = useState(false); // DatePicker 열림 상태
  
  const [address, setAddress] = useState(''); // 도로명 주소
  const [locationDetail, setLocationDetail] = useState(''); // 상세 주소
  const [memberLimit, setMemberLimit] = useState('');
  const [description, setDescription] = useState('');

  const [selectedLocation, setSelectedLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);

  const [region, setRegion] = useState({
    latitude: 37.5665,
    longitude: 126.978,
    latitudeDelta: 0.01,
    longitudeDelta: 0.01,
  });

  // 지도 클릭 핸들러 (역지오코딩 추가)
  const handleMapPress = async (e: MapPressEvent) => {
    const coordinate = e.nativeEvent.coordinate;
    setSelectedLocation(coordinate);
    console.log('Selected Coordinate:', coordinate);

    try {
      const apiKey = Config.GOOGLE_MAPS_API_KEY;
      if (!apiKey) {
        console.error('Google Maps API Key is missing in Config');
        return;
      }

      const response = await fetch(
        `https://maps.googleapis.com/maps/api/geocode/json?latlng=${coordinate.latitude},${coordinate.longitude}&key=${apiKey}&language=ko`
      );
      const data = await response.json();

      if (data.status === 'OK' && data.results.length > 0) {
        const formattedAddress = data.results[0].formatted_address;
        setAddress(formattedAddress);
        console.log('Fetched Address:', formattedAddress);
      } else {
        console.warn('Geocoding Status:', data.status);
        const errorDetail = data.error_message || '상세 사유 없음';
        
        // 구글에서 보내주는 구체적인 거절 사유를 팝업으로 표시
        Alert.alert(
          '주소 조회 실패',
          `사유: ${data.status}\n메시지: ${errorDetail}`
        );
      }
    } catch (error) {
      console.error('Reverse Geocoding Error:', error);
      Alert.alert('오류', '주소를 가져오는 중 네트워크 오류가 발생했습니다.');
    }
  };

  const handleCreateMeeting = async () => {
    if (!summary.trim()) {
      Alert.alert('입력 오류', '모임 이름을 입력해주세요.');
      return;
    }

    if (!description.trim()) {
      Alert.alert('입력 오류', '상세 설명을 입력해주세요.');
      return;
    }

    const now = new Date();
    if (date < now) {
      Alert.alert('입력 오류', '모임 시간은 현재 시간 이후로 설정해주세요.');
      return;
    }

    if (!memberLimit.trim()) {
      Alert.alert('입력 오류', '최대 인원을 입력해주세요.');
      return;
    }

    const memberCount = parseInt(memberLimit, 10);
    if (isNaN(memberCount) || memberCount <= 1) {
      Alert.alert('입력 오류', '최대 인원은 2명 이상이어야 합니다.');
      return;
    }

    if (!selectedLocation || !address) {
      Alert.alert('입력 오류', '지도에서 모임 장소를 선택해주세요.');
      return;
    }

    if (!locationDetail.trim()) {
      Alert.alert('입력 오류', '상세 장소 설명을 입력해주세요.');
      return;
    }

    try {
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      const seconds = '00';
      
      const meetingDateStr = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;

      // 도로명 주소와 상세 주소 결합
      const fullAddress = `${address} (${locationDetail.trim()})`;

      const payload = {
        clubId: parseInt(clubId, 10), 
        title: summary,
        content: description,
        address: fullAddress,
        maxUser: parseInt(memberLimit, 10),
        meetingDate: meetingDateStr,
        latitude: selectedLocation.latitude,
        longitude: selectedLocation.longitude,
        status: 'OPEN',
      };

      console.log('Sending payload:', payload);

      const response = await fetch(`${BASE_URL}/api/mobile/recruitment/new`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': token ? `Bearer ${token}` : '', 
        },
        body: JSON.stringify(payload),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        Alert.alert('모임 생성 완료', data.message || '모임이 성공적으로 생성되었습니다.');
        navigation.goBack();
      } else {
        Alert.alert('생성 실패', data.message || '모임 생성 중 오류가 발생했습니다.');
      }
    } catch (error) {
      console.error('Create meeting error:', error);
      Alert.alert('오류', '서버 통신 중 오류가 발생했습니다.');
    }
  };

  // 날짜/시간 포맷팅 함수 (화면 표시용)
  const formatDisplayDate = (d: Date) => {
    const year = d.getFullYear();
    const month = d.getMonth() + 1;
    const day = d.getDate();
    const hour = d.getHours();
    const minute = d.getMinutes().toString().padStart(2, '0');
    const ampm = hour >= 12 ? '오후' : '오전';
    const displayHour = hour % 12 || 12; // 0시는 12시로 표시

    return `${year}년 ${month}월 ${day}일 ${ampm} ${displayHour}:${minute}`;
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.headerButtonText}>취소</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>모임 생성</Text>
        <TouchableOpacity onPress={handleCreateMeeting}>
          <Text style={[styles.headerButtonText, styles.headerButtonConfirm]}>생성</Text>
        </TouchableOpacity>
      </View>

      <ScrollView style={styles.container} keyboardShouldPersistTaps="handled">
        <View style={styles.formGroup}>
          <Text style={styles.label}>모임 이름</Text>
          <TextInput
            style={styles.input}
            placeholder="모임의 주제를 알려주세요"
            value={summary}
            onChangeText={setSummary}
          />
        </View>

        <View style={styles.formGroup}>
          <Text style={styles.label}>상세 설명</Text>
          <TextInput
            style={[styles.input, styles.textarea]}
            placeholder="모임에 대한 자세한 내용을 공유해주세요"
            value={description}
            onChangeText={setDescription}
            multiline
          />
        </View>

        {/* 날짜 및 시간 선택 (통합) */}
        <View style={styles.formGroup}>
          <Text style={styles.label}>날짜 및 시간</Text>
          <TouchableOpacity
            style={styles.iconInput}
            onPress={() => setOpen(true)}
          >
            <Icon name="calendar-clock" size={20} color="#9B7E5C" style={styles.inputIcon} />
            <Text style={styles.inputText}>{formatDisplayDate(date)}</Text>
          </TouchableOpacity>
          <DatePicker
            modal
            open={open}
            date={date}
            onConfirm={(selectedDate) => {
              setOpen(false);
              setDate(selectedDate);
            }}
            onCancel={() => {
              setOpen(false);
            }}
            mode="datetime" // 날짜와 시간 동시 선택
            title="모임 날짜와 시간 선택"
            confirmText="확인"
            cancelText="취소"
          />
        </View>

        <View style={styles.formGroup}>
          <Text style={styles.label}>최대 인원</Text>
          <TextInput
            style={styles.input}
            placeholder="숫자만"
            value={memberLimit}
            onChangeText={setMemberLimit}
            keyboardType="number-pad"
          />
        </View>

        <View style={styles.formGroup}>
          <Text style={styles.label}>장소</Text>
          <View style={styles.mapWrapper}>
            <MapView
              provider={PROVIDER_GOOGLE}
              style={styles.map}
              initialRegion={region}
              onPress={handleMapPress}
            >
              {selectedLocation && (
                <Marker
                  coordinate={selectedLocation}
                  title="선택한 위치"
                />
              )}
            </MapView>
          </View>

          <View style={[styles.input, { marginTop: 12, backgroundColor: '#F0F0F0', minHeight: 50 }]}>
            <Text style={[styles.inputText, !address && { color: '#999' }]}>
              {address || '지도에서 위치를 선택하면 주소가 나타납니다'}
            </Text>
          </View>

          <TextInput
            style={[styles.input, { marginTop: 12 }]}
            placeholder="상세 장소 설명 (예: 2층 카페 안쪽)"
            value={locationDetail}
            onChangeText={setLocationDetail}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#E5E5E5',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  headerButtonText: {
    fontSize: 16,
    color: '#9B7E5C',
  },
  headerButtonConfirm: {
    fontWeight: 'bold',
  },
  container: {
    flex: 1,
    padding: 20,
  },
  formGroup: {
    marginBottom: 24,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#444',
    marginBottom: 8,
  },
  input: {
    backgroundColor: '#F8F7F5',
    borderWidth: 1,
    borderColor: '#E5E5E5',
    borderRadius: 10,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 16,
    justifyContent: 'center',
  },
  iconInput: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8F7F5',
    borderWidth: 1,
    borderColor: '#E5E5E5',
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 14,
  },
  inputIcon: {
    marginRight: 8,
  },
  inputText: {
    fontSize: 16,
    color: '#333',
  },
  row: {
    flexDirection: 'row',
    gap: 16,
  },
  flex: {
    flex: 1,
  },
  textarea: {
    height: 120,
    textAlignVertical: 'top',
  },
  mapWrapper: {
    height: 250,
    width: '100%',
    borderRadius: 10,
    marginTop: 12,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#E5E5E5',
  },
  map: {
    flex: 1,
  },
  locationInfo: {
    marginTop: 8,
    padding: 12,
    backgroundColor: '#F8F7F5',
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#E5E5E5',
  },
  locationText: {
    fontSize: 14,
    color: '#666',
  },
});

export default CreateMeetingScreen;