// 커스텀 모달 함수
function showModal(type, title, message, callback) {
    // 모달이 아직 없으면 생성
    let overlay = document.getElementById('customModalOverlay');
    if (!overlay) {
        createModalHTML();
        overlay = document.getElementById('customModalOverlay');
    }

    const icon = document.getElementById('modalIcon');
    const titleEl = document.getElementById('modalTitle');
    const messageEl = document.getElementById('modalMessage');
    const buttons = document.getElementById('modalButtons');

    // 아이콘 설정
    icon.className = 'modal-icon ' + (type === 'prompt' ? 'confirm' : type); // prompt는 confirm과 같은 아이콘 사용
    const icons = {
        'success': '✓',
        'error': '✕',
        'warning': '!',
        'confirm': '?',
        'prompt': '?' 
    };
    icon.textContent = icons[type] || '✓';

    // 제목 설정
    titleEl.textContent = title;
    
    // 메시지 및 입력창 설정 (prompt일 경우 input 추가)
    if (type === 'prompt') {
        messageEl.innerHTML = `
            <div>${message}</div>
            <input type="text" id="modalInput" class="form-control mt-3" style="width:100%; padding:8px; margin-top:10px; border:1px solid #ddd; border-radius:4px;" placeholder="입력하세요">
        `;
    } else {
        messageEl.textContent = message;
    }

    // 버튼 설정
    if (type === 'confirm') {
        buttons.innerHTML = `
            <button class="modal-btn secondary" onclick="closeModal()">취소</button>
            <button class="modal-btn primary" onclick="confirmAction()">확인</button>
        `;
        window.confirmCallback = callback;
    } else if (type === 'prompt') {
        // Prompt일 때는 입력값을 넘겨주는 함수(confirmPrompt) 호출
        buttons.innerHTML = `
            <button class="modal-btn secondary" onclick="closeModal()">취소</button>
            <button class="modal-btn primary" onclick="confirmPrompt()">확인</button>
        `;
        window.promptCallback = callback;
    } else {
        buttons.innerHTML = `
            <button class="modal-btn primary" onclick="closeModal(${callback ? 'true' : ''})">확인</button>
        `;
        if (callback) {
            window.modalCallback = callback;
        }
    }

    overlay.classList.add('active');
    
    // prompt일 경우 input에 자동 포커스
    if(type === 'prompt') {
        setTimeout(() => document.getElementById('modalInput').focus(), 100);
    }
}

function closeModal(executeCallback) {
    const overlay = document.getElementById('customModalOverlay');
    if (overlay) {
        overlay.classList.remove('active');
    }
    if (executeCallback && window.modalCallback) {
        window.modalCallback();
        window.modalCallback = null;
    }
}

function confirmAction() {
    closeModal();
    if (window.confirmCallback) {
        window.confirmCallback();
        window.confirmCallback = null;
    }
}

// Prompt용 확인 함수 추가 (입력값 전달)
function confirmPrompt() {
    const inputVal = document.getElementById('modalInput').value;
    closeModal();
    if (window.promptCallback) {
        window.promptCallback(inputVal); // 입력값을 콜백으로 전달
        window.promptCallback = null;
    }
}

// 모달 HTML 생성 함수 (기존과 동일)
function createModalHTML() {
    // 이미 존재하면 생성하지 않음
    if (document.getElementById('customModalOverlay')) return;

    const modalHTML = `
        <div id="customModalOverlay" class="custom-modal-overlay" onclick="if(event.target === this) closeModal()">
            <div class="custom-modal">
                <div id="modalIcon" class="modal-icon success">✓</div>
                <div id="modalTitle" class="modal-title">알림</div>
                <div id="modalMessage" class="modal-message">메시지</div>
                <div id="modalButtons" class="modal-buttons">
                    <button class="modal-btn primary" onclick="closeModal()">확인</button>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 페이지 로드 시 모달 HTML 생성
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', createModalHTML);
} else {
    createModalHTML();
}