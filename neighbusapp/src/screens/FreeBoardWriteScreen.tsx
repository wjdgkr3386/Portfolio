import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  TextInput,
  ScrollView,
  Modal,
  Alert,
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { actions, RichEditor, RichToolbar } from 'react-native-pell-rich-editor';
import { launchImageLibrary } from 'react-native-image-picker';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';
import type { RootStackScreenProps } from '../../App';
import theme from '../styles/theme';
import { useUser } from '../context/UserContext';
import { BASE_URL } from '../config';

type Props = RootStackScreenProps<'FreeBoardWrite'>;

type Club = {
  id: number;
  clubName: string;
};

const FONT_SIZES = [
  { label: '작게', value: '3' },
  { label: '보통', value: '4' },
  { label: '크게', value: '5' },
];

const COLORS = [
  '#000000', '#EE2323', '#F29B0E', '#209E2F', '#217AF4', '#A67C52', '#FFFFFF'
];

interface RichEditorExtended extends RichEditor {
  focusContentEditor: () => void;
}

const FreeBoardWriteScreen: React.FC<Props> = ({ navigation }) => {
  const { token } = useUser();
  const richText = useRef<RichEditorExtended>(null);
  
  const [clubs, setClubs] = useState<Club[]>([]);
  const [selectedClubId, setSelectedClubId] = useState<number | null>(null);
  const [selectedClubName, setSelectedClubName] = useState('');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  
  const [showClubModal, setShowClubModal] = useState(false);
  const [showColorModal, setShowColorModal] = useState(false);
  const [showSizeModal, setShowSizeModal] = useState(false);
  const [loading, setLoading] = useState(false);

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
    if (title || content) {
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
    // Editor에서 최신 컨텐츠 가져오기
    const html = await richText.current?.getContentHtml();
    
    if (!selectedClubId) {
      Alert.alert('알림', '동아리를 선택해주세요.');
      return;
    }
    if (!title.trim()) {
      Alert.alert('알림', '제목을 입력해주세요.');
      return;
    }
    if (!html || html.trim() === '' || html === '<br>') {
      Alert.alert('알림', '내용을 입력해주세요.');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/freeboard/write`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          clubId: selectedClubId,
          title: title,
          content: html,
        }),
      });

      const data = await response.json();

      if (data.success) {
        Alert.alert('알림', '게시글이 등록되었습니다.', [
          { text: '확인', onPress: () => navigation.goBack() },
        ]);
      } else {
        Alert.alert('등록 실패', data.message || '게시글 등록 중 오류가 발생했습니다.');
      }
    } catch (error) {
      console.error('Write post error:', error);
      Alert.alert('오류', '서버 통신 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const onPressAddImage = useCallback(() => {
    launchImageLibrary({ 
      mediaType: 'photo', 
      quality: 0.5, // 이미지 용량 최적화를 위해 퀄리티 조절
      includeBase64: true // DB 저장을 위해 Base64 포함
    }, async (response) => {
      if (response.didCancel || !response.assets || response.assets.length === 0) return;
      
      const asset = response.assets[0];
      
      if (asset.base64) {
        // 이미지를 Base64 데이터 URI 형식으로 에디터에 직접 삽입
        const dataUri = `data:${asset.type};base64,${asset.base64}`;
        richText.current?.insertImage(dataUri);
      } else {
        Alert.alert('오류', '이미지 데이터를 읽어오는데 실패했습니다.');
      }
    });
  }, []);

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity onPress={handleClose}>
          <Text style={styles.headerButtonText}>✕</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>게시글 작성</Text>
        <TouchableOpacity onPress={handleSave} disabled={loading}>
          {loading ? (
            <ActivityIndicator size="small" color={theme.colors.primary} />
          ) : (
            <Text style={styles.headerSubmitText}>등록</Text>
          )}
        </TouchableOpacity>
      </View>

      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={{ flex: 1 }}
      >
        <View style={styles.topSection}>
          <TouchableOpacity
            style={styles.categoryButton}
            onPress={() => setShowClubModal(true)}
          >
            <Text style={[styles.categoryButtonText, !selectedClubId && styles.placeholder]}>
              {selectedClubName || '동아리 선택'}
            </Text>
            <Icon name="chevron-down" size={16} color={theme.colors.textSecondary} />
          </TouchableOpacity>

          <TextInput
            style={styles.titleInput}
            placeholder="제목을 입력하세요"
            placeholderTextColor={theme.colors.textLight}
            value={title}
            onChangeText={setTitle}
          />
        </View>

        <RichToolbar
          editor={richText}
          actions={[
            actions.insertImage,
            actions.setBold,
            actions.setUnderline,
            actions.alignLeft,
            actions.alignCenter,
            actions.alignRight,
            'foreColor',
            'fontSize',
            actions.insertLine,
          ]}
          iconMap={{
            foreColor: ({ tintColor }: any) => <Icon name="palette" size={20} color={tintColor} />,
            fontSize: ({ tintColor }: any) => <Icon name="format-size" size={20} color={tintColor} />,
          }}
          foreColor={() => {
            richText.current?.focusContentEditor();
            setShowColorModal(true);
            setShowSizeModal(false); // Close other picker
          }}
          fontSize={() => {
            richText.current?.focusContentEditor();
            setShowSizeModal(true);
            setShowColorModal(false); // Close other picker
          }}
          onPressAddImage={onPressAddImage}
          selectedIconTint={theme.colors.primary}
          iconTint={theme.colors.textSecondary}
          style={styles.toolbar}
        />

        <View style={styles.editorWrapper}>
          {/* 선택창을 에디터 상단에 배치하여 키보드/선택메뉴 간섭 방지 */}
          {showColorModal && (
            <View style={styles.inlinePickerContainer}>
              <View style={styles.pickerHeader}>
                <Text style={styles.pickerTitle}>색상 선택</Text>
                <TouchableOpacity onPress={() => setShowColorModal(false)}>
                  <Icon name="close" size={20} color={theme.colors.textSecondary} />
                </TouchableOpacity>
              </View>
              <View style={styles.colorGrid}>
                {COLORS.map(color => (
                  <TouchableOpacity
                    key={color}
                    style={[styles.colorOption, { backgroundColor: color }]}
                    onPress={() => {
                      richText.current?.setForeColor(color);
                      setShowColorModal(false);
                    }}
                  />
                ))}
              </View>
            </View>
          )}

          {showSizeModal && (
            <View style={styles.inlinePickerContainer}>
              <View style={styles.pickerHeader}>
                <Text style={styles.pickerTitle}>글꼴 크기</Text>
                <TouchableOpacity onPress={() => setShowSizeModal(false)}>
                  <Icon name="close" size={20} color={theme.colors.textSecondary} />
                </TouchableOpacity>
              </View>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.sizeList}>
                {FONT_SIZES.map(size => (
                  <TouchableOpacity
                    key={size.value}
                    style={styles.sizeOption}
                    onPress={() => {
                      richText.current?.setFontSize(size.value as any);
                      setShowSizeModal(false);
                    }}
                  >
                    <Text style={styles.sizeOptionText}>{size.label}</Text>
                  </TouchableOpacity>
                ))}
              </ScrollView>
            </View>
          )}

          <RichEditor
            ref={richText}
            placeholder="내용을 입력하세요."
            onChange={setContent}
            initialHeight={400}
            style={styles.editor}
            editorStyle={{
              backgroundColor: theme.colors.white,
              color: theme.colors.textPrimary,
              placeholderColor: theme.colors.textLight,
              contentCSSText: 'font-family: sans-serif; font-size: 16px; line-height: 1.6;',
            }}
          />
        </View>
      </KeyboardAvoidingView>

      {/* 동아리 선택 모달 */}
      <Modal visible={showClubModal} transparent animationType="fade">
        <TouchableOpacity style={styles.modalOverlay} onPress={() => setShowClubModal(false)}>
          <View style={styles.modalContainer}>
            <Text style={styles.modalTitle}>동아리 선택</Text>
            <ScrollView>
              {clubs.map(club => (
                <TouchableOpacity
                  key={club.id}
                  style={styles.optionItem}
                  onPress={() => {
                    setSelectedClubId(club.id);
                    setSelectedClubName(club.clubName);
                    setShowClubModal(false);
                  }}
                >
                  <Text style={styles.optionText}>{club.clubName}</Text>
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
  safeArea: { flex: 1, backgroundColor: theme.colors.white },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.borderColor,
  },
  headerButtonText: { fontSize: 22, color: theme.colors.textPrimary },
  headerTitle: { fontSize: 18, fontWeight: '700', color: theme.colors.textPrimary },
  headerSubmitText: { fontSize: 16, color: theme.colors.primary, fontWeight: '700' },
  topSection: { padding: 16, borderBottomWidth: 1, borderBottomColor: '#F0F0F0' },
  categoryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F8F8F8',
    padding: 12,
    borderRadius: 8,
    marginBottom: 12,
  },
  categoryButtonText: { fontSize: 15, color: theme.colors.textPrimary },
  placeholder: { color: theme.colors.textLight },
  titleInput: { fontSize: 20, fontWeight: '700', color: theme.colors.textPrimary, paddingVertical: 8 },
  toolbar: { backgroundColor: '#F8F8F8', borderBottomWidth: 1, borderBottomColor: theme.colors.borderColor },
  editorWrapper: { 
    flex: 1,
    position: 'relative', // Relative positioning for absolute children (pickers)
  },
  editor: { flex: 1 },
  inlinePickerContainer: {
    position: 'absolute',
    top: 0, // Position at the top of the editor area
    left: 0,
    right: 0,
    backgroundColor: 'white',
    padding: 16,
    borderBottomWidth: 1, // Bottom border instead of top
    borderBottomColor: '#DDD',
    zIndex: 1000, // High zIndex to stay above everything
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  pickerHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  pickerTitle: { fontSize: 14, fontWeight: '700', color: theme.colors.textSecondary },
  sizeList: { paddingVertical: 8 },
  sizeOption: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    backgroundColor: '#F0F0F0',
    borderRadius: 20,
    marginRight: 10,
  },
  sizeOptionText: { fontSize: 14, color: theme.colors.textPrimary },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'center', alignItems: 'center' },
  modalContainer: { width: '80%', backgroundColor: 'white', borderRadius: 16, padding: 20 },
  modalTitle: { fontSize: 18, fontWeight: '700', marginBottom: 16, textAlign: 'center' },
  optionItem: { paddingVertical: 15, borderBottomWidth: 1, borderBottomColor: '#F0F0F0' },
  optionText: { fontSize: 16, textAlign: 'center', color: theme.colors.textPrimary },
  colorGrid: { flexDirection: 'row', flexWrap: 'wrap', justifyContent: 'center', gap: 10 },
  colorOption: { width: 40, height: 40, borderRadius: 20, borderWidth: 1, borderColor: '#DDD' },
});

export default FreeBoardWriteScreen;
