import React, {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import '../../styles/admin/AdminPage.css';
import {fetchMemberList, fetchRecentReports, changeMemberRole, doSanction, fetchReportStats} from '../../api/adminApi';
import {getCurrentUser} from '../../utils/authUtils';

const AdminPage = () => {

    const navigate = useNavigate();

    // 1. 상태 관리
    const [members, setMembers] = useState(null);
    const [recentReports, setRecentReports] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [stats, setStats] = useState({pending: 0, processed: 0});

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [targetMemberId, setTargetMemberId] = useState('');
    const [targetRole, setTargetRole] = useState('PREMIUM');

    const [isReportModalOpen, setIsReportModalOpen] = useState(false);
    const [selectedReportId, setSelectedReportId] = useState(null);
    const [sanctionData, setSanctionData] = useState({
        sanctionType: 'WARNING',
        content: ''
    });

    // 2. 데이터 로드 함수
    const loadData = async () => {
        setIsLoading(true);
        try {
            const [memberRes, reportRes, statsRes] = await Promise.all([
                fetchMemberList(0, 10),
                fetchRecentReports(),
                fetchReportStats()
            ]);
            setMembers(memberRes.data || memberRes);
            setRecentReports(reportRes.data || reportRes);
            setStats(statsRes.data || statsRes);
        } catch (error) {
            console.error("데이터 로딩 실패", error);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        const currentUser = getCurrentUser();

        if (!currentUser || currentUser.role !== 'ADMIN') {
            alert('비정상적인 접근입니다. 관리자만 이용할 수 있습니다 🚨');
            navigate('/feed', {replace: true});
            return;
        }

        loadData();
    }, [navigate]);

    // 3. 신고 제재 실행
    const handleSanctionSubmit = async () => {
        if (!sanctionData.content) {
            alert('제재 사유(내용)를 입력해주세요.');
            return;
        }

        try {
            const currentUser = getCurrentUser();
            const extractedAdminId = currentUser.id || currentUser.memberId;
            const adminIdNum = Number(extractedAdminId);

            await doSanction(selectedReportId, {
                adminId: adminIdNum,
                sanctionType: sanctionData.sanctionType,
                reasonType: 'ETC',
                content: sanctionData.content
            });

            alert('신고 처리가 완료되었습니다!');
            setIsReportModalOpen(false);
            setSanctionData({sanctionType: 'WARNING', content: ''});

            await loadData();
        } catch (error) {
            console.error(error);
            alert('신고 처리에 실패했습니다.');
        }
    };

    const handleRoleSubmit = async () => {
        if (!targetMemberId) {
            alert('권한을 변경할 회원을 선택해주세요.');
            return;
        }

        try {
            await changeMemberRole(Number(targetMemberId), targetRole);
            alert('회원 권한이 성공적으로 변경되었습니다!');
            setIsModalOpen(false);
            await loadData();
        } catch (error) {
            console.error(error);
            alert('권한 변경에 실패했습니다.');
        }
    };

    const formatReportData = (report) => {
        let category = '기타';
        let labelColor = '#f3f4f6';

        if (report.reasonType === 'SWEARING') {
            category = '욕설/비방';
            labelColor = '#fee2e2';
        } else if (report.reasonType === 'ADVERTISEMENT') {
            category = '광고/홍보';
            labelColor = '#ffedd5';
        } else if (report.reasonType === 'SPAM') {
            category = '도배/스팸';
            labelColor = '#dbeafe';
        } else if (report.reasonType === 'PERSONAL_INFO') {
            category = '개인정보';
            labelColor = '#d1fae5';
        }

        let status = '대기';
        let statusColor = '#ef4444';

        if (report.status === 'PROCESSED') {
            status = '완료';
            statusColor = '#10b981';
        } else if (report.status === 'IN_PROGRESS') {
            status = '검토중';
            statusColor = '#3b82f6';
        }

        return {category, detail: report.reasonDetail, status, statusColor, labelColor, rawStatus: report.status};
    };

    // 🔥 전체 TIL 카드 삭제
    const statsData = [
        {title: '전체 회원', count: `${members?.totalElements || 0}명`, icon: '👥'},
        {title: '대기 신고', count: `${stats.pending}건`, icon: '⚠️', isAlert: true},
        {title: '처리 완료', count: `${stats.processed}건`, icon: '✅'},
    ];

    return (
        <div className="admin-page">
            <div className="admin-hero-card">
                <h2 className="admin-hero-title">관리자 대시보드</h2>
                <p className="admin-hero-subtitle">회원, 게시글, 신고 및 운영 현황을 관리합니다.</p>
            </div>

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

                                const isProcessed = formatted.rawStatus === 'PROCESSED' || formatted.status === '완료';
                                return (
                                    <li
                                        className="admin-report-item"
                                        key={report.reportId}
                                        // 🔥 완료된 항목은 클릭 불가(커서 모양도 기본)
                                        style={{
                                            cursor: isProcessed ? 'default' : 'pointer',
                                            transition: 'background 0.2s',
                                            opacity: isProcessed ? 0.5 : 1 // 완료된 건 약간 흐리게
                                        }}
                                        onClick={() => {
                                            // 🔥 완료 상태면 아무 동작 안 함
                                            if (isProcessed) return;
                                            setSelectedReportId(report.reportId);
                                            setIsReportModalOpen(true);
                                        }}
                                        onMouseEnter={(e) => {
                                            if (!isProcessed) e.currentTarget.style.backgroundColor = '#f3f4f6';
                                        }}
                                        onMouseLeave={(e) => {
                                            if (!isProcessed) e.currentTarget.style.backgroundColor = '#fafafa';
                                        }}
                                    >
                                        <span className="admin-report-label" style={{
                                            backgroundColor: formatted.labelColor,
                                            color: formatted.statusColor
                                        }}>
                                            {formatted.category}
                                        </span>
                                        <span className="admin-report-detail"
                                              style={{textDecoration: isProcessed ? 'line-through' : 'none'}}>
    <strong style={{color: '#4b5563', marginRight: '8px'}}>
        [{report.reporterNickname || '알수없음'} ➡️ {report.reportedNickname || '알수없음'}]
    </strong>
                                            {formatted.detail}
</span>
                                        <span className="admin-report-status" style={{color: formatted.statusColor}}>
                                            {formatted.status}
                                        </span>
                                    </li>
                                )
                            })
                        )}
                    </ul>
                </div>

                <div className="admin-side-grid">
                    <div className="admin-panel">
                        <h3 className="admin-panel-title">운영 관리</h3>
                        <div className="admin-op-row" style={{gridTemplateColumns: '1fr'}}>
                            <div
                                className="admin-op-item"
                                style={{cursor: 'pointer', transition: '0.2s'}}
                                onClick={() => setIsModalOpen(true)}
                                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                            >
                                <div className="admin-op-icon purple-gradient">🔒</div>
                                <div className="admin-op-info">
                                    <span className="op-title" style={{fontSize: '15px', color: '#111827'}}>회원 및 권한 통합 관리</span>
                                    <span className="op-sub">전체 회원 목록 조회 및 권한/상태 변경</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 회원/권한 모달 */}
            {isModalOpen && (
                <div className="admin-modal-overlay">
                    <div className="admin-modal-card" style={{maxWidth: '800px'}}>
                        <h3>회원 및 권한 통합 관리</h3>

                        <div className="admin-modal-form" style={{
                            display: 'flex',
                            flexDirection: 'row',
                            gap: '16px',
                            alignItems: 'flex-end',
                            marginBottom: '24px'
                        }}>
                            <div style={{flex: 2}}>
                                <label>대상 회원 선택</label>
                                <select value={targetMemberId} onChange={(e) => setTargetMemberId(e.target.value)}
                                        style={{width: '100%'}}>
                                    <option value="">회원을 선택하세요</option>
                                    {members?.content?.map(member => (
                                        <option key={member.memberId} value={member.memberId}>
                                            {member.nickname} ({member.email}) - {member.role}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div style={{flex: 1}}>
                                <label>변경할 권한</label>
                                <select value={targetRole} onChange={(e) => setTargetRole(e.target.value)}
                                        style={{width: '100%'}}>
                                    <option value="USER">일반 사용자</option>
                                    <option value="PREMIUM">프리미엄</option>
                                    <option value="MENTOR">멘토</option>
                                    <option value="ADMIN">관리자</option>
                                </select>
                            </div>
                            <button className="admin-btn-save" style={{flex: 1, height: '48px'}}
                                    onClick={handleRoleSubmit}>
                                변경 적용
                            </button>
                        </div>

                        <hr style={{border: 'none', borderTop: '1px solid #e5e7eb', marginBottom: '24px'}}/>

                        <h4 style={{fontSize: '15px', fontWeight: 'bold', marginBottom: '12px'}}>전체 회원 목록
                            ({members?.totalElements || 0}명)</h4>
                        <div style={{
                            maxHeight: '300px',
                            overflowY: 'auto',
                            border: '1px solid #e5e7eb',
                            borderRadius: '12px'
                        }}>
                            <table style={{
                                width: '100%',
                                borderCollapse: 'collapse',
                                textAlign: 'left',
                                fontSize: '13px'
                            }}>
                                <thead style={{position: 'sticky', top: 0, backgroundColor: '#f9fafb', zIndex: 1}}>
                                <tr style={{color: '#4b5563'}}>
                                    <th style={{padding: '12px'}}>ID</th>
                                    <th style={{padding: '12px'}}>닉네임</th>
                                    <th style={{padding: '12px'}}>이메일</th>
                                    <th style={{padding: '12px'}}>권한</th>
                                    <th style={{padding: '12px'}}>상태</th>
                                </tr>
                                </thead>
                                <tbody>
                                {members?.content?.map((m) => (
                                    <tr key={m.memberId} style={{borderBottom: '1px solid #f3f4f6'}}>
                                        <td style={{padding: '12px', color: '#9ca3af'}}>#{m.memberId}</td>
                                        <td style={{padding: '12px', fontWeight: 'bold'}}>{m.nickname}</td>
                                        <td style={{padding: '12px'}}>{m.email}</td>
                                        <td style={{
                                            padding: '12px',
                                            fontWeight: 'bold',
                                            color: m.role === 'ADMIN' ? '#ef4444' : m.role === 'MENTOR' ? '#8b5cf6' : '#6b7280'
                                        }}>
                                            {m.role}
                                        </td>
                                        <td style={{padding: '12px', color: m.isBanned ? '#ef4444' : '#10b981'}}>
                                            {m.isBanned ? '정지됨' : '정상'}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="admin-modal-actions" style={{marginTop: '24px'}}>
                            <button className="admin-btn-cancel" onClick={() => setIsModalOpen(false)}>닫기</button>
                        </div>
                    </div>
                </div>
            )}

            {/* 신고 처리 모달 */}
            {isReportModalOpen && (
                <div className="admin-modal-overlay">
                    <div className="admin-modal-card">
                        <h3>신고 처리 및 회원 제재</h3>

                        <div className="admin-modal-form">
                            <label>제재 수위 선택</label>
                            <select
                                value={sanctionData.sanctionType}
                                onChange={(e) => setSanctionData({...sanctionData, sanctionType: e.target.value})}
                            >
                                <option value="WARNING">경고 (WARNING)</option>
                                <option value="SUSPENSION">계정 정지 (SUSPENSION)</option>
                                <option value="BAN">영구 추방 (BAN)</option>
                            </select>

                            <label>제재 사유 작성 (유저에게 발송됨)</label>
                            <textarea
                                rows={4}
                                placeholder="이용 약관 위반 사유를 상세히 적어주세요."
                                value={sanctionData.content}
                                onChange={(e) => setSanctionData({...sanctionData, content: e.target.value})}
                            />
                        </div>

                        <div className="admin-modal-actions">
                            <button className="admin-btn-cancel" onClick={() => setIsReportModalOpen(false)}>
                                취소
                            </button>
                            <button className="admin-btn-save" style={{background: '#ef4444'}}
                                    onClick={handleSanctionSubmit}>
                                제재 확정
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminPage;