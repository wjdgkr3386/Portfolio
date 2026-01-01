// src/screens/GalleryWriteScreen.tsx
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  ScrollView,
  Alert,
  Image,
  Dimensions,
  ActivityIndicator,
  Modal,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import ReactNativeBlobUtil from 'react-native-blob-util';
import type { RootStackScreenProps } from '../../App';
import theme from '../styles/theme';
import { BASE_URL } from '../config';
import { useUser } from '../context/UserContext';
import { launchImageLibrary, Asset } from 'react-native-image-picker';

const { width: SCREEN_WIDTH } = Dimensions.get('window');
const GRID_PADDING = 16 + 18; 
const GAP = 10;
const BOX_SIZE = (SCREEN_WIDTH - (GRID_PADDING * 2) - (GAP * 2)) / 3;

type Props = RootStackScreenProps<'GalleryWrite'>;

type Club = {
  id: number;
  clubName: string;
};

const TITLE_MAX_LENGTH = 50;
const CONTENT_MAX_LENGTH = 1000;
const MAX_IMAGES = 6;

const GalleryWriteScreen: React.FC<Props> = ({ navigation }) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [images, setImages] = useState<Asset[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [clubs, setClubs] = useState<Club[]>([]);
  const [selectedClubId, setSelectedClubId] = useState<number | null>(null);
  const [selectedClubName, setSelectedClubName] = useState('');
  const [showClubModal, setShowClubModal] = useState(false);

  const { token } = useUser();

  // 내 동아리 목록 가져오기
  useEffect(() => {
    const fetchMyClubs = async () => {
      try {
        const response = await fetch(`${BASE_URL}/api/mobile/mypage/info`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        const result = await response.json();
        if (response.ok && Array.isArray(result.myClubs)) {
          setClubs(result.myClubs);
        }
      } catch (error) {
        console.error('Failed to fetch clubs:', error);
      }
    };
    fetchMyClubs();
  }, [token]);

  const handleClose = () => {
    if (title || content || images.length > 0) {
      Alert.alert(
        '작성 취소',
        '작성중인 내용이 있습니다. 정말 취소하시겠습니까?',
        [
          { text: '계속 작성', style: 'cancel' },
          { text: '취소', style: 'destructive', onPress: () => navigation.goBack() },
        ]
      );
    } else {
      navigation.goBack();
    }
  };

  const handleSave = async () => {
    if (!selectedClubId) {
      Alert.alert('알림', '동아리를 선택해주세요.');
      return;
    }
    if (!title.trim()) {
      Alert.alert('알림', '제목을 입력해주세요.');
      return;
    }
    if (images.length === 0) {
      Alert.alert('알림', '최소 1장 이상의 이미지를 선택해주세요.');
      return;
    }
    if (!token) {
      Alert.alert('오류', '로그인이 필요합니다.');
      return;
    }

    setIsSaving(true);

    try {
      // 이미지 파일 정보 구성
      const uploadImages = images.map((image, index) => {
        const uri = image.uri?.replace('file://', '') || '';
        return {
          name: 'fileList',
          filename: image.fileName || `gallery_${index}_${Date.now()}.jpg`,
          type: image.type || 'image/jpeg',
          data: ReactNativeBlobUtil.wrap(uri),
        };
      });

      const response = await ReactNativeBlobUtil.fetch(
        'POST',
        `${BASE_URL}/api/mobile/gallery/insertGallery`,
        {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'multipart/form-data',
        },
        [
          { name: 'title', data: title.trim() },
          { name: 'content', data: content.trim() },
          { name: 'clubId', data: String(selectedClubId) },
          ...uploadImages,
        ]
      );

      const responseText = await response.text();

      let responseData;
      try {
        responseData = JSON.parse(responseText);
      } catch (e) {
        throw new Error('서버 응답 형식이 올바르지 않습니다.');
      }

      if (response.respInfo.status === 200 && responseData.status === 1) {
        Alert.alert('성공', '게시글이 등록되었습니다.', [
          { text: '확인', onPress: () => navigation.goBack() },
        ]);
      } else {
        Alert.alert('실패', responseData.message || '등록 처리 중 오류가 발생했습니다.');
      }
    } catch (error: any) {
      Alert.alert('오류', `서버 통신 중 오류가 발생했습니다: ${error.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleSelectImage = async () => {
    if (images.length >= MAX_IMAGES) {
      Alert.alert('알림', `이미지는 최대 ${MAX_IMAGES}장까지 추가할 수 있습니다.`);
      return;
    }
    try {
      const result = await launchImageLibrary({
        mediaType: 'photo',
        selectionLimit: MAX_IMAGES - images.length,
        quality: 0.5,
      });

      if (result.didCancel) return;
      if (result.errorCode) {
        Alert.alert('오류', `이미지 선택 중 오류가 발생했습니다.`);
        return;
      }
      if (result.assets) {
        setImages([...images, ...result.assets]);
      }
    } catch (error) {
      Alert.alert('오류', '이미지 라이브러리를 열 수 없습니다.');
    }
  };

  const handleRemoveImage = (index: number) => {
    setImages(images.filter((_, i) => i !== index));
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity onPress={handleClose} style={styles.backButton}>
          <Icon name="close" size={24} color={theme.colors.textPrimary} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>갤러리 글쓰기</Text>
        <TouchableOpacity onPress={handleSave} disabled={isSaving}>
          {isSaving ? (
            <ActivityIndicator size="small" color={theme.colors.primary} />
          ) : (
            <Text style={styles.headerSubmitText}>등록</Text>
          )}
        </TouchableOpacity>
      </View>

      <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer} showsVerticalScrollIndicator={false}>
        <View style={styles.sectionCard}>
          <Text style={styles.inputLabel}>동아리 선택</Text>
          <TouchableOpacity style={styles.clubSelectButton} onPress={() => setShowClubModal(true)}>
            <Text style={[styles.clubSelectText, !selectedClubId && styles.placeholder]}>
              {selectedClubName || '게시글을 올릴 동아리를 선택하세요'}
            </Text>
            <Icon name="chevron-down" size={20} color={theme.colors.textSecondary} />
          </TouchableOpacity>
        </View>

        <View style={styles.sectionCard}>
          <View style={styles.inputHeader}>
            <Text style={styles.inputLabel}>제목</Text>
            <Text style={styles.charCount}>{title.length}/{TITLE_MAX_LENGTH}</Text>
          </View>
          <TextInput
            style={styles.titleInput}
            placeholder="제목을 입력해주세요"
            placeholderTextColor={theme.colors.textLight}
            value={title}
            onChangeText={setTitle}
            maxLength={TITLE_MAX_LENGTH}
          />
        </View>

        <View style={styles.sectionCard}>
          <View style={styles.inputHeader}>
            <Text style={styles.inputLabel}>사진</Text>
            <Text style={styles.charCount}>{images.length}/{MAX_IMAGES}</Text>
          </View>
          <View style={styles.imageGrid}>
            {images.map((image, index) => (
              <View key={image.uri || index} style={styles.imageContainer}>
                <Image source={{ uri: image.uri }} style={styles.imagePreview} />
                <TouchableOpacity style={styles.imageDeleteButton} onPress={() => handleRemoveImage(index)}>
                  <Icon name="close-circle" size={20} color={theme.colors.danger} />
                </TouchableOpacity>
              </View>
            ))}
            {images.length < MAX_IMAGES && (
              <TouchableOpacity style={styles.imageAddButton} onPress={handleSelectImage}>
                <Icon name="camera-plus" size={30} color={theme.colors.primary} />
                <Text style={styles.imageAddText}>사진 추가</Text>
              </TouchableOpacity>
            )}
          </View>
        </View>

        <View style={styles.sectionCard}>
          <View style={styles.inputHeader}>
            <Text style={styles.inputLabel}>내용</Text>
            <Text style={styles.charCount}>{content.length}/{CONTENT_MAX_LENGTH}</Text>
          </View>
          <TextInput
            style={styles.contentInput}
            placeholder="내용을 입력해주세요"
            placeholderTextColor={theme.colors.textLight}
            value={content}
            onChangeText={setContent}
            multiline
            textAlignVertical="top"
            maxLength={CONTENT_MAX_LENGTH}
          />
        </View>
      </ScrollView>

      <Modal visible={showClubModal} transparent animationType="fade">
        <TouchableOpacity style={styles.modalOverlay} activeOpacity={1} onPress={() => setShowClubModal(false)}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>동아리 선택</Text>
            <ScrollView>
              {clubs.map((club) => (
                <TouchableOpacity
                  key={club.id}
                  style={[styles.optionItem, selectedClubId === club.id && styles.optionItemActive]}
                  onPress={() => {
                    setSelectedClubId(club.id);
                    setSelectedClubName(club.clubName);
                    setShowClubModal(false);
                  }}
                >
                  <Text style={[styles.optionText, selectedClubId === club.id && styles.optionTextActive]}>{club.clubName}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>
        </TouchableOpacity>
      </Modal>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: theme.colors.bodyBg },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: theme.colors.white,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.borderColor,
  },
  backButton: { padding: 4 },
  headerTitle: { fontSize: 18, fontWeight: '700', color: theme.colors.textPrimary },
  headerSubmitText: { fontSize: 16, color: theme.colors.primary, fontWeight: '700' },
  container: { flex: 1 },
  contentContainer: { padding: 16, paddingBottom: 30 },
  sectionCard: {
    backgroundColor: theme.colors.white,
    borderRadius: 16,
    padding: 18,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: 'rgba(166, 124, 82, 0.1)',
  },
  inputHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 },
  inputLabel: { fontSize: 16, fontWeight: '700', color: theme.colors.textPrimary },
  charCount: { fontSize: 12, color: theme.colors.textSecondary },
  clubSelectButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: theme.colors.bodyBg,
    borderRadius: 12,
    padding: 15,
    marginTop: 10,
  },
  clubSelectText: { fontSize: 15, color: theme.colors.textPrimary },
  placeholder: { color: theme.colors.textLight },
  titleInput: { backgroundColor: theme.colors.bodyBg, borderRadius: 12, padding: 15, fontSize: 16, color: theme.colors.textPrimary },
  imageGrid: { 
    flexDirection: 'row', 
    flexWrap: 'wrap', 
    gap: 10,
    marginTop: 5,
  },
  imageContainer: { 
    width: BOX_SIZE,
    height: BOX_SIZE,
    borderRadius: 12, 
    overflow: 'hidden', 
    position: 'relative',
    borderWidth: 1,
    borderColor: '#E5E5E5',
    backgroundColor: '#F8F8F8',
    marginBottom: 5,
  },
  imagePreview: { width: '100%', height: '100%', resizeMode: 'cover' },
  imageDeleteButton: { 
    position: 'absolute', 
    top: 5, 
    right: 5, 
    backgroundColor: 'white', 
    borderRadius: 12,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 2,
  },
  imageAddButton: { 
    width: BOX_SIZE,
    height: BOX_SIZE,
    borderRadius: 12, 
    borderWidth: 1, 
    borderColor: theme.colors.primary, 
    borderStyle: 'dashed', 
    justifyContent: 'center', 
    alignItems: 'center',
    backgroundColor: '#F8F8F8',
    marginBottom: 5,
  },
  imageAddText: { fontSize: 12, color: theme.colors.primary, marginTop: 4 },
  contentInput: { backgroundColor: theme.colors.bodyBg, borderRadius: 12, padding: 15, fontSize: 15, minHeight: 150 },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContainer: {
    width: '80%',
    maxHeight: '60%',
    backgroundColor: theme.colors.white,
    borderRadius: 20,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 5,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: theme.colors.textPrimary,
    marginBottom: 16,
    textAlign: 'center',
  },
  optionItem: {
    paddingVertical: 15,
    paddingHorizontal: 16,
    borderRadius: 10,
    marginBottom: 8,
  },
  optionItemActive: {
    backgroundColor: theme.colors.primaryLight,
  },
  optionText: {
    fontSize: 16,
    color: theme.colors.textPrimary,
    textAlign: 'center',
  },
  optionTextActive: {
    fontWeight: '700',
    color: theme.colors.primary,
  },
});

export default GalleryWriteScreen;