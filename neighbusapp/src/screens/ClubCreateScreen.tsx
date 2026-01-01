// src/screens/ClubCreateScreen.tsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  ScrollView,
  Alert,
  Platform,
  ActivityIndicator,
  Image,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Picker } from '@react-native-picker/picker';
import { launchImageLibrary, Asset } from 'react-native-image-picker';
import ReactNativeBlobUtil from 'react-native-blob-util';
import type { RootStackScreenProps } from '../../App';
import theme from '../styles/theme';
import { BASE_URL } from '../config';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'ClubCreate'>;

type Category = { id: number; name: string };
type Province = { id: number; province: string };
type City = { id: number; city: string; province: number };

const ClubCreateScreen: React.FC<Props> = ({ navigation }) => {
  const { token } = useUser();
  const [clubName, setClubName] = useState('');
  const [description, setDescription] = useState('');
  const [selectedImage, setSelectedImage] = useState<Asset | null>(null);
  
  const [categories, setCategories] = useState<Category[]>([]);
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [allCities, setAllCities] = useState<City[]>([]);
  const [filteredCities, setFilteredCities] = useState<City[]>([]);
  
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [selectedProvince, setSelectedProvince] = useState<number | null>(null);
  const [selectedCity, setSelectedCity] = useState<number | null>(null);
  
  const [loading, setLoading] = useState(false);
  const [fetchingData, setFetchingData] = useState(true);

  // 초기 데이터 가져오기 (카테고리, 지역)
  useEffect(() => {
    const fetchData = async () => {
      try {
        // 1. 카테고리 가져오기 (기존 getClubs API 활용)
        const catRes = await fetch(`${BASE_URL}/api/mobile/club/getClubs?category=0`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const catData = await catRes.json();
        if (catData.categoryList) setCategories(catData.categoryList);

        // 2. 지역 정보 가져오기 (마이페이지 정보 API 활용)
        const regionRes = await fetch(`${BASE_URL}/api/mobile/mypage/info`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const regionData = await regionRes.json();
        if (regionData.provinceList) setProvinces(regionData.provinceList);
        if (regionData.regionList) setAllCities(regionData.regionList);

      } catch (error) {
        console.error('Failed to fetch initial data:', error);
        Alert.alert('오류', '데이터를 불러오는 중 문제가 발생했습니다.');
      } finally {
        setFetchingData(false);
      }
    };
    fetchData();
  }, [token]);

  // 도/시 선택 시 해당 지역의 시/군/구 필터링
  useEffect(() => {
    if (selectedProvince) {
      const filtered = allCities.filter(city => city.province === selectedProvince);
      setFilteredCities(filtered);
      if (filtered.length > 0) {
        setSelectedCity(filtered[0].id);
      }
    } else {
      setFilteredCities([]);
    }
  }, [selectedProvince, allCities]);

  const handleSelectImage = () => {
    launchImageLibrary({ mediaType: 'photo', quality: 0.7 }, (response) => {
      if (response.didCancel) return;
      if (response.assets && response.assets.length > 0) {
        setSelectedImage(response.assets[0]);
      }
    });
  };

  const handleCreateClub = async () => {
    if (!clubName.trim()) {
      Alert.alert('알림', '동아리 이름을 입력해주세요.');
      return;
    }
    if (!selectedCategory) {
      Alert.alert('알림', '카테고리를 선택해주세요.');
      return;
    }
    if (!selectedProvince || !selectedCity) {
      Alert.alert('알림', '지역을 선택해주세요.');
      return;
    }
    if (!description.trim()) {
      Alert.alert('알림', '동아리 소개를 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      const uploadData = [
        { name: 'clubName', data: clubName.trim() },
        { name: 'category', data: String(selectedCategory) },
        { name: 'provinceId', data: String(selectedProvince) },
        { name: 'city', data: String(selectedCity) },
        { name: 'clubInfo', data: description.trim() },
      ];

      if (selectedImage && selectedImage.uri) {
        const uri = selectedImage.uri.replace('file://', '');
        uploadData.push({
          name: 'clubImage',
          filename: selectedImage.fileName || `club_img_${Date.now()}.jpg`,
          type: selectedImage.type || 'image/jpeg',
          data: ReactNativeBlobUtil.wrap(uri),
        } as any);
      }

      const response = await ReactNativeBlobUtil.fetch(
        'POST',
        `${BASE_URL}/api/mobile/club/create`,
        {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'multipart/form-data',
        },
        uploadData
      );

      const responseText = await response.text();
      let result;
      try {
        result = JSON.parse(responseText);
      } catch (e) {
        throw new Error('서버 응답 형식이 올바르지 않습니다.');
      }

      if (response.respInfo.status === 200 && result.status === 1) {
        Alert.alert('성공', '동아리가 성공적으로 생성되었습니다.', [
          { text: '확인', onPress: () => navigation.goBack() },
        ]);
      } else {
        Alert.alert('실패', result.message || '동아리 생성에 실패했습니다.');
      }
    } catch (error: any) {
      console.error('Club creation error:', error);
      Alert.alert('오류', `서버 통신 중 오류가 발생했습니다: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  if (fetchingData) {
    return (
      <SafeAreaView style={[styles.safeArea, styles.centered]}>
        <ActivityIndicator size="large" color={theme.colors.primary} />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backButton}>
          <Text style={styles.backIcon}>←</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>동아리 생성</Text>
        <View style={styles.headerRight} />
      </View>

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.card}>
          <Text style={styles.label}>동아리 이름</Text>
          <TextInput
            style={styles.input}
            placeholder="동아리 이름을 입력하세요"
            placeholderTextColor={theme.colors.textLight}
            value={clubName}
            onChangeText={setClubName}
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>카테고리</Text>
          <View style={styles.pickerWrapper}>
            <Picker
              selectedValue={selectedCategory}
              onValueChange={(itemValue) => setSelectedCategory(itemValue)}
              style={styles.picker}
            >
              <Picker.Item label="카테고리 선택" value={null} />
              {categories.map(cat => (
                <Picker.Item key={cat.id} label={cat.name} value={cat.id} />
              ))}
            </Picker>
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>활동 지역</Text>
          <View style={styles.regionContainer}>
            <View style={[styles.pickerWrapper, styles.flexOne]}>
              <Picker
                selectedValue={selectedProvince}
                onValueChange={(itemValue) => setSelectedProvince(itemValue)}
                style={styles.picker}
              >
                <Picker.Item label="시/도 선택" value={null} />
                {provinces.map(p => (
                  <Picker.Item key={p.id} label={p.province} value={p.id} />
                ))}
              </Picker>
            </View>
            <View style={[styles.pickerWrapper, styles.flexOne]}>
              <Picker
                selectedValue={selectedCity}
                onValueChange={(itemValue) => setSelectedCity(itemValue)}
                style={styles.picker}
                enabled={!!selectedProvince}
              >
                <Picker.Item label="시/군/구" value={null} />
                {filteredCities.map(c => (
                  <Picker.Item key={c.id} label={c.city} value={c.id} />
                ))}
              </Picker>
            </View>
          </View>
        </View>
        
        <View style={styles.card}>
          <Text style={styles.label}>동아리 소개</Text>
          <TextInput
            style={[styles.input, styles.multilineInput]}
            placeholder="동아리에 대해 자유롭게 소개해주세요."
            placeholderTextColor={theme.colors.textLight}
            value={description}
            onChangeText={setDescription}
            multiline
            textAlignVertical="top"
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>대표 이미지</Text>
          <TouchableOpacity 
            style={styles.imagePickerButton} 
            activeOpacity={0.7}
            onPress={handleSelectImage}
          >
            {selectedImage ? (
              <Image source={{ uri: selectedImage.uri }} style={styles.selectedImage} />
            ) : (
              <>
                <Text style={styles.imagePickerIcon}>📷</Text>
                <Text style={styles.imagePickerText}>이미지 선택</Text>
              </>
            )}
          </TouchableOpacity>
        </View>

      </ScrollView>

      <View style={styles.bottomButtonContainer}>
        <TouchableOpacity 
          style={styles.createButton} 
          onPress={handleCreateClub}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text style={styles.createButtonText}>생성하기</Text>
          )}
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

export default ClubCreateScreen;

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.bodyBg,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
    backgroundColor: theme.colors.white,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.borderColor,
  },
  backButton: {
    padding: 4,
  },
  backIcon: {
    fontSize: 24,
    color: theme.colors.textPrimary,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: theme.colors.textPrimary,
  },
  headerRight: {
    width: 28, // to balance the back button
  },
  container: {
    flex: 1,
  },
  contentContainer: {
    padding: 16,
  },
  card: {
    backgroundColor: theme.colors.white,
    borderRadius: 16,
    padding: 18,
    marginBottom: 16,
    ...Platform.select({
      ios: { shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.08, shadowRadius: 8 },
      android: { elevation: 3 },
    }),
  },
  label: {
    fontSize: 16,
    fontWeight: '700',
    color: theme.colors.textPrimary,
    marginBottom: 12,
  },
  input: {
    backgroundColor: theme.colors.bodyBg,
    borderWidth: 1.5,
    borderColor: theme.colors.borderColor,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 15,
    fontSize: 15,
    color: theme.colors.textPrimary,
  },
  multilineInput: {
    minHeight: 120,
    lineHeight: 22,
  },
  pickerWrapper: {
    backgroundColor: theme.colors.bodyBg,
    borderWidth: 1.5,
    borderColor: theme.colors.borderColor,
    borderRadius: 12,
    justifyContent: 'center',
  },
  picker: {
    // On iOS, picker has its own UI, so we can't style it much here.
    // On Android, this can be used for basic styling.
  },
  regionContainer: {
    flexDirection: 'row',
    gap: 12,
  },
  flexOne: {
    flex: 1,
  },
  centered: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  imagePickerButton: {
    height: 150,
    backgroundColor: theme.colors.bodyBg,
    borderWidth: 2,
    borderColor: theme.colors.primary,
    borderStyle: 'dashed',
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
    overflow: 'hidden',
  },
  selectedImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  imagePickerIcon: {
    fontSize: 30,
  },
  imagePickerText: {
    fontSize: 12,
    color: theme.colors.primary,
    fontWeight: '700',
    marginTop: 8,
  },
  bottomButtonContainer: {
    padding: 16,
    backgroundColor: theme.colors.white,
    borderTopWidth: 1,
    borderTopColor: theme.colors.borderColor,
  },
  createButton: {
    backgroundColor: theme.colors.primary,
    paddingVertical: 16,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  createButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: theme.colors.white,
  },
});
