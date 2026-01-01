import React, { useState, useEffect, useCallback } from 'react';
import { Alert } from 'react-native';
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
  Button,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import type { RootStackScreenProps } from '../../App';
import theme from '../styles/theme';
import { BASE_URL } from '../config';
import { useUser } from '../context/UserContext';

type Props = RootStackScreenProps<'GalleryDetail'>;

type CommentType = {
  ID: number;
  WRITER: string;
  CONTENT: string;
  CREATED_AT: string;
  avatarUrl?: string; // Will use WRITER_IMAGE for display
  WRITER_IMAGE?: string; // Actual image path for the commenter, if available
  REPLIES?: CommentType[]; // Nested replies
};

type GalleryPost = {
  ID: number;
  TITLE: string;
  WRITER: string;
  CREATED_AT: string;
  CONTENT: string;
  IMAGES: { ID: number; IMG: string; GALLERY: number }[];
  VIEW_COUNT: number;
  likes: number; // Assuming this comes from reaction data
  avatarUrl?: string; // Will use WRITER_IMAGE for display
  WRITER_IMAGE?: string; // Actual image path for the post author
  COMMENTS?: CommentType[];
};

const GalleryDetailScreen: React.FC<Props> = ({ route, navigation }) => {
  const { postId } = route.params;
  const { token, user } = useUser();

  const [galleryPost, setGalleryPost] = useState<GalleryPost | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newCommentText, setNewCommentText] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [reactionInfo, setReactionInfo] = useState<{ 
    likeCount: number; 
    dislikeCount: number; 
    userReaction: number | null 
  }>({ 
    likeCount: 0, 
    dislikeCount: 0,
    userReaction: null 
  });

  const fetchGalleryDetail = useCallback(async () => {
    // We don't want to show the main loading spinner for a refresh
    if (!galleryPost) {
      setLoading(true);
    }
    setError(null);
    if (!token) {
      setError('로그인이 필요한 서비스입니다.');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${BASE_URL}/api/mobile/gallery/detail/${postId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.status === 401) {
        setError('로그인 세션이 만료되었습니다. 다시 로그인해주세요.');
        return;
      }
      if (!response.ok) {
        throw new Error('갤러리 상세 정보를 불러오는데 실패했습니다.');
      }

      const responseData = await response.json();

      if (responseData.status === 'success' && responseData.galleryMap) {
        const gm = responseData.galleryMap;
        const reaction = responseData.reaction;

        // Clean &nbsp; from title and content
        const titleCleaned = (gm.TITLE || '').replace(/&nbsp;/g, ' ');
        const contentCleaned = (gm.CONTENT || '').replace(/&nbsp;/g, ' ');

        // Map comments and use placeholder for avatar if not provided by backend
        const mappedComments: CommentType[] = (gm.COMMENTS || []).map((comment: any) => ({
          ID: comment.ID,
          WRITER: comment.WRITER,
          CONTENT: comment.CONTENT,
          CREATED_AT: comment.CREATED_AT,
          avatarUrl: comment.WRITER_IMAGE || 'https://i.pravatar.cc/150?u=comment-user', // Use WRITER_IMAGE if backend provides, else placeholder
          // REPLIES are not currently handled here, but the type allows it
        }));

        setGalleryPost({
          ID: gm.ID,
          TITLE: titleCleaned,
          WRITER: gm.WRITER,
          CREATED_AT: gm.CREATED_AT,
          CONTENT: contentCleaned,
          IMAGES: gm.IMAGES || [],
          VIEW_COUNT: gm.VIEW_COUNT,
          likes: reaction?.likeCount || 0, // Keep for backward compatibility if needed, but rely on reactionInfo
          avatarUrl: gm.WRITER_IMAGE || 'https://i.pravatar.cc/150?u=post-author', // Use WRITER_IMAGE for post author, else placeholder
          WRITER_IMAGE: gm.WRITER_IMAGE, // Store actual image path
          COMMENTS: mappedComments, // Use mapped comments
        });

        setReactionInfo({
          likeCount: reaction?.likeCount || 0,
          dislikeCount: reaction?.dislikeCount || 0,
          userReaction: reaction?.userReaction,
        });
      } else {
        setError(responseData.message || '게시글을 찾을 수 없습니다.');
      }
    } catch (err: any) {
      setError(err.message || '네트워크 오류가 발생했습니다.');
      console.error('Error fetching gallery detail:', err);
    } finally {
      setLoading(false);
    }
  }, [postId, token, galleryPost]);

  useEffect(() => {
    fetchGalleryDetail();
  }, [postId, token]); 

  const toggleReaction = async (type: 1 | 2) => {
    if (!token) {
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
      const body: any = { galleryId: parseInt(postId, 10) }; // Use galleryId for Gallery API

      if (previousReaction === type) {
        url = `${BASE_URL}/api/mobile/gallery/reaction/delete`;
        method = 'DELETE';
      } else if (previousReaction) {
        url = `${BASE_URL}/api/mobile/gallery/reaction/update`;
        method = 'PUT';
        body.reactionType = type;
      } else {
        url = `${BASE_URL}/api/mobile/gallery/reaction/insert`;
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
    if (!newCommentText.trim() || !galleryPost || !user) {
      return;
    }
    
    setIsSubmittingComment(true);

    const formData = new FormData();
    formData.append('comment', newCommentText);

    try {
      const response = await fetch(`${BASE_URL}/api/mobile/gallery/insertComment/${galleryPost.ID}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      const responseData = await response.json();

      if (response.ok && responseData.success) {
        setNewCommentText('');
        // Optimistically update the UI with the new comment's image
        const newComment: CommentType = {
          ID: Date.now(), // Temporary ID for optimistic update
          WRITER: user.nickname || user.username || '익명', // Use user's nickname or username
          CONTENT: newCommentText,
          CREATED_AT: new Date().toISOString(),
          avatarUrl: user.image || 'https://i.pravatar.cc/150?u=current-user', // Use user's actual image if available
        };

        setGalleryPost(prevPost => {
          if (!prevPost) return null;
          return {
            ...prevPost,
            COMMENTS: [...(prevPost.COMMENTS || []), newComment],
          };
        });
        // await fetchGalleryDetail(); // Refetch to get the latest comments and confirmed ID
      } else {
        Alert.alert('오류', responseData.message || '댓글 등록에 실패했습니다.');
      }
    } catch (error) {
      Alert.alert('오류', '댓글 등록 중 오류가 발생했습니다.');
      console.error('Error adding comment:', error);
    } finally {
      setIsSubmittingComment(false);
    }
  };

  const renderContent = () => {
    if (loading) {
      return (
        <View style={styles.messageContainer}>
          <ActivityIndicator size="large" color={theme.colors.primary} />
          <Text style={styles.messageText}>상세 정보 로딩 중...</Text>
        </View>
      );
    }

    if (error) {
      return (
        <View style={styles.messageContainer}>
          <Text style={styles.messageText}>{error}</Text>
          {token && <Button title="다시 시도" onPress={fetchGalleryDetail} color={theme.colors.primary} />}
        </View>
      );
    }

    if (!galleryPost) {
      return (
        <View style={styles.messageContainer}>
          <Text style={styles.messageText}>게시글을 찾을 수 없습니다.</Text>
        </View>
      );
    }

    return (
      <ScrollView style={styles.container}>
        {/* Post Header */}
        <View style={styles.contentWrapper}>
          <View style={styles.authorInfo}>
            <Image source={{ uri: galleryPost.avatarUrl }} style={styles.authorAvatar} />
            <View>
              <Text style={styles.authorName}>{galleryPost.WRITER}</Text>
              <Text style={styles.postDate}>
                {new Date(galleryPost.CREATED_AT).toLocaleDateString()} · 조회 {galleryPost.VIEW_COUNT}
              </Text>
            </View>
          </View>
          <Text style={styles.postTitle}>{galleryPost.TITLE}</Text>
        </View>

        {/* Images */}
        {galleryPost.IMAGES.map((imgObj) => (
          <Image 
            key={imgObj.ID} 
            source={{ uri: imgObj.IMG }} 
            style={styles.postImage} 
          />
        ))}

        {/* Post Content */}
        <View style={styles.contentWrapper}>
          <Text style={styles.postContent}>{galleryPost.CONTENT}</Text>
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
            <Text style={styles.actionText}>댓글 {galleryPost.COMMENTS?.length || 0}</Text>
          </TouchableOpacity>
        </View>

        {/* Comments Section */}
        <View style={styles.commentsSection}>
          {galleryPost.COMMENTS?.map((comment) => (
            <View key={comment.ID} style={styles.comment}>
              <Image source={{ uri: comment.avatarUrl || 'https://i.pravatar.cc/150?u=comment-user' }} style={styles.commentAvatar} />
              <View style={styles.commentBody}>
                <Text style={styles.commentAuthor}>{comment.WRITER}</Text>
                <Text style={styles.commentText}>{comment.CONTENT}</Text>
                <Text style={styles.commentDate}>{new Date(comment.CREATED_AT).toLocaleString()}</Text>
              </View>
            </View>
          ))}
        </View>
      </ScrollView>
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={100}
      >
        <View style={styles.header}>
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <Text style={styles.headerButtonText}>←</Text>
          </TouchableOpacity>
          <Text style={styles.headerTitle}>갤러리</Text>
          <TouchableOpacity>
            <Text style={styles.headerButtonText}>︙</Text>
          </TouchableOpacity>
        </View>

        {renderContent()}

        {/* Comment Input */}
        <View style={styles.commentInputContainer}>
          <TextInput
            style={styles.commentInput}
            placeholder="댓글을 입력하세요..."
            placeholderTextColor={theme.colors.textLight}
            value={newCommentText}
            onChangeText={setNewCommentText}
            editable={!!token && !isSubmittingComment} // Disable if not logged in or submitting
          />
          <TouchableOpacity 
            style={styles.sendButton} 
            onPress={handleAddComment} 
            disabled={!token || !newCommentText.trim() || isSubmittingComment}
          >
            {isSubmittingComment ? (
              <ActivityIndicator color={theme.colors.primary} />
            ) : (
              <Text style={styles.sendButtonText}>등록</Text>
            )}
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
    marginBottom: 0,
    lineHeight: 32,
  },
  postImage: {
    width: '100%',
    height: 450, // 높이를 키워서 더 크게 보이게 함
    resizeMode: 'contain', // 이미지가 잘리지 않게 조정
    backgroundColor: '#F0F0F0', // 이미지가 비는 공간을 채울 배경색
    marginBottom: 10,
  },
  postContent: {
    fontSize: 16,
    lineHeight: 26,
    color: theme.colors.textSecondary,
    marginTop: 20,
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
  messageContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  messageText: {
    fontSize: 16,
    color: theme.colors.textSecondary,
    textAlign: 'center',
    marginBottom: 10,
  },
});

export default GalleryDetailScreen;