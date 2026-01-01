// src/components/SideMenu.tsx
import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Modal,
  Animated,
  TouchableWithoutFeedback,
  Alert,
  ScrollView,
  TextInput,
} from 'react-native';
import { useUser } from '../context/UserContext';

const INQUIRY_TYPES = ['서비스 이용 문의', '기능 오류 신고', '개선 제안', '계정 문의', '기타'];

type SideMenuProps = {
  visible: boolean;
  onClose: () => void;
  navigation: any;
};

const SideMenu: React.FC<SideMenuProps> = ({ visible, onClose, navigation }) => {
  const { user, setUser } = useUser();
  const slideAnim = React.useRef(new Animated.Value(300)).current;

  const [showInquiryModal, setShowInquiryModal] = useState(false);
  const [showTypeDropdown, setShowTypeDropdown] = useState(false);
  const [inquiryType, setInquiryType] = useState('');
  const [inquiryTitle, setInquiryTitle] = useState('');
  const [inquiryContent, setInquiryContent] = useState('');

  React.useEffect(() => {
    if (visible) {
      Animated.spring(slideAnim, {
        toValue: 0,
        useNativeDriver: true,
        tension: 100, // Increased for faster response
        friction: 20, // Increased to reduce bounciness
      }).start();
    } else {
      Animated.timing(slideAnim, {
        toValue: 300,
        duration: 250,
        useNativeDriver: true,
      }).start();
    }

    return () => {
      slideAnim.stopAnimation();
    };
  }, [visible, slideAnim]);

  const handleNotice = () => {
    onClose();
    navigation.navigate('NoticeList');
  };

  const handleInquiry = () => {
    setShowInquiryModal(true);
  };

  const handleSelectType = (type: string) => {
    setInquiryType(type);
    setShowTypeDropdown(false);
  };

  const handleSubmitInquiry = () => {
    if (!inquiryType) {
      Alert.alert('알림', '문의 유형을 선택해주세요.');
      return;
    }
    if (!inquiryTitle.trim()) {
      Alert.alert('알림', '제목을 입력해주세요.');
      return;
    }
    if (!inquiryContent.trim()) {
      Alert.alert('알림', '내용을 입력해주세요.');
      return;
    }

    Alert.alert('문의 접수 완료', '문의가 성공적으로 접수되었습니다.\n빠른 시일 내에 답변드리겠습니다.', [
      {
        text: '확인',
        onPress: () => {
          setShowInquiryModal(false);
          setInquiryType('');
          setInquiryTitle('');
          setInquiryContent('');
        },
      },
    ]);
  };

  const handleCloseInquiry = () => {
    setShowInquiryModal(false);
    setShowTypeDropdown(false);
    setInquiryType('');
    setInquiryTitle('');
    setInquiryContent('');
  };

  const handleLogout = () => {
    Alert.alert(
      '로그아웃',
      '정말 로그아웃 하시겠습니까?',
      [
        { text: '취소', style: 'cancel' },
        {
          text: '로그아웃',
          style: 'destructive',
          onPress: () => {
            setUser(null);
            onClose();
            navigation.reset({
              index: 0,
              routes: [{ name: 'Login' }],
            });
          },
        },
      ]
    );
  };

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="none"
      onRequestClose={onClose}
    >
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay}>
          <TouchableWithoutFeedback>
            <Animated.View
              style={[
                styles.menuContainer,
                {
                  transform: [{ translateX: slideAnim }],
                },
              ]}
            >
              {/* 헤더 */}
              <View style={styles.header}>
                <Text style={styles.headerTitle}>메뉴</Text>
                <TouchableOpacity onPress={onClose} style={styles.closeButton}>
                  <Text style={styles.closeIcon}>✕</Text>
                </TouchableOpacity>
              </View>

              {/* 사용자 정보 */}
              <View style={styles.userSection}>
                <View style={styles.userAvatar}>
                  <Text style={styles.userAvatarText}>👤</Text>
                </View>
                <Text style={styles.userName}>{user?.name || '사용자'} 님</Text>
                <Text style={styles.userGreeting}>안녕하세요!</Text>
              </View>

              {/* 구분선 */}
              <View style={styles.divider} />

              {/* 메뉴 항목들 */}
              <View style={styles.menuList}>
                <TouchableOpacity
                  style={styles.menuItem}
                  onPress={handleNotice}
                  activeOpacity={0.7}
                >
                  <Text style={styles.menuIcon}>📢</Text>
                  <Text style={styles.menuText}>공지사항</Text>
                  <Text style={styles.menuArrow}>›</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={styles.menuItem}
                  onPress={handleInquiry}
                  activeOpacity={0.7}
                >
                  <Text style={styles.menuIcon}>💬</Text>
                  <Text style={styles.menuText}>문의하기</Text>
                  <Text style={styles.menuArrow}>›</Text>
                </TouchableOpacity>

                <View style={styles.menuDivider} />

                <TouchableOpacity
                  style={styles.menuItem}
                  onPress={handleLogout}
                  activeOpacity={0.7}
                >
                  <Text style={styles.menuIcon}>🚪</Text>
                  <Text style={styles.menuTextLogout}>로그아웃</Text>
                  <Text style={styles.menuArrow}>›</Text>
                </TouchableOpacity>
              </View>

              {/* 앱 정보 */}
              <View style={styles.footer}>
                <Text style={styles.footerText}>NEIGHBUS v1.0</Text>
                <Text style={styles.footerSubtext}>함께하는 이웃</Text>
              </View>
            </Animated.View>
          </TouchableWithoutFeedback>
        </View>
      </TouchableWithoutFeedback>

      {/* 문의하기 모달 */}
      <Modal
        visible={showInquiryModal}
        transparent={true}
        animationType="slide"
        onRequestClose={handleCloseInquiry}
      >
        <View style={styles.inquiryModalOverlay}>
          <View style={styles.inquiryModalContainer}>
            {/* 모달 헤더 */}
            <View style={styles.inquiryModalHeader}>
              <Text style={styles.inquiryModalTitle}>문의하기</Text>
              <TouchableOpacity onPress={handleCloseInquiry}>
                <Text style={styles.inquiryCloseButton}>✕</Text>
              </TouchableOpacity>
            </View>

            <ScrollView style={styles.inquiryModalContent} showsVerticalScrollIndicator={false}>
              {/* 문의 유형 드롭다운 */}
              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>문의 유형 <Text style={styles.required}>*</Text></Text>
                <TouchableOpacity
                  style={styles.dropdownButton}
                  onPress={() => setShowTypeDropdown(!showTypeDropdown)}
                  activeOpacity={0.7}
                >
                  <Text style={[styles.dropdownButtonText, !inquiryType && styles.placeholderText]}>
                    {inquiryType || '문의 유형을 선택하세요'}
                  </Text>
                  <Text style={styles.dropdownIcon}>{showTypeDropdown ? '▲' : '▼'}</Text>
                </TouchableOpacity>

                {showTypeDropdown && (
                  <View style={styles.dropdownList}>
                    {INQUIRY_TYPES.map((type, index) => (
                      <TouchableOpacity
                        key={index}
                        style={[
                          styles.dropdownItem,
                          index === INQUIRY_TYPES.length - 1 && styles.dropdownItemLast,
                          inquiryType === type && styles.dropdownItemSelected,
                        ]}
                        onPress={() => handleSelectType(type)}
                        activeOpacity={0.7}
                      >
                        <Text style={[
                          styles.dropdownItemText,
                          inquiryType === type && styles.dropdownItemTextSelected,
                        ]}>
                          {type}
                        </Text>
                        {inquiryType === type && <Text style={styles.checkIcon}>✓</Text>}
                      </TouchableOpacity>
                    ))}
                  </View>
                )}
              </View>

              {/* 제목 입력 */}
              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>제목 <Text style={styles.required}>*</Text></Text>
                <TextInput
                  style={styles.textInput}
                  placeholder="제목을 입력하세요"
                  placeholderTextColor="#B8B8B8"
                  value={inquiryTitle}
                  onChangeText={setInquiryTitle}
                  maxLength={100}
                />
                <Text style={styles.charCount}>{inquiryTitle.length}/100</Text>
              </View>

              {/* 내용 입력 */}
              <View style={styles.formGroup}>
                <Text style={styles.formLabel}>내용 <Text style={styles.required}>*</Text></Text>
                <TextInput
                  style={[styles.textInput, styles.textArea]}
                  placeholder="문의 내용을 상세히 입력해주세요"
                  placeholderTextColor="#B8B8B8"
                  value={inquiryContent}
                  onChangeText={setInquiryContent}
                  multiline
                  numberOfLines={8}
                  textAlignVertical="top"
                  maxLength={1000}
                />
                <Text style={styles.charCount}>{inquiryContent.length}/1000</Text>
              </View>

              {/* 안내 메시지 */}
              <View style={styles.infoBox}>
                <Text style={styles.infoIcon}>ℹ️</Text>
                <Text style={styles.infoText}>
                  접수된 문의는 영업일 기준 1-2일 이내에 답변드립니다.
                </Text>
              </View>
            </ScrollView>

            {/* 하단 버튼 */}
            <View style={styles.inquiryModalFooter}>
              <TouchableOpacity
                style={[styles.inquiryModalButton, styles.cancelButton]}
                onPress={handleCloseInquiry}
                activeOpacity={0.7}
              >
                <Text style={styles.cancelButtonText}>취소</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.inquiryModalButton, styles.submitButton]}
                onPress={handleSubmitInquiry}
                activeOpacity={0.7}
              >
                <Text style={styles.submitButtonText}>문의 접수</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </Modal>
  );
};

