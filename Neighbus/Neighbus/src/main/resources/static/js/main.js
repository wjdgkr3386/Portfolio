var stompClient = null;

// 1. 뱃지 숫자 갱신 함수 (브라운 테마 적용)
function updateBadgeCount() {
    fetch('/api/notifications/count')
        .then(res => res.text())
        .then(count => {
            const badge = document.getElementById('alarmBadge');
            if (badge) {
                if (parseInt(count) > 0) {
                    badge.style.display = 'inline-block';
                    badge.innerText = count;
                    // 배지 색상을 브라운 테마의 강조색(빨간색 또는 진한 갈색)으로 유지하거나 
                    // CSS에서 정의한 클래스를 따릅니다.
                } else {
                    badge.style.display = 'none';
                }
            }
        })
        .catch(err => console.error("뱃지 업데이트 실패", err));
}

// 2. 웹소켓 연결 함수
function connect() {
    var socket = new SockJS('/ws-stomp');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // 콘솔 로그 깔끔하게 정리

    stompClient.connect({}, function (frame) {
        updateBadgeCount(); 

        stompClient.subscribe('/user/queue/notifications', function (message) {
            updateBadgeCount(); 
            
            var modal = document.getElementById('notificationModal');
            if (modal && modal.classList.contains('show')) {
                openNotificationModal(); 
            }
        });
    });
}

// 3. 알림 모달 열기 및 목록 로드 (브라운 디자인 적용)
function openNotificationModal() {
    var modalElement = document.getElementById('notificationModal');
    if (!modalElement) return;
    
    var myModal = bootstrap.Modal.getInstance(modalElement) || new bootstrap.Modal(modalElement);
    
    fetch('/api/notifications')
        .then(response => response.json())
        .then(data => {
            const listArea = document.getElementById('notificationList');
            listArea.innerHTML = ""; 

            if (data.length === 0) {
                listArea.innerHTML = '<li class="list-group-item text-center py-4 text-muted">새로운 알림이 없습니다.</li>';
            } else {
                data.forEach(noti => {
                    // [수정] 브라운 테마 배경색 설정
                    // 안읽음: 따뜻한 베이지(#FFF5EB), 읽음: 밝은 회색베이지
                    let bgStyle = noti.isRead == 1 ? "background-color: #FAF0E6; color: #8D6E63;" : "background-color: #FFFBF7; font-weight: 700;";
                    let typeColor = "#A67C52"; // 브라운 메인 컬러
                    
                    let dateStr = new Date(noti.createdAt).toLocaleString();

                    // [수정] 알림 아이템 디자인: 텍스트 색상을 브라운 계열(#5D4037)로 변경
                    let item = `
                        <li class="list-group-item" style="${bgStyle} border-color: #E8D7C3; padding: 12px 15px;">
                            <a href="#" onclick="readAndDelete(${noti.id}, '${noti.url}'); return false;" class="text-decoration-none d-block">
                                <small style="color: ${typeColor}; font-weight: 800; font-size: 0.75rem;">[${noti.notificationType}]</small>
                                <div class="mb-1" style="color: #5D4037; font-size: 0.9rem; margin-top: 3px;">${noti.content}</div>
                                <small style="color: #8D6E63; font-size: 0.75rem;">
                                    ${dateStr}
                                </small>
                            </a>
                        </li>
                    `;
                    listArea.innerHTML += item;
                });
            }
            myModal.show();
        })
        .catch(error => console.error('Error:', error));
}

// 4. 알림 클릭 시 처리
function readAndDelete(notiId, targetUrl) {
    fetch('/api/notifications/' + notiId, {
        method: 'DELETE',
    })
    .then(() => {
        window.location.href = targetUrl;
    })
    .catch(error => {
        console.error('Error:', error);
        window.location.href = targetUrl;
    });
}

// 5. 알림 전체 삭제 함수 (새로 추가됨!)
function deleteAllNotifications() {
    if (!confirm("모든 알림을 삭제하시겠습니까?")) return;

    fetch('/api/notifications/deleteAll', {
        method: 'DELETE',
    })
    .then(response => response.text())
    .then(data => {
        if (data === "deleted all") {
            // UI 즉시 갱신: 리스트 비우고 '없음' 메시지 표시
            const listArea = document.getElementById('notificationList');
            if (listArea) {
                listArea.innerHTML = '<li class="list-group-item text-center">새로운 알림이 없습니다.</li>';
            }
            // 뱃지 숫자 갱신 (0으로)
            updateBadgeCount();
        } else {
            alert("삭제에 실패했습니다.");
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert("오류가 발생했습니다.");
    });
}

// 4. 페이지 로드 시 웹소켓 자동 연결
document.addEventListener("DOMContentLoaded", function() {
    connect();
});