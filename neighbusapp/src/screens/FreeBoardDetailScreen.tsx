// src/screens/FreeBoardDetailScreen.tsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  KeyboardAvoidingView,
  Platform,
  Image,
  ActivityIndicator,
  Alert,
  useWindowDimensions,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import RenderHtml from 'react-native-render-html';
import type { RootStackScreenProps } from '../../App';
import theme from '../styles/theme';
import { useUser } from '../context/UserContext';
import { BASE_URL } from '../config';

type Props = RootStackScreenProps<'FreeBoardDetail'>;

type PostDetail = {
  id: string;
  category: string;
  title: string;
  author: string;
  avatarUrl: string;
  date: string;
  content: string;
  views: number;
  likes: number;
};

type Comment = {
  id: string;
  author: string;
  avatarUrl: string;
  text: string;
  date: string;
  writerId: number;
};

const FreeBoardDetailScreen: React.FC<Props> = ({ route, navigation }) => {
  const { postId } = route.params;
  const { token } = useUser();
  const { width } = useWindowDimensions();
  
  const [postData, setPostData] = useState<PostDetail | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [loading, setLoading] = useState(true);
  const [currentUserId, setCurrentUserId] = useState<number>(0);
  const [reactionInfo, setReactionInfo] = useState<{ 
    likeCount: number; 
    dislikeCount: number; 
    userReaction: number | null 
  }>({ 
    likeCount: 0, 
    dislikeCount: 0,
    userReaction: null 
  });

  const fetchPostDetail = useCallback(async () => {
    // setLoading(true); // Don't reset loading on every fetch to avoid flicker if just refreshing comments
    try {
      const response = await fetch(`${BASE_URL}/api/mobile/freeboard/${postId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await response.json();

      if (data.success) {
        const { post, reaction, comments: fetchedComments, currentUserId: fetchedUserId } = data;

        setCurrentUserId(fetchedUserId);

        setPostData({
          id: post.id.toString(),
          category: post.clubName || '자유',
          title: post.title,
          author: post.writerNickname || post.writerUsername || '익명',
          avatarUrl: post.image || `https://ui-avatars.com/api/?name=${encodeURIComponent(post.writerNickname || 'U')}&background=random`,
          date: post.createdAt ? post.createdAt.replace('T', ' ').substring(0, 16) : '',
          content: post.content,
          views: post.viewCount,
          likes: reaction.likeCount || 0,
        });

        setReactionInfo({
          likeCount: reaction.likeCount || 0,
          dislikeCount: reaction.dislikeCount || 0,
          userReaction: reaction.userReaction,
        });

        if (Array.isArray(fetchedComments)) {
          const mappedComments = fetchedComments.map((c: any) => ({
            id: c.id?.toString() || Math.random().toString(),
            author: c.nickname || c.writerUsername || '익명',
            avatarUrl: c.image || `https://ui-avatars.com/api/?name=${encodeURIComponent(c.nickname || 'U')}&background=random`,
            text: c.content,
            date: c.createdAt ? c.createdAt.replace('T', ' ').substring(0, 16) : '',
            writerId: c.writer,
          }));
          setComments(mappedComments);
        }
      } else {
        Alert.alert('오류', data.message || '게시글을 불러올 수 없습니다.');
        navigation.goBack();
      }
    } catch (error) {
      console.error('Fetch detail error:', error);
      Alert.alert('오류', '네트워크 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [postId, token, navigation]);

  useEffect(() => {
    setLoading(true);
    fetchPostDetail();
  }, [fetchPostDetail]);

  const toggleReaction = async (type: 1 | 2) => {
    if (!currentUserId) {
      Alert.alert('알림', '로그인이 필요합니다.');
      return;
    }

    const previousReaction = reactionInfo.userReaction;
    const previousLikeCount = reactionInfo.likeCount;
    const previousDislikeCount = reactionInfo.dislikeCount;
    
    // Optimistic Update 로직
    let newLikeCount = previousLikeCount;
    let newDislikeCount = previousDislikeCount;
    let newUserReaction: number | null = type;

    if (previousReaction === type) {
      // 취소 (Delete)
      newUserReaction = null;
      if (type === 1) newLikeCount--;
      else newDislikeCount--;
    } else {
      // 새로운 반응 또는 변경
      if (type === 1) {
        newLikeCount++;
        if (previousReaction === 2) newDislikeCount--;
      } else {
        newDislikeCount++;
        if (previousReaction === 1) newLikeCount--;
      }
    }

    setReactionInfo({
      likeCount: newLikeCount,
      dislikeCount: newDislikeCount,
      userReaction: newUserReaction,
    });

    try {
      let url = '';
      let method = '';
      const body: any = { freeboardId: parseInt(postId, 10) };

      if (previousReaction === type) {
        // 삭제
        url = `${BASE_URL}/api/mobile/freeboard/reaction/delete`;
        method = 'DELETE';
      } else if (previousReaction) {
        // 수정 (1->2 or 2->1)
        url = `${BASE_URL}/api/mobile/freeboard/reaction/update`;
        method = 'PUT';
        body.reactionType = type;
      } else {
        // 등록
        url = `${BASE_URL}/api/mobile/freeboard/reaction/insert`;
        method = 'POST';
        body.reactionType = type;
      }

      const response = await fetch(url, {
        method: method,
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      });

      const data = await response.json();

      if (data.likeCount !== undefined) {
          setReactionInfo({
              likeCount: data.likeCount,
              dislikeCount: data.dislikeCount || 0,
              userReaction: data.userReaction
          });
      }

    } catch (error) {
      console.error('Reaction error:', error);
      setReactionInfo({
        likeCount: previousLikeCount,
        dislikeCount: previousDislikeCount,
        userReaction: previousReaction,
      });
      Alert.alert('오류', '반응 처리 중 오류가 발생했습니다.');
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim()) {
      return;
    }

    try {
      const response = await fetch(`${BASE_URL}/api/mobile/freeboard/comment`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          freeboard: parseInt(postId, 10),
          content: newComment,
          parent: 0,
        }),
      });

      const data = await response.json();

      if (data.success) {
        setNewComment('');
        fetchPostDetail(); // Refresh comments
      } else {
        Alert.alert('실패', data.message || '댓글 등록에 실패했습니다.');
      }
    } catch (error) {
      console.error('Add comment error:', error);
      Alert.alert('오류', '서버 통신 중 오류가 발생했습니다.');
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    Alert.alert('댓글 삭제', '정말 삭제하시겠습니까?', [
      { text: '취소', style: 'cancel' },
      {
        text: '삭제',
        style: 'destructive',
        onPress: async () => {
          try {
            const response = await fetch(`${BASE_URL}/api/mobile/freeboard/comment/${commentId}`, {
              method: 'DELETE',
              headers: {
                Authorization: `Bearer ${token}`,
              },
            });
            const data = await response.json();
            if (data.success) {
              fetchPostDetail();
            } else {
              Alert.alert('실패', data.message);
            }
          } catch (error) {
            console.error('Delete comment error:', error);
            Alert.alert('오류', '삭제 중 오류가 발생했습니다.');
          }
        },
      },
    ]);
  };

  if (loading) {
    return (
      <View style={[styles.safeArea, { justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color={theme.colors.primary} />
      </View>
    );
  }

  if (!postData) {
    return null; 
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
      >
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Text style={styles.headerButtonText}>←</Text>
          </TouchableOpacity>
          <Text style={styles.headerTitle}>자유게시판</Text>
          <TouchableOpacity>
            <Text style={styles.headerButtonText}>︙</Text>
          </TouchableOpacity>
        </View>

        <ScrollView style={styles.container}>
          <View style={styles.contentWrapper}>
            {/* Author Info */}
            <View style={styles.authorInfo}>
              <Image source={{ uri: postData.avatarUrl }} style={styles.authorAvatar} />
              <View>
                <Text style={styles.authorName}>{postData.author}</Text>
                <Text style={styles.postDate}>{postData.date} · 조회 {postData.views}</Text>
              </View>
            </View>

            {/* Title */}
            <Text style={styles.postTitle}>{postData.title}</Text>
            
            {/* HTML Content Rendering */}
            <RenderHtml
              contentWidth={width - 40}
              source={{ html: postData.content }}
              ignoredStyles={['width', 'height', 'lineHeight']} // 인라인 스타일로 인한 짤림 방지
              tagsStyles={{
                body: {
                  fontSize: 16,
                  lineHeight: 26,
                  color: theme.colors.textSecondary,
                },
                img: {
                  borderRadius: 8,
                  marginVertical: 10,
                },
                hr: {
                  borderTopWidth: 1,
                  borderTopColor: theme.colors.borderColor,
                  marginVertical: 15,
                }
              }}
            />
          </View>

          {/* Action Bar */}
          <View style={styles.actionBar}>
            <TouchableOpacity style={styles.actionButton} onPress={() => toggleReaction(1)}>
              <Text style={styles.actionIcon}>{reactionInfo.userReaction === 1 ? '❤️' : '🤍'}</Text>
              <Text style={styles.actionText}>좋아요 {reactionInfo.likeCount}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionButton} onPress={() => toggleReaction(2)}>
              <Text style={styles.actionIcon}>{reactionInfo.userReaction === 2 ? '👎' : '👎🏻'}</Text>
              <Text style={styles.actionText}>싫어요 {reactionInfo.dislikeCount}</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.actionButton}>
              <Text style={styles.actionIcon}>💬</Text>
              <Text style={styles.actionText}>댓글 {comments.length}</Text>
            </TouchableOpacity>
          </View>

          {/* Comments Section */}
          <View style={styles.commentsSection}>
            {comments.map((comment) => (
              <View key={comment.id} style={styles.comment}>
                <Image source={{ uri: comment.avatarUrl }} style={styles.commentAvatar} />
                <View style={styles.commentBody}>
                  <Text style={styles.commentAuthor}>{comment.author}</Text>
                  <Text style={styles.commentText}>{comment.text}</Text>
                  <Text style={styles.commentDate}>{comment.date}</Text>
                </View>
                {comment.writerId === currentUserId && (
                  <TouchableOpacity
                    style={styles.deleteButton}
                    onPress={() => handleDeleteComment(comment.id)}
                  >
                    <Text style={styles.deleteButtonText}>✕</Text>
                  </TouchableOpacity>
                )}
              </View>
            ))}
            {comments.length === 0 && (
              <View style={{ padding: 40, alignItems: 'center' }}>
                <Text style={{ color: theme.colors.textLight }}>첫 댓글을 남겨보세요.</Text>
              </View>
            )}
          </View>
        </ScrollView>

        {/* Comment Input */}
        <View style={styles.commentInputContainer}>
          <TextInput
            style={styles.commentInput}
            placeholder="댓글을 입력하세요..."
            placeholderTextColor={theme.colors.textLight}
            value={newComment}
            onChangeText={setNewComment}
            multiline={false}
          />
          <TouchableOpacity style={styles.sendButton} onPress={handleAddComment}>
            <Text style={styles.sendButtonText}>등록</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: theme.colors.cardBg,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.borderColor,
  },
  headerButtonText: {
    fontSize: 24,
    color: theme.colors.textPrimary,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: theme.colors.textPrimary,
  },
  container: {
    flex: 1,
    backgroundColor: theme.colors.cardBg,
  },
  contentWrapper: {
    padding: 20,
  },
  authorInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  authorAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    marginRight: 12,
    backgroundColor: theme.colors.borderColor,
  },
  authorName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: theme.colors.textPrimary,
  },
  postDate: {
    fontSize: 13,
    color: theme.colors.textLight,
    marginTop: 2,
  },
  postTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: theme.colors.textPrimary,
    marginBottom: 20,
    lineHeight: 32,
  },
  actionBar: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderTopWidth: 1,
    borderTopColor: theme.colors.borderColor,
    backgroundColor: theme.colors.bodyBg,
    gap: 24,
  },
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  actionIcon: {
    fontSize: 16,
  },
  actionText: {
    fontSize: 14,
    color: theme.colors.textSecondary,
    fontWeight: '500',
  },
  commentsSection: {
    paddingTop: 12,
    backgroundColor: theme.colors.cardBg,
  },
  comment: {
    flexDirection: 'row',
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderTopWidth: 1,
    borderTopColor: theme.colors.borderColor,
  },
  commentAvatar: {
    width: 36,
    height: 36,
    borderRadius: 18,
    marginRight: 12,
    backgroundColor: theme.colors.borderColor,
  },
  commentBody: {
    flex: 1,
  },
  commentAuthor: {
    fontSize: 15,
    fontWeight: 'bold',
    color: theme.colors.textPrimary,
  },
  commentText: {
    fontSize: 15,
    color: theme.colors.textSecondary,
    marginTop: 4,
    lineHeight: 22,
  },
  commentDate: {
    fontSize: 12,
    color: theme.colors.textLight,
    marginTop: 6,
  },
  commentInputContainer: {
    flexDirection: 'row',
    padding: 12,
    borderTopWidth: 1,
    borderTopColor: theme.colors.borderColor,
    backgroundColor: theme.colors.cardBg,
  },
  commentInput: {
    flex: 1,
    backgroundColor: theme.colors.bodyBg,
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 15,
    marginRight: 8,
    color: theme.colors.textPrimary,
  },
  sendButton: {
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 8,
  },
  sendButtonText: {
    fontSize: 16,
    color: theme.colors.primary,
    fontWeight: '600',
  },
  deleteButton: {
    padding: 8,
    justifyContent: 'center',
  },
  deleteButtonText: {
    fontSize: 14,
    color: theme.colors.textLight,
  },
});

export default FreeBoardDetailScreen;