export default SideMenu;

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'flex-end',
  },
  menuContainer: {
    width: 280,
    height: '100%',
    backgroundColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: -2, height: 0 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 5,
  },

  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 20,
    paddingTop: 60,
    backgroundColor: '#F5EDE4',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: '700',
    color: '#5C4A3A',
  },
  closeButton: {
    padding: 4,
  },
  closeIcon: {
    fontSize: 24,
    color: '#5C4A3A',
  },
  userSection: {
    alignItems: 'center',
    paddingVertical: 32,
    backgroundColor: '#F5EDE4',
  },
  userAvatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#FFFFFF',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    borderWidth: 2,
    borderColor: '#D8D0C8',
  },
  userAvatarText: {
    fontSize: 36,
  },
  userName: {
    fontSize: 18,
    fontWeight: '700',
    color: '#5C4A3A',
    marginBottom: 4,
  },
  userGreeting: {
    fontSize: 14,
    color: '#8B7355',
  },
  divider: {
    height: 8,
    backgroundColor: '#F5F5F5',
  },
  menuList: {
    flex: 1,
    paddingVertical: 16,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingVertical: 18,
  },
  menuIcon: {
    fontSize: 24,
    marginRight: 16,
  },
  menuText: {
    flex: 1,
    fontSize: 16,
    fontWeight: '500',
    color: '#5C4A3A',
  },
  menuTextLogout: {
    flex: 1,
    fontSize: 16,
    fontWeight: '500',
    color: '#D9534F',
  },
  menuArrow: {
    fontSize: 24,
    color: '#B8B8B8',
  },
  menuDivider: {
    height: 1,
    backgroundColor: '#E5E5E5',
    marginVertical: 8,
    marginHorizontal: 24,
  },
  footer: {
    alignItems: 'center',
    paddingVertical: 24,
    borderTopWidth: 1,
    borderTopColor: '#E5E5E5',
  },
  footerText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#8B7355',
    marginBottom: 4,
  },
  footerSubtext: {
    fontSize: 11,
    color: '#B8B8B8',
  },
  // 문의하기 모달
  inquiryModalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'flex-end',
  },
  inquiryModalContainer: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '90%',
    paddingBottom: 20,
  },
  inquiryModalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingTop: 20,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  inquiryModalTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#5C4A3A',
  },
  inquiryCloseButton: {
    fontSize: 28,
    color: '#8B7355',
    padding: 4,
  },
  inquiryModalContent: {
    paddingHorizontal: 24,
    paddingTop: 20,
  },
  formGroup: {
    marginBottom: 24,
  },
  formLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#5C4A3A',
    marginBottom: 8,
  },
  required: {
    color: '#D64545',
  },
  dropdownButton: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#FAFAFA',
    borderWidth: 1.5,
    borderColor: '#D8D0C8',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  dropdownButtonText: {
    fontSize: 15,
    color: '#333333',
  },
  placeholderText: {
    color: '#B8B8B8',
  },
  dropdownIcon: {
    fontSize: 12,
    color: '#9B7E5C',
  },
  dropdownList: {
    marginTop: 8,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#D8D0C8',
    borderRadius: 12,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  dropdownItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 14,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  dropdownItemLast: {
    borderBottomWidth: 0,
  },
  dropdownItemSelected: {
    backgroundColor: '#F5EDE4',
  },
  dropdownItemText: {
    fontSize: 15,
    color: '#5C4A3A',
  },
  dropdownItemTextSelected: {
    fontWeight: '600',
    color: '#9B7E5C',
  },
  checkIcon: {
    fontSize: 16,
    color: '#9B7E5C',
    fontWeight: 'bold',
  },
  textInput: {
    backgroundColor: '#FAFAFA',
    borderWidth: 1.5,
    borderColor: '#D8D0C8',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 15,
    color: '#333333',
  },
  textArea: {
    minHeight: 120,
    paddingTop: 12,
  },
  charCount: {
    fontSize: 12,
    color: '#B8B8B8',
    textAlign: 'right',
    marginTop: 6,
  },
  infoBox: {
    flexDirection: 'row',
    backgroundColor: '#F5F9FF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#D6E8FF',
  },
  infoIcon: {
    fontSize: 16,
    marginRight: 8,
  },
  infoText: {
    flex: 1,
    fontSize: 13,
    color: '#5C7A9B',
    lineHeight: 18,
  },
  inquiryModalFooter: {
    flexDirection: 'row',
    paddingHorizontal: 24,
    paddingTop: 16,
    gap: 12,
  },
  inquiryModalButton: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
  },
  cancelButton: {
    backgroundColor: '#F0F0F0',
  },
  cancelButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#8B7355',
  },
  submitButton: {
    backgroundColor: '#9B7E5C',
    shadowColor: '#9B7E5C',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 3,
  },
  submitButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#FFFFFF',
  },
});
