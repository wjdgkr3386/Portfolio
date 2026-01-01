import React, { useState, useRef, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  TextInput,
  KeyboardAvoidingView,
  Platform,
  Modal,
  Alert,
  PanResponder,
  Image,
  ActivityIndicator,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Clipboard from '@react-native-clipboard/clipboard';
import type { RootStackScreenProps } from '../../App';
import SideMenu from '../components/SideMenu';
import { useUser } from '../context/UserContext';
import BottomNavBar from '../components/BottomNavBar';
import theme from '../styles/theme';
import { BASE_URL } from '../config';

// ★ [1] STOMP 라이브러리 및 인코더 임포트
import { Client } from '@stomp/stompjs';
import { TextEncoder, TextDecoder } from 'text-encoding';

// ★ [2] [핵심 해결] TypeScript에게 global 변수가 있다고 강제로 알려줌
declare var global: any;

// React Native용 Polyfill 설정
if (typeof global.TextEncoder === 'undefined') {
  global.TextEncoder = TextEncoder;
}
if (typeof global.TextDecoder === 'undefined') {
  global.TextDecoder = TextDecoder;
}

type Props = RootStackScreenProps<'Chat'>;

type ChatMessage = { 
  id: string; 
  sender: string; 
  message: string; 
  time: string; 
  isMe: boolean; 
  senderNickname?: string; 
};

type Friend = { 
  id: string; 
  uuid: string; 
  name: string; 
  image: string; 
  status: string; 
  location: string; 
  username?: string; 
};

type FriendRequest = { 
  id: string; 
  uuid: string; 
  name: string; 
  nickname: string;
  image: string; 
  status: string; 
  location: string; 
};

const ChatScreen: React.FC<Props> = ({ navigation }) => {
  const { user, token } = useUser();
  const [showSideMenu, setShowSideMenu] = useState(false);
  const [activeTab, setActiveTab] = useState<'friends' | 'chats'>('friends');
  const [selectedChat, setSelectedChat] = useState<string | null>(null);
  const [messageInput, setMessageInput] = useState('');
  const [showAddFriendModal, setShowAddFriendModal] = useState(false);
  const [showRequestsModal, setShowRequestsModal] = useState(false);
  const [friendUuid, setFriendUuid] = useState('');
  const [selectedFriend, setSelectedFriend] = useState<Friend | null>(null);
  
  const [friends, setFriends] = useState<Friend[]>([]);
  const [friendRequests, setFriendRequests] = useState<FriendRequest[]>([]);
  const [myUuid, setMyUuid] = useState('');
  const [loading, setLoading] = useState(true);
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [currentChatPartner, setCurrentChatPartner] = useState<{ name: string; image: string; username?: string } | null>(null);

  // ★ [3] STOMP 클라이언트 및 FlatList Ref 생성
  const stompClient = useRef<Client | null>(null);
  const flatListRef = useRef<FlatList>(null);

  // 친구 목록 불러오기
  const fetchFriendData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/friend/list`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await response.json();
      
      if (data) {
        setMyUuid(data.myUuid || '');
        if (Array.isArray(data.friends)) {
          setFriends(data.friends.map((f: any) => ({
            id: f.id.toString(),
            uuid: f.user_uuid || '',
            name: f.nickname || f.name || '알 수 없음',
            image: f.image,
            status: f.status || '', 
            location: f.city || f.province || '',
            username: f.username 
          })));
        }
        if (Array.isArray(data.requests)) {
          console.log('Friend Requests Data:', data.requests);
          const mappedRequests: FriendRequest[] = data.requests.map((r: any) => ({
            id: r.id.toString(),
            uuid: r.user_uuid || r.userUuid || '',
            name: r.name || '',
            nickname: r.nickname || r.name || '알 수 없는 사용자',
            image: r.image ? (r.image.startsWith('http') ? r.image : `${BASE_URL}${r.image}`) : '', 
            status: '', 
            location: r.city || r.province || '',
          }));
          setFriendRequests(mappedRequests);
        }
      }
    } catch (error) {
      console.error('Failed to fetch friend list:', error);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (activeTab === 'friends') fetchFriendData();
  }, [fetchFriendData, activeTab]);

  // ★ [4] 채팅방 입장 시 웹소켓 연결 및 구독
  useEffect(() => {
    if (!selectedChat || !token) return;

    const wsUrl = BASE_URL.replace('http', 'ws') + '/ws-stomp/websocket'; 

    const client = new Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`, 
      },
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      onConnect: () => {
        console.log('Connected to WebSocket!');
        
        client.subscribe(`/sub/chat/room/${selectedChat}`, (message) => {
          if (message.body) {
            const receivedMsg = JSON.parse(message.body);
            
            const newChatMsg: ChatMessage = {
              id: receivedMsg.messageId?.toString() || Date.now().toString(),
              sender: receivedMsg.senderNickname || receivedMsg.sender,
              message: receivedMsg.message,
              time: receivedMsg.createdAt 
                ? new Date(receivedMsg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) 
                : new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
              isMe: receivedMsg.sender === user?.username,
              senderNickname: receivedMsg.senderNickname,
            };

            setChatMessages((prev) => [...prev, newChatMsg]);
            
            // 새 메시지 도착 시 스크롤
            setTimeout(() => {
              flatListRef.current?.scrollToEnd({ animated: true });
            }, 100);
          }
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
      forceBinaryWSFrames: true,
      appendMissingNULLonIncoming: true,
    });

    client.activate();
    stompClient.current = client;

    return () => {
      console.log('Deactivating WebSocket...');
      if (client) {
        client.deactivate();
      }
    };
  }, [selectedChat, token, user?.username]);

  const panResponder = useRef(PanResponder.create({
    onStartShouldSetPanResponder: () => false,
    onMoveShouldSetPanResponder: (_, gestureState) => Math.abs(gestureState.dx) > 10,
    onPanResponderRelease: (_, gestureState) => { if (gestureState.dx > 30) setShowSideMenu(true); },
  })).current;

  const handleBackToList = () => setSelectedChat(null);

  // 친구 관련 기능들
  const handleAddFriend = async () => {
    if (!friendUuid.trim()) {
      Alert.alert('알림', 'UUID를 입력해주세요.');
      return;
    }
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/friend/request`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ uuid: friendUuid }),
      });
      const data = await response.json();
      if (data.result === 1) {
        Alert.alert('성공', '친구 요청을 보냈습니다.');
        setFriendUuid('');
        setShowAddFriendModal(false);
        fetchFriendData();
      } else {
        Alert.alert('알림', '요청에 실패했거나 이미 처리된 상태입니다.');
      }
    } catch (error) {
      Alert.alert('오류', '서버 통신 중 오류가 발생했습니다.');
    }
  };

  const handleAcceptRequest = async (request: FriendRequest) => {
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/friend/accept`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ friendId: parseInt(request.id, 10) }),
      });
      const data = await response.json();
      if (data.result === 1) {
        Alert.alert('수락 완료', `${request.name}님과 친구가 되었습니다.`);
        fetchFriendData();
      }
    } catch (error) {
      console.error(error);
    }
  };

  const handleRejectRequest = async (request: FriendRequest) => {
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/friend/refuse`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ friendId: parseInt(request.id, 10) }),
      });
      const data = await response.json();
      if (data.result === 1) {
        Alert.alert('거절 완료', '친구 요청을 거절했습니다.');
        fetchFriendData();
      }
    } catch (error) {
      console.error(error);
    }
  };

  const handleDeleteFriend = async (friend: Friend) => {
    Alert.alert('친구 삭제', `${friend.name}님을 정말로 삭제하시겠습니까?`, [
      { text: '취소', style: 'cancel' },
      { 
        text: '삭제', 
        style: 'destructive', 
        onPress: async () => {
          try {
            const response = await fetch(`${BASE_URL}/api/mobile/friend/delete`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
              body: JSON.stringify({ friendId: parseInt(friend.id, 10) }),
            });
            const data = await response.json();
            if (data.result === 1) {
              Alert.alert('삭제 완료', '친구를 삭제했습니다.');
              setSelectedFriend(null);
              fetchFriendData();
            }
          } catch (error) {
            console.error(error);
          }
        }
      }
    ]);
  };

  const handleOpenChat = async (friend: Friend) => {
    setSelectedFriend(null);
    setCurrentChatPartner({ 
      name: friend.name, 
      image: friend.image || 'https://i.pravatar.cc/150',
      username: friend.username 
    });
    
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/friend/chat/room`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ friendId: parseInt(friend.id, 10) }),
      });
      const data = await response.json();
      
      if (data.roomId) {
        if (data.history) {
          setChatMessages(data.history.map((msg: any) => ({
            id: msg.messageId?.toString() || Math.random().toString(),
            sender: msg.senderNickname || msg.sender,
            message: msg.message,
            time: msg.createdAt 
              ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) 
              : '',
            isMe: msg.sender === user?.username,
            senderNickname: msg.senderNickname,
          })));
        } else {
          setChatMessages([]);
        }
        
        setSelectedChat(data.roomId.toString());
        setActiveTab('chats');
      }
    } catch (error) {
      Alert.alert('오류', '채팅방을 여는 중 오류가 발생했습니다.');
      console.error(error);
    }
  };

  // ★ [5] 메시지 전송 로직
  const handleSendMessage = () => {
    if (!messageInput.trim() || !stompClient.current || !stompClient.current.connected || !selectedChat) {
      console.log('Cannot send message: Client not connected or empty input');
      return;
    }

    const chatMessage = {
      roomId: selectedChat,
      sender: user?.username, 
      message: messageInput,
      messageType: 'TALK',
      recipientUsername: currentChatPartner?.username 
    };

    stompClient.current.publish({
      destination: "/pub/chat/message",
      body: JSON.stringify(chatMessage),
    });

    setMessageInput('');
  };

  const handleCopyMyUuid = () => {
    if (myUuid) {
      Clipboard.setString(myUuid);
      Alert.alert('복사 완료', '아이디가 복사되었습니다.');
    }
  };

  // ★ [개선] 메시지 렌더링 - 웹 버전과 동일하게 닉네임 표시
  const renderMessage = ({ item, index }: { item: ChatMessage, index: number }) => {
    const showTimestamp = index === 0 || chatMessages[index - 1].time !== item.time;
    const prevMessage = index > 0 ? chatMessages[index - 1] : null;
    const showSenderName = !item.isMe && (!prevMessage || prevMessage.sender !== item.sender || prevMessage.isMe);

    return (
      <View>
        {showTimestamp && <Text style={styles.timestamp}>--- {item.time} ---</Text>}
        
        {/* 상대방 메시지인 경우 닉네임 먼저 표시 (웹과 동일) */}
        {!item.isMe && showSenderName && (
          <Text style={styles.senderName}>{item.sender}</Text>
        )}
        
        <View style={[styles.messageRow, item.isMe ? styles.myMessageRow : styles.otherMessageRow]}>
          {!item.isMe && showSenderName && (
            <Image 
              source={{ uri: currentChatPartner?.image || `https://i.pravatar.cc/150?u=${item.sender}` }} 
              style={styles.messageAvatar} 
            />
          )}
          {!item.isMe && !showSenderName && <View style={styles.messageAvatar} />}
          
          <View style={[styles.messageBubble, item.isMe ? styles.myMessageBubble : styles.otherMessageBubble]}>
            <Text style={item.isMe ? styles.myMessageText : styles.otherMessageText}>
              {item.message}
            </Text>
          </View>
        </View>
      </View>
    );
  };

  const renderChatDetail = () => (
    <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.flexOne}>
      <View style={styles.chatHeader}>
        <TouchableOpacity onPress={handleBackToList} style={styles.backButton}>
          <Text style={styles.backIcon}>‹</Text>
        </TouchableOpacity>
        <Image 
          source={{ uri: currentChatPartner?.image || 'https://i.pravatar.cc/150' }} 
          style={styles.chatHeaderImage} 
        />
        <Text style={styles.chatHeaderTitle}>{currentChatPartner?.name || '채팅'}</Text>
        <View style={styles.headerRight} />
      </View>
      
      {/* ★ [6] FlatList Ref 수정 적용 */}
      <FlatList 
        data={chatMessages} 
        renderItem={renderMessage} 
        keyExtractor={(item) => item.id} 
        contentContainerStyle={styles.messagesList}
        ref={flatListRef}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: true })}
        onLayout={() => flatListRef.current?.scrollToEnd({ animated: true })}
      />
      
      <View style={styles.inputContainer}>
        <TextInput 
          style={styles.messageInput} 
          placeholder="메시지 입력..." 
          value={messageInput} 
          onChangeText={setMessageInput} 
          multiline 
        />
        <TouchableOpacity style={styles.sendButton} onPress={handleSendMessage}>
          <Text style={styles.sendButtonText}>➤</Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );

  return (
    <SafeAreaView style={styles.safeArea}>
      {selectedChat ? renderChatDetail() : (
        <View style={styles.flexOne} {...panResponder.panHandlers}>
          <View style={styles.header}>
            <Text style={styles.headerTitle}>친구</Text>
          </View>
          
          <View style={styles.friendButtonContainer}>
            <TouchableOpacity 
              style={styles.friendActionButton} 
              onPress={() => setShowAddFriendModal(true)}
            >
              <Text style={styles.friendActionText}>➕ 친구 추가</Text>
            </TouchableOpacity>
            <TouchableOpacity 
              style={styles.friendActionButton} 
              onPress={() => setShowRequestsModal(true)}
            >
              <Text style={styles.friendActionText}>📬 받은 요청 ({friendRequests.length})</Text>
            </TouchableOpacity>
          </View>
          
          {loading ? (
            <ActivityIndicator size="large" color={theme.colors.primary} style={{ marginTop: 20 }} />
          ) : (
            <FlatList 
              data={friends} 
              renderItem={({ item }) => (
                <TouchableOpacity style={styles.friendItem} onPress={() => setSelectedFriend(item)}>
                  <Image 
                    source={{ uri: item.image || `https://i.pravatar.cc/150?u=${item.uuid}` }} 
                    style={styles.avatar} 
                  />
                  <View style={styles.friendInfo}>
                    <Text style={styles.friendName}>{item.name}</Text>
                    <Text style={styles.friendStatus}>{item.status}</Text>
                  </View>
                </TouchableOpacity>
              )} 
              keyExtractor={(item) => item.id}
              ItemSeparatorComponent={() => <View style={styles.separator} />}
              contentContainerStyle={styles.listContent}
              ListEmptyComponent={
                <Text style={{ textAlign: 'center', marginTop: 40, color: '#888' }}>
                  등록된 친구가 없습니다.
                </Text>
              }
            />
          )}
        </View>
      )}
      
      {!selectedChat && <BottomNavBar currentScreen="Chat" />}

      {/* 친구 프로필 모달 */}
      {selectedFriend && (
        <Modal transparent visible={!!selectedFriend} animationType="fade">
          <TouchableOpacity style={styles.modalOverlay} onPress={() => setSelectedFriend(null)}>
            <View style={styles.profileModalContainer}>
              <Image 
                source={{ uri: selectedFriend.image || 'https://i.pravatar.cc/150' }} 
                style={styles.profileAvatar} 
              />
              <Text style={styles.profileName}>{selectedFriend.name}</Text>
              <View style={styles.profileActions}>
                <TouchableOpacity 
                  style={styles.profileButton} 
                  onPress={() => handleOpenChat(selectedFriend)}
                >
                  <Text style={{color: '#fff'}}>1:1 대화</Text>
                </TouchableOpacity>
                <TouchableOpacity 
                  style={[styles.profileButton, styles.deleteButton]} 
                  onPress={() => handleDeleteFriend(selectedFriend)}
                >
                  <Text style={{color: 'red'}}>친구 삭제</Text>
                </TouchableOpacity>
              </View>
            </View>
          </TouchableOpacity>
        </Modal>
      )}
      
      <SideMenu visible={showSideMenu} onClose={() => setShowSideMenu(false)} navigation={navigation} />
      
      {/* 친구 추가 모달 */}
      <Modal 
        transparent 
        visible={showAddFriendModal} 
        animationType="slide"
        // [추가] 안드로이드 물리 뒤로 가기 버튼 대응
        onRequestClose={() => setShowAddFriendModal(false)}
      >
        {/* [추가] 배경 터치 시 닫히게 하기 위해 전체 영역을 TouchableOpacity로 감쌉니다 */}
        <TouchableOpacity 
          style={styles.modalOverlay} 
          activeOpacity={1} 
          onPress={() => setShowAddFriendModal(false)}
        >
          <KeyboardAvoidingView 
            behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            style={{ width: '100%' }}
          >
            {/* [중요] 실제 모달 컨텐츠 영역 클릭 시에는 닫히지 않도록 전파를 막습니다 */}
            <TouchableOpacity 
              activeOpacity={1} 
              onPress={(e) => e.stopPropagation()} 
              style={[styles.profileModalContainer, { alignItems: 'stretch' }]}
            >
              <Text style={[styles.profileName, { textAlign: 'center', marginBottom: 25 }]}>
                친구 추가
              </Text>
              
              <TextInput 
                style={{
                  width: '100%',
                  height: 55,
                  borderWidth: 2,
                  borderColor: theme.colors.primary,
                  borderRadius: 12,
                  paddingHorizontal: 20,
                  fontSize: 16,
                  color: '#000',
                  backgroundColor: '#fff',
                  marginBottom: 20
                }} 
                placeholder="친구 UUID 코드를 입력하세요"
                placeholderTextColor="#999"
                value={friendUuid}
                onChangeText={setFriendUuid}
                autoCapitalize="none"
              />
              
              <TouchableOpacity 
                style={{
                  width: '100%',
                  height: 55,
                  backgroundColor: theme.colors.primary,
                  borderRadius: 12,
                  justifyContent: 'center',
                  alignItems: 'center'
                }} 
                onPress={handleAddFriend}
              >
                <Text style={{ color: '#fff', fontSize: 18, fontWeight: 'bold' }}>
                  요청 보내기
                </Text>
              </TouchableOpacity>
            </TouchableOpacity>
          </KeyboardAvoidingView>
        </TouchableOpacity>
      </Modal>

      {/* 친구 요청 목록 모달 */}
      <Modal 
        transparent 
        visible={showRequestsModal} 
        animationType="slide"
        // 안드로이드 물리 뒤로 가기 대응
        onRequestClose={() => setShowRequestsModal(false)}
      >
        {/* 배경 터치 시 닫기 */}
        <TouchableOpacity 
          style={styles.modalOverlay} 
          activeOpacity={1} 
          onPress={() => setShowRequestsModal(false)}
        >
          {/* 컨텐츠 영역 클릭 시에는 안 닫히게 전파 방지 */}
          <TouchableOpacity 
            activeOpacity={1} 
            onPress={(e) => e.stopPropagation()} 
            style={[styles.profileModalContainer, { maxHeight: '70%', alignItems: 'stretch' }]}
          >
            <Text style={[styles.profileName, { textAlign: 'center' }]}>받은 요청</Text>
            
            <FlatList 
              data={friendRequests}
              keyExtractor={(item) => item.id}
              renderItem={({item}) => (
                <View style={{
                  flexDirection: 'row', 
                  alignItems: 'center', 
                  marginBottom: 15, 
                  width: '100%',
                  paddingHorizontal: 5
                }}>
                  <Image 
                    source={{ uri: item.image || `https://i.pravatar.cc/150?u=${item.uuid}` }} 
                    style={{width: 45, height: 45, borderRadius: 22.5, marginRight: 12}} 
                  />
                  <View style={{flex: 1, justifyContent: 'center', marginRight: 10}}>
                    <Text 
                      style={{fontSize: 16, fontWeight: 'bold', color: '#5C4A3A'}}
                      numberOfLines={1}
                    >
                      {item.nickname || item.name || '알 수 없는 사용자'}
                    </Text>
                  </View>
                  <View style={{flexDirection: 'row', gap: 5}}>
                    <TouchableOpacity 
                      onPress={() => handleAcceptRequest(item)} 
                      style={{backgroundColor: theme.colors.primary, paddingHorizontal: 12, paddingVertical: 8, borderRadius: 6}}
                    >
                      <Text style={{color: '#fff', fontSize: 12, fontWeight: 'bold'}}>수락</Text>
                    </TouchableOpacity>
                    <TouchableOpacity 
                      onPress={() => handleRejectRequest(item)} 
                      style={{backgroundColor: '#E5E5E5', paddingHorizontal: 12, paddingVertical: 8, borderRadius: 6}}
                    >
                      <Text style={{color: '#5C4A3A', fontSize: 12, fontWeight: 'bold'}}>거절</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              )}
              ListEmptyComponent={
                <Text style={{textAlign: 'center', color: '#999', marginTop: 20}}>
                  새로운 요청이 없습니다.
                </Text>
              }
            />
          </TouchableOpacity>
        </TouchableOpacity>
      </Modal>

    </SafeAreaView>
  );
};

export default ChatScreen;

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#FFF8F0' },
  flexOne: { flex: 1 },
  header: { padding: 20, borderBottomWidth: 1, borderBottomColor: '#eee' },
  headerTitle: { fontSize: 24, fontWeight: '800' },
  tabContainer: { flexDirection: 'row', borderBottomWidth: 1, borderBottomColor: '#eee' },
  tab: { flex: 1, padding: 15, alignItems: 'center' },
  activeTab: { borderBottomWidth: 2, borderBottomColor: theme.colors.primary },
  tabText: { fontSize: 16, color: '#888' },
  activeTabText: { color: '#000', fontWeight: '700' },
  friendButtonContainer: { flexDirection: 'row', padding: 10, gap: 10 },
  friendActionButton: { flex: 1, padding: 12, borderWidth: 1, borderColor: theme.colors.primary, borderRadius: 8, alignItems: 'center' },
  friendActionText: { fontSize: 14, fontWeight: '600', color: theme.colors.primary },
  friendActionIcon: { fontSize: 16, marginRight: 6, color: theme.colors.primary },
  listContent: { paddingBottom: 100, backgroundColor: '#FFF8F0' },
  separator: { height: 1, backgroundColor: '#eee' },
  avatar: { width: 50, height: 50, borderRadius: 25 },
  friendItem: { flexDirection: 'row', padding: 15, alignItems: 'center' },
  friendInfo: { marginLeft: 15 },
  friendName: { fontSize: 16, fontWeight: '600' },
  friendStatus: { fontSize: 14, color: '#666' },
  chatHeader: { flexDirection: 'row', alignItems: 'center', padding: 10, borderBottomWidth: 1, borderBottomColor: '#eee' },
  chatHeaderImage: { width: 40, height: 40, borderRadius: 20 },
  chatHeaderTitle: { flex: 1, marginLeft: 10, fontSize: 18, fontWeight: '700' },
  backButton: { padding: 10 },
  backIcon: { fontSize: 30 },
  messagesList: { padding: 15 },
  messageRow: { flexDirection: 'row', marginBottom: 10 },
  myMessageRow: { justifyContent: 'flex-end' },
  otherMessageRow: { justifyContent: 'flex-start' },
  messageBubble: { padding: 12, borderRadius: 15, maxWidth: '80%' },
  myMessageBubble: { backgroundColor: theme.colors.primary },
  otherMessageBubble: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#eee' },
  myMessageText: { color: '#fff' },
  otherMessageText: { color: '#000' },
  inputContainer: { flexDirection: 'row', padding: 10, borderTopWidth: 1, borderTopColor: '#eee', alignItems: 'center' },
  messageInput: { flex: 1, backgroundColor: '#fff', borderRadius: 20, paddingHorizontal: 15, maxHeight: 100 },
  sendButton: { marginLeft: 10, width: 40, height: 40, borderRadius: 20, backgroundColor: theme.colors.primary, justifyContent: 'center', alignItems: 'center' },
  sendButtonText: { color: '#fff', fontSize: 18 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' },
  profileModalContainer: { 
    backgroundColor: '#fff', 
    padding: 30, 
    paddingBottom: 40,             // 하단 여유 공간
    borderTopLeftRadius: 25, 
    borderTopRightRadius: 25, 
    alignItems: 'stretch',         // ★ center에서 stretch로 변경 (매우 중요)
  },
  profileAvatar: { width: 100, height: 100, borderRadius: 50, marginBottom: 15 },
  profileName: { fontSize: 20, fontWeight: '700', marginBottom: 20 },
  profileActions: { flexDirection: 'row', gap: 10, width: '100%' },
  profileButton: { flex: 1, padding: 15, borderRadius: 10, backgroundColor: theme.colors.primary, alignItems: 'center' },
  deleteButton: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#eee' },
  timestamp: { textAlign: 'center', color: '#aaa', fontSize: 12, marginVertical: 10 },
  headerRight: { width: 40 },
  messageAvatar: { width: 32, height: 32, borderRadius: 16, marginRight: 8, backgroundColor: '#ddd' },
  // ★ 새로 추가된 스타일
  senderName: {
    fontSize: 12,
    color: '#8D6E63',
    marginLeft: 50,
    marginBottom: 4,
    fontWeight: '600',
    fontFamily: Platform.OS === 'android' ? 'sans-serif' : 'System',
  },
});