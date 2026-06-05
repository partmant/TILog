// src/pages/feedback/FeedbackPage.jsx
import React, { useState, useEffect } from 'react';
import '../../styles/feedback/FeedbackPage.css';
// 🔥 writeFeedback API 함수도 꼭 import에 추가해 주세요!
import { fetchFeedbackList, requestFeedback, writeFeedback, getFeedbackDetail } from '../../api/feedbackApi';import { getCurrentUser } from '../../utils/authUtils';

const FeedbackPage = () => {
    // 1. 현재 로그인한 유저 정보 확인
    const user = getCurrentUser();
    const isMentor = user?.role === 'MENTOR' || user?.role === 'ADMIN';
    const myMemberId = user?.id || user?.memberId; // 내 회원 ID

    // 2. 메인 화면 탭 및 리스트 상태
    const [activeTab, setActiveTab] = useState(isMentor ? 'RECEIVED' : 'REQUESTED');
    const [feedbacks, setFeedbacks] = useState([]);

    // 3. [피드백 요청하기] 모달 상태 (일반/프리미엄 유저용)
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [requestData, setRequestData] = useState({
        tilId: '',
        mentorId: '',
        comment: ''
    });
    const [myPosts, setMyPosts] = useState([]);
    const [mentors, setMentors] = useState([]);

    // 4. [멘토 답변 작성하기] 모달 상태 (멘토용)
    const [isWriteModalOpen, setIsWriteModalOpen] = useState(false);
    const [selectedFeedbackId, setSelectedFeedbackId] = useState(null);
    const [writeData, setWriteData] = useState({
        technicalScore: 5,
        flowScore: 5,
        designScore: 5,
        comment: ''
    });

    // ==========================================
    // 🔥 5. 상세 보기(결과) 모달 상태 추가
    // ==========================================
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
    const [detailData, setDetailData] = useState(null);

    // 상세 보기 버튼 클릭 시 실행할 함수
    const openDetailModal = async (feedbackId) => {
        try {
            // 1. 백엔드 API를 찔러서 상세 데이터를 가져옵니다.
            // const data = await getFeedbackDetail(feedbackId);
            // setDetailData(data);

            // 🚨 [주의] 현재 DB에 진짜 데이터가 없을 수 있으므로 테스트용 가짜 데이터를 세팅해 둡니다.
            // 나중에 백엔드와 진짜 연동할 때는 위 주석을 풀고 아래 setDetailData를 지워주세요!
            setDetailData({
                technicalScore: 4,
                flowScore: 5,
                designScore: 4,
                comment: "전반적인 흐름과 설계가 아주 훌륭합니다. 다만 JPA N+1 문제가 발생할 여지가 있으니 fetch join을 적용해 보는 것을 추천합니다!",
                requestedAt: "2026-06-04T10:00:00",
                completedAt: "2026-06-04T14:30:00"
            });

            // 2. 모달창을 엽니다.
            setIsDetailModalOpen(true);
        } catch (error) {
            console.error(error);
            alert("상세 정보를 불러오는데 실패했습니다.");
        }
    };

    useEffect(() => {
        const loadFeedbacks = async () => {
            try {
                const response = await fetchFeedbackList();

                setFeedbacks(response.data || response || []);

            } catch (error) {
                console.error("피드백 목록을 불러오는데 실패했습니다.", error);
            }
        };
        const loadDropdownOptions = async () => {
            // 가짜 '내 게시글' 목록
            setMyPosts([
                { postId: 101, title: "[Day 1] Spring Boot 초기 세팅하기" },
                { postId: 102, title: "[Day 2] 리액트 컴포넌트 분리 연습" },
                { postId: 103, title: "[Day 3] JPA 연관관계 매핑 (어려움)" }
            ]);

            // 가짜 '멘토' 목록
            setMentors([
                { memberId: 1, nickname: "adm (수석 멘토)" },
                { memberId: 2, nickname: "senior_dev (백엔드 마스터)" },
                { memberId: 3, nickname: "frontend_king (리액트 장인)" }
            ]);
        };

        loadDropdownOptions();

        loadFeedbacks();
    }, []); // 컴포넌트가 처음 화면에 켜질 때 딱 한 번 실행

    // 현재 선택된 탭('REQUESTED' or 'RECEIVED')에 맞게 데이터 필터링
    const filteredFeedbacks = feedbacks.filter(fb => fb.type === activeTab);

    // ==========================================
    // 🚀 함수: 피드백 요청 제출 (POST)
    // ==========================================
    const handleRequestSubmit = async () => {
        if (!requestData.tilId || !requestData.mentorId || !requestData.message) {
            alert('모든 항목을 입력해주세요!');
            return;
        }

        try {
            await requestFeedback(
                Number(requestData.tilId),
                Number(myMemberId), // 내 ID 자동 삽입
                Number(requestData.mentorId),
                requestData.message
            );

            alert('멘토에게 피드백이 성공적으로 요청되었습니다!');
            setIsModalOpen(false);
            setRequestData({ tilId: '', mentorId: '', message: '' });
        } catch (error) {
            console.error(error);
            alert('피드백 요청에 실패했습니다.');
        }
    };

    // ==========================================
    // 🚀 함수: 멘토 답변 모달 열기 & 제출 (PATCH)
    // ==========================================
    const openWriteModal = (feedbackId) => {
        setSelectedFeedbackId(feedbackId);
        setIsWriteModalOpen(true);
    };

    const handleWriteSubmit = async () => {
        if (!writeData.comment) {
            alert('코멘트를 작성해주세요!');
            return;
        }

        try {
            await writeFeedback(
                selectedFeedbackId,
                Number(myMemberId), // 멘토(나)의 ID
                Number(writeData.technicalScore),
                Number(writeData.flowScore),
                Number(writeData.designScore),
                writeData.comment
            );

            alert('성공적으로 피드백을 작성했습니다!');
            setIsWriteModalOpen(false);
            setWriteData({ technicalScore: 5, flowScore: 5, designScore: 5, comment: '' });
        } catch (error) {
            console.error(error);
            alert('피드백 작성에 실패했습니다.');
        }
    };

    // ==========================================
    // 🎨 화면 그리기 (렌더링)
    // ==========================================
    return (
        <div className="feedback-page">
            {/* 1. 상단 영웅(Hero) 배너 */}
            <div className="feedback-hero">
                <div className="feedback-hero-content">
                    <h2>멘토 피드백</h2>
                    <p>전문가 멘토의 꼼꼼한 코드 리뷰와 조언으로 한 단계 더 성장하세요.</p>
                </div>
                <button className="feedback-request-btn" onClick={() => setIsModalOpen(true)}>
                    + 피드백 요청하기
                </button>
            </div>

            {/* 2. 탭 메뉴 */}
            <div className="feedback-tabs">
                <button
                    className={`tab-btn ${activeTab === 'REQUESTED' ? 'active' : ''}`}
                    onClick={() => setActiveTab('REQUESTED')}
                >
                    내가 요청한 피드백
                </button>
                {isMentor && (
                    <button
                        className={`tab-btn ${activeTab === 'RECEIVED' ? 'active' : ''}`}
                        onClick={() => setActiveTab('RECEIVED')}
                    >
                        내가 받은 요청 (멘토용)
                    </button>
                )}
            </div>

            {/* 3. 피드백 리스트 콘텐츠 */}
            <div className="feedback-content">
                {filteredFeedbacks.length === 0 ? (
                    <div className="feedback-empty">
                        <span>📝</span>
                        <p>해당 내역이 없습니다.</p>
                    </div>
                ) : (
                    <div className="feedback-grid">
                        {filteredFeedbacks.map(fb => (
                            <div className="feedback-card" key={fb.id}>
                                <div className="fb-card-header">
                                    <span className={`fb-status ${fb.status}`}>
                                        {fb.status === 'WAITING' ? '대기 중' : '답변 완료'}
                                    </span>
                                    <span className="fb-date">{fb.date}</span>
                                </div>
                                <h3 className="fb-post-title">{fb.postTitle}</h3>
                                <div className="fb-card-footer">
                                    <span className="fb-person">
                                        {activeTab === 'REQUESTED' ? `멘토: ${fb.mentorName}` : `요청자: ${fb.requesterName}`}
                                    </span>

                                    {/* 멘토 탭 & 대기중일 때만 '답변하기' 버튼 노출 */}
                                    {activeTab === 'RECEIVED' && fb.status === 'WAITING' ? (
                                        <button className="fb-action-btn write-btn" onClick={() => openWriteModal(fb.id)}>
                                            답변하기
                                        </button>
                                    ) : (
                                        <button className="fb-action-btn" onClick={() => openDetailModal(fb.id)}>
                                            상세 보기
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* ========================================== */}
            {/* 팝업창 1: 일반 유저 피드백 요청 모달 */}
            {/* ========================================== */}
            {isModalOpen && (
                <div className="feedback-modal-overlay">
                    <div className="feedback-modal-card">
                        <h3>피드백 요청하기</h3>
                        <div className="feedback-modal-form">
                            <label>피드백 받을 TIL 게시글</label>
                            <select
                                value={requestData.tilId}
                                onChange={(e) => setRequestData({...requestData, tilId: e.target.value})}
                            >
                                <option value="" disabled>어떤 게시글을 리뷰 받을까요?</option>
                                {myPosts.map(post => (
                                    <option key={post.postId} value={post.postId}>
                                        {post.title}
                                    </option>
                                ))}
                            </select>

                            {/* 🔥 2. 멘토 선택 드롭다운으로 변경! */}
                            <label>담당 멘토 선택</label>
                            <select
                                value={requestData.mentorId}
                                onChange={(e) => setRequestData({...requestData, mentorId: e.target.value})}
                            >
                                <option value="" disabled>어떤 멘토에게 요청할까요?</option>
                                {mentors.map(mentor => (
                                    <option key={mentor.memberId} value={mentor.memberId}>
                                        {mentor.nickname}
                                    </option>
                                ))}
                            </select>

                            <label>요청 메시지 (message)</label>
                            <textarea
                                placeholder="어떤 부분을 리뷰받고 싶은지 상세히 적어주세요!"
                                value={requestData.message}
                                onChange={(e) => setRequestData({...requestData, message: e.target.value})}
                                rows={4}
                            />
                        </div>
                        <div className="feedback-modal-actions">
                            <button className="fb-btn-cancel" onClick={() => setIsModalOpen(false)}>취소</button>
                            <button className="fb-btn-submit" onClick={handleRequestSubmit}>요청 보내기</button>
                        </div>
                    </div>
                </div>
            )}

            {/* ========================================== */}
            {/* 팝업창 2: 멘토 답변 작성 모달 */}
            {/* ========================================== */}
            {isWriteModalOpen && (
                <div className="feedback-modal-overlay">
                    <div className="feedback-modal-card">
                        <h3>피드백 작성하기</h3>
                        <div className="feedback-modal-form">
                            <div style={{ display: 'flex', gap: '12px' }}>
                                <div style={{ flex: 1 }}>
                                    <label>기술 점수 (1~5)</label>
                                    <input
                                        type="number" min="1" max="5"
                                        value={writeData.technicalScore}
                                        onChange={(e) => setWriteData({...writeData, technicalScore: e.target.value})}
                                    />
                                </div>
                                <div style={{ flex: 1 }}>
                                    <label>논리/흐름 (1~5)</label>
                                    <input
                                        type="number" min="1" max="5"
                                        value={writeData.flowScore}
                                        onChange={(e) => setWriteData({...writeData, flowScore: e.target.value})}
                                    />
                                </div>
                                <div style={{ flex: 1 }}>
                                    <label>구조/설계 (1~5)</label>
                                    <input
                                        type="number" min="1" max="5"
                                        value={writeData.designScore}
                                        onChange={(e) => setWriteData({...writeData, designScore: e.target.value})}
                                    />
                                </div>
                            </div>
                            <label>총평 (comment)</label>
                            <textarea
                                placeholder="작성자의 성장을 위한 따뜻하고 예리한 피드백을 남겨주세요."
                                value={writeData.comment}
                                onChange={(e) => setWriteData({...writeData, comment: e.target.value})}
                                rows={5}
                            />
                        </div>
                        <div className="feedback-modal-actions">
                            <button className="fb-btn-cancel" onClick={() => setIsWriteModalOpen(false)}>취소</button>
                            <button className="fb-btn-submit" onClick={handleWriteSubmit}>피드백 등록</button>
                        </div>
                    </div>
                </div>
            )}

            {/* ========================================== */}
            {/* 팝업창 3: 상세 결과 확인 모달 */}
            {/* ========================================== */}
            {isDetailModalOpen && detailData && (
                <div className="feedback-modal-overlay">
                    <div className="feedback-modal-card">
                        <h3>피드백 결과 리포트</h3>

                        <div className="feedback-detail-content" style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginBottom: '32px' }}>
                            <div style={{ display: 'flex', gap: '12px', background: '#f8fafc', padding: '16px', borderRadius: '16px' }}>
                                <div style={{ flex: 1, textAlign: 'center' }}>
                                    <div style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>기술 점수</div>
                                    <div style={{ fontSize: '24px', fontWeight: '900', color: '#8b5cf6' }}>{detailData.technicalScore}<span style={{fontSize: '14px', color: '#94a3b8'}}>/5</span></div>
                                </div>
                                <div style={{ flex: 1, textAlign: 'center', borderLeft: '1px solid #e2e8f0', borderRight: '1px solid #e2e8f0' }}>
                                    <div style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>논리/흐름</div>
                                    <div style={{ fontSize: '24px', fontWeight: '900', color: '#06b6d4' }}>{detailData.flowScore}<span style={{fontSize: '14px', color: '#94a3b8'}}>/5</span></div>
                                </div>
                                <div style={{ flex: 1, textAlign: 'center' }}>
                                    <div style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>구조/설계</div>
                                    <div style={{ fontSize: '24px', fontWeight: '900', color: '#10b981' }}>{detailData.designScore}<span style={{fontSize: '14px', color: '#94a3b8'}}>/5</span></div>
                                </div>
                            </div>

                            <div>
                                <h4 style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#334155' }}>멘토의 총평</h4>
                                <div style={{ background: '#f1f5f9', padding: '16px', borderRadius: '12px', fontSize: '15px', color: '#1e293b', lineHeight: '1.6' }}>
                                    {detailData.comment}
                                </div>
                            </div>
                        </div>

                        <div className="feedback-modal-actions">
                            <button className="fb-btn-cancel" style={{ width: '100%' }} onClick={() => setIsDetailModalOpen(false)}>닫기</button>
                        </div>
                    </div>
                </div>
            )}

        </div>
    );
};

export default FeedbackPage;