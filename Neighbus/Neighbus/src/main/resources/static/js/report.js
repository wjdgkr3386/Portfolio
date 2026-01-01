document.addEventListener('DOMContentLoaded', () => {
    const reportModalElement = document.getElementById('reportModal');
    if (!reportModalElement) {
        // On pages without the modal, do nothing.
        return;
    }

    const reportModalInstance = new bootstrap.Modal(reportModalElement);
    const reportForm = document.getElementById('reportForm');
    const reportTypeInput = document.getElementById('report-type');
    const reportTargetIdInput = document.getElementById('report-target-id');
    const reportCategoryInput = document.getElementById('report-category');
    const reportReasonInput = document.getElementById('report-reason');

    const getReporterId = () => {
        const reporterInput = document.getElementById('report-reporter-id');
        if (reporterInput) return reporterInput.value;

        const guestReporterInput = document.getElementById('report-reporter-id-guest');
        if (guestReporterInput) return guestReporterInput.value;

        return '0'; // Default to guest
    };

    // Use event delegation to handle all report button clicks
    document.body.addEventListener('click', function(event) {
        // Find the closest ancestor that is a report button
        const reportButton = event.target.closest('[data-bs-toggle="modal"][data-bs-target="#reportModal"]');

        if (reportButton) {
            event.preventDefault();
            const type = reportButton.dataset.reportType;
            const targetId = reportButton.dataset.reportTargetId;

            if (type && targetId) {
                // Set the data for the modal form
                reportTypeInput.value = type;
                reportTargetIdInput.value = targetId;
                reportCategoryInput.value = ''; // Clear previous category
                reportReasonInput.value = ''; // Clear previous reason

                // Show the modal
                reportModalInstance.show();
            } else {
                console.error('Report button is missing data-report-type or data-report-target-id attributes.');
            }
        }
    });

    if (reportForm) {
        reportForm.addEventListener('submit', async function(event) {
            event.preventDefault();

            const type = reportTypeInput.value;
            const targetId = reportTargetIdInput.value;
            const category = reportCategoryInput.value;
            const reason = reportReasonInput.value;
            const reporterId = getReporterId();

            if (!category) {
                if (typeof showModal === 'function') {
                    showModal('warning', '입력 필요', '신고 종류를 선택해주세요.');
                } else {
                    alert('신고 종류를 선택해주세요.');
                }
                return;
            }

            if (!reason.trim()) {
                if (typeof showModal === 'function') {
                    showModal('warning', '입력 필요', '신고 사유를 상세히 작성해주세요.');
                } else {
                    alert('신고 사유를 입력해주세요.');
                }
                return;
            }

            if (reporterId === '0') { // Guest user
                const proceed = typeof showModal === 'function'
                    ? await new Promise(resolve => {
                        showModal('confirm', '확인', '로그인하지 않은 상태로 신고를 제출하시겠습니까?', () => resolve(true));
                    })
                    : confirm('로그인하지 않은 상태로 신고를 제출하시겠습니까?');

                if (!proceed) return;
            }

            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

                const headers = { 'Content-Type': 'application/json' };
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                } else {
                    console.warn('CSRF meta tags not found. Proceeding without CSRF token.');
                }

                const response = await fetch('/api/reports/submit', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify({
                        type: type,
                        reporterId: reporterId,
                        targetId: targetId,
                        category: category,
                        reason: reason
                    })
                });

                const result = await response.json();

                if (response.ok && result.status === 1) {
                    if (typeof showModal === 'function') {
                        showModal('success', '접수 완료', '신고가 접수되었습니다. 검토 후 조치하겠습니다.', () => {
                            reportModalInstance.hide();
                        });
                    } else {
                        alert('신고가 성공적으로 접수되었습니다.');
                        reportModalInstance.hide();
                    }
                } else {
                    if (typeof showModal === 'function') {
                        showModal('error', '접수 실패', '신고 접수에 실패했습니다: ' + (result.message || '알 수 없는 오류'));
                    } else {
                        alert('신고 제출 실패: ' + (result.message || '알 수 없는 오류가 발생했습니다.'));
                    }
                }
            } catch (error) {
                console.error('Error submitting report:', error);
                if (typeof showModal === 'function') {
                    showModal('error', '오류', '신고 접수 중 오류가 발생했습니다.');
                } else {
                    alert('신고 제출 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.');
                }
            }
        });
    } else {
        console.error('Report form element (#reportForm) not found.');
    }
});
