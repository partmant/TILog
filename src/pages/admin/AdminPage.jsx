import React, { useState, useEffect } from 'react';
import '../../styles/admin/AdminPage.css';
import { fetchMemberList, fetchRecentReports, changeMemberRole } from '../../api/adminApi';

const AdminPage = () => {
    // ==========================================
    // 1. 상태(State) 관리: 마법의 데이터 상자
    // ==========================================
    const [members, setMembers] = useState(null);
    const [recentReports, setRecentReports] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [targetMemberId, setTargetMemberId] = useState('');
    const [targetRole, setTargetRole] = useState('PREMIUM');
    // ==========================================
    // 2. 부수 효과(Effect): 화면 렌더링 시 API 찌르기
    // ==========================================
    useEffect(() => {
        const loadData = async () => {
            try {
                // 백엔드 컨트롤러(getMemberList)에 1페이지, 10명 데이터를 요청합니다.
                const [memberData, reportData] = await Promise.all([
                    fetchMemberList(0, 10),
                    fetchRecentReports()
                ]);

                setMembers(memberData);
                setRecentReports(reportData);

            } catch (error) {
                console.error("회원 데이터를 불러오는데 실패했습니다.", error);
            } finally {
                setIsLoading(false); // 로딩이 끝났음을 알립니다.
            }
        };

        loadData();
    }, []); // 빈 배열 `[]`: 컴포넌트가 처음 화면에 나타날 때 딱 한 번만 실행!

    // 3. 백엔드 데이터(Enum)를 화면용 예쁜 한글/색상으로 변환해 주는 마법의 함수
    const formatReportData = (report) => {
        // 사유(ReasonType) 변환
        let category = '기타';
        let labelColor = '#f3f4f6';

        if (report.reasonType === 'SWEARING') { category = '욕설/비방'; labelColor = '#fee2e2'; }
        else if (report.reasonType === 'ADVERTISEMENT') { category = '광고/홍보'; labelColor = '#ffedd5'; }
        else if (report.reasonType === 'SPAM') { category = '도배/스팸'; labelColor = '#dbeafe'; }
        else if (report.reasonType === 'PERSONAL_INFO') { category = '개인정보'; labelColor = '#d1fae5'; }

        // 처리 상태(Status) 변환
        let status = '대기';
        let statusColor = '#ef4444'; // 빨간색

        if (report.status === 'PROCESSED') { status = '완료'; statusColor = '#10b981'; } // 초록색
        else if (report.status === 'IN_PROGRESS') { status = '검토중'; statusColor = '#3b82f6'; } // 파란색

        return {
            category,
            detail: report.reasonDetail, // 백엔드에서 온 상세 내용
            status,
            statusColor,
            labelColor
        };
    };

    const handleRoleSubmit = async () => {
        if (!targetMemberId) {
            alert('권한을 변경할 회원을 선택해주세요.');
            return;
        }

        try {
            // 백엔드로 PATCH API 요청 발사!
            await changeMemberRole(Number(targetMemberId), targetRole);
            alert('회원 권한이 성공적으로 변경되었습니다!');

            setIsModalOpen(false); // 모달창 닫기

            // 실무 꿀팁: 권한이 변경되었으니, 회원 목록 데이터를 다시 불러와 화면을 갱신해줍니다.
            const updatedMembers = await fetchMemberList(0, 10);
            setMembers(updatedMembers);
        } catch (error) {
            console.error(error);
            alert('권한 변경에 실패했습니다.');
        }
    };
    
    // ==========================================
    // 3. 화면에 보여줄 데이터 준비
    // ==========================================
    const statsData = [
        {
            title: '전체 회원',
            // 🔥 마법이 일어나는 곳: 로딩 중이면 '로딩중...', 완료되면 실제 DB의 회원 수 출력!
            count: isLoading ? '로딩중...' : `${members?.totalElements || 0}명`,
            subText: '실시간 백엔드 연동 완료!',
            icon: '👥'
        },
        { title: '전체 TIL', count: '8,932개', subText: '오늘 +128', icon: '📝' },
        { title: '대기 신고', count: '17건', subText: '확인 필요', icon: '⚠️', isAlert: true },
        { title: '처리 완료', count: '284건', subText: '이번 달 기준', icon: '✅' },
    ];

    // ==========================================
    // 4. 화면 그리기 (렌더링)
    // ==========================================
    return (
        <div className="admin-page">
            {/* 상단 배너 */}
            <div className="admin-hero-card">
                <h2 className="admin-hero-title">관리자 대시보드</h2>
                <p className="admin-hero-subtitle">회원, 게시글, 신고 및 운영 현황을 관리합니다.</p>
            </div>

            {/* 통계 카드 섹션 */}
            <div className="admin-stats-row">
                {statsData.map((stat, index) => (
                    <div className="admin-stat-card" key={index}>
                        <div className="admin-stat-icon">{stat.icon}</div>
                        <div className="admin-stat-info">
                            <span className="admin-stat-title">{stat.title}</span>
                            <span className={`admin-stat-count ${stat.isAlert ? 'text-error' : ''}`}>
                                {stat.count}
                            </span>
                            <span className="admin-stat-subtext">{stat.subText}</span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="admin-content-grid">
                <div className="admin-panel">
                    <h3 className="admin-panel-title">최근 신고 목록</h3>
                    <ul className="admin-report-list">
                        {recentReports.length === 0 && !isLoading ? (
                            <li className="admin-report-item">접수된 신고가 없습니다.</li>
                        ) : (
                            recentReports.map((report) => {
                                const formatted = formatReportData(report);
                                return (
                                    <li className="admin-report-item" key={report.reportId}>
                                        <span className="admin-report-label" style={{ backgroundColor: formatted.labelColor, color: formatted.statusColor }}>
                                            {formatted.category}
                                        </span>
                                        <span className="admin-report-detail">{formatted.detail}</span>
                                        <span className="admin-report-status" style={{ color: formatted.statusColor }}>
                                            {formatted.status}
                                        </span>
                                    </li>
                                )
                            })
                        )}
                    </ul>
                </div>

                {/* 우측: 운영 관리 및 빠른 작업 */}
                <div className="admin-side-grid">
                    <div className="admin-panel">
                        <h3 className="admin-panel-title">운영 관리</h3>
                        <div className="admin-op-row">
                            <div className="admin-op-item">
                                <div className="admin-op-icon purple-gradient">🔒</div>
                                <div className="admin-op-info">
                                    <span className="op-title">권한 관리</span>
                                    <span className="op-count">6개</span>
                                    <span className="op-sub">관리자/일반 사용자</span>
                                </div>
                            </div>
                            <div className="admin-op-item">
                                <div className="admin-op-icon cyan-gradient">📊</div>
                                <div className="admin-op-info">
                                    <span className="op-title">활성 챌린지</span>
                                    <span className="op-count">3개</span>
                                    <span className="op-sub">진행 중인 페이백</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="admin-panel">
                        <h3 className="admin-panel-title">빠른 작업</h3>
                        <div className="admin-action-grid">
                            <button className="admin-action-btn">신고 상세 보기</button>
                            <button className="admin-action-btn" onClick={() => setIsModalOpen(true)}>
                                회원 상태 변경
                            </button>
                            {isModalOpen && (
                                <div className="admin-modal-overlay">
                                    <div className="admin-modal-card">
                                        <h3>회원 권한 변경</h3>

                                        <div className="admin-modal-form">
                                            <label>대상 회원</label>
                                            {/* 아까 가져온 members.content 데이터를 select 박스에 뿌려줍니다! */}
                                            <select
                                                value={targetMemberId}
                                                onChange={(e) => setTargetMemberId(e.target.value)}
                                            >
                                                <option value="">회원을 선택하세요</option>
                                                {members?.content?.map(member => (
                                                    <option key={member.memberId || member.id} value={member.memberId || member.id}>
                                                        {member.nickname} ({member.email}) - 현재: {member.role}
                                                    </option>
                                                ))}
                                            </select>

                                            <label>변경할 권한</label>
                                            <select
                                                value={targetRole}
                                                onChange={(e) => setTargetRole(e.target.value)}
                                            >
                                                <option value="USER">일반 사용자 (USER)</option>
                                                <option value="PREMIUM">프리미엄 (PREMIUM)</option>
                                                <option value="MENTOR">멘토 (MENTOR)</option>
                                                <option value="ADMIN">관리자 (ADMIN)</option>
                                            </select>
                                        </div>

                                        <div className="admin-modal-actions">
                                            <button className="admin-btn-cancel" onClick={() => setIsModalOpen(false)}>
                                                취소
                                            </button>
                                            <button className="admin-btn-save" onClick={handleRoleSubmit}>
                                                변경 저장
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            )}
                            <button className="admin-action-btn">공지 등록</button>
                            <button className="admin-action-btn">페이백 정산 확인</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminPage;