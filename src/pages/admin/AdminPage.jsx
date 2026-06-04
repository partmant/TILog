import React, { useState, useEffect } from 'react';
import '../../styles/admin/AdminPage.css';
import { fetchMemberList } from '../../api/adminApi'; // 🔥 백엔드 API 호출 함수

const AdminPage = () => {
    // ==========================================
    // 1. 상태(State) 관리: 마법의 데이터 상자
    // ==========================================
    const [members, setMembers] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    // ==========================================
    // 2. 부수 효과(Effect): 화면 렌더링 시 API 찌르기
    // ==========================================
    useEffect(() => {
        const loadData = async () => {
            try {
                // 백엔드 컨트롤러(getMemberList)에 1페이지, 10명 데이터를 요청합니다.
                const data = await fetchMemberList(0, 10);
                setMembers(data); // 성공하면 상자에 데이터를 담습니다.
            } catch (error) {
                console.error("회원 데이터를 불러오는데 실패했습니다.", error);
            } finally {
                setIsLoading(false); // 로딩이 끝났음을 알립니다.
            }
        };

        loadData();
    }, []); // 빈 배열 `[]`: 컴포넌트가 처음 화면에 나타날 때 딱 한 번만 실행!

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

    // 임시 더미 데이터 (나중에 신고 내역 API를 만들면 이것도 교체할 예정입니다)
    const recentReports = [
        { category: '욕설/비방', detail: '댓글 내용에 부적절한 표현 포함', status: '대기', statusColor: '#ef4444', labelColor: '#fee2e2' },
        { category: '광고/홍보', detail: '외부 링크 반복 등록', status: '대기', statusColor: '#ef4444', labelColor: '#ffedd5' },
        { category: '개인정보', detail: '이메일 주소 노출', status: '완료', statusColor: '#10b981', labelColor: '#d1fae5' },
        { category: '도배/스팸', detail: '동일 내용 반복 작성', status: '검토중', statusColor: '#3b82f6', labelColor: '#dbeafe' },
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

            {/* 메인 콘텐츠 그리드 */}
            <div className="admin-content-grid">
                {/* 좌측: 최근 신고 목록 */}
                <div className="admin-panel">
                    <h3 className="admin-panel-title">최근 신고 목록</h3>
                    <ul className="admin-report-list">
                        {recentReports.map((report, index) => (
                            <li className="admin-report-item" key={index}>
                                <span className="admin-report-label" style={{ backgroundColor: report.labelColor, color: report.statusColor }}>
                                    {report.category}
                                </span>
                                <span className="admin-report-detail">{report.detail}</span>
                                <span className="admin-report-status" style={{ color: report.statusColor }}>
                                    {report.status}
                                </span>
                            </li>
                        ))}
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
                            <button className="admin-action-btn">회원 상태 변경</button>
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