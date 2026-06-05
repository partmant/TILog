import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../styles/feedback/FeedbackPage.css';
import {
    fetchFeedbackList,
    requestFeedback,
    writeFeedback,
    getFeedbackDetail,
    fetchMentors,
    fetchMySimplePosts
} from '../../api/feedbackApi';
import { getCurrentUser } from '../../utils/authUtils';

const FeedbackPage = () => {
    // 1. 내 정보
    const navigate = useNavigate();
    const user = getCurrentUser();
    const isMentor = user?.role === 'MENTOR' || user?.role === 'ADMIN';
    const myMemberId = user?.id || user?.memberId;

    // 2. 메인 리스트 상태
    const [activeTab, setActiveTab] = useState(isMentor ? 'RECEIVED' : 'REQUESTED');
    const [feedbacks, setFeedbacks] = useState([]);

    // 3. 모달 제어 상태
    const [isModalOpen, setIsModalOpen] = useState(false); // 피드백 요청 모달
    const [isWriteModalOpen, setIsWriteModalOpen] = useState(false); // 답변 작성 모달
    const [isDetailModalOpen, setIsDetailModalOpen] = useState(false); // 상세 보기 모달

    // 4. 데이터 상태
    const [requestData, setRequestData] = useState({ tilId: '', mentorId: '', message: '' });
    const [writeData, setWriteData] = useState({ technicalScore: 5, flowScore: 5, designScore: 5, comment: '' });
    const [detailData, setDetailData] = useState(null);
    const [selectedFeedbackId, setSelectedFeedbackId] = useState(null);

    // ==========================================
    // 드롭다운용 DB 데이터 상태
    // ==========================================
    const [myPosts, setMyPosts] = useState([]);
    const [mentors, setMentors] = useState([]);

    useEffect(() => {
        // user 정보가 없거나(비로그인), 권한이 일반 USER인 경우
        if (!user || user.role === 'USER') {
            alert('프리미엄 회원 또는 멘토만 이용할 수 있는 기능입니다');
            navigate(-1); // 이전 페이지로 강제 백! (또는 navigate('/feed') 로 메인으로 쫓아냄)
        }
    }, [user, navigate]);

    // 화면 렌더링 시 피드백 목록 불러오기
    useEffect(() => {
        const loadFeedbacks = async () => {
            try {
                const res = await fetchFeedbackList();
                setFeedbacks(res.data || res || []);
            } catch (error) {
                console.error("목록 불러오기 실패", error);
            }
        };
        loadFeedbacks();
    }, []);

    // 모달창이 열릴 때마다 멘토 목록과 내 게시글 목록을 DB에서 가져오기!
    useEffect(() => {
        if (isModalOpen) {
            const loadDropdownData = async () => {
                try {
                    const mentorsRes = await fetchMentors();
                    const postsRes = await fetchMySimplePosts();

                    // 백엔드 ApiResponse 구조(success, data)에 맞게 추출
                    setMentors(mentorsRes.data || mentorsRes || []);
                    setMyPosts(postsRes.data || postsRes || []);
                } catch (error) {
                    console.error("드롭다운 데이터를 불러오는데 실패했습니다.", error);
                }
            };
            loadDropdownData();
        }
    }, [isModalOpen]); // isModalOpen이 true가 될 때만 실행!

    const filteredFeedbacks = feedbacks.filter(fb => fb.type === activeTab);

    // ==========================================
    // 🛠 액션 함수들 (요청, 작성, 상세조회)
    // ==========================================
    const handleRequestSubmit = async () => {
        if (!requestData.tilId || !requestData.mentorId || !requestData.message) {
            alert('게시글, 멘토, 메시지를 모두 입력해주세요!');
            return;
        }
        try {
            await requestFeedback(Number(requestData.tilId), Number(myMemberId), Number(requestData.mentorId), requestData.message);
            alert('성공적으로 피드백이 요청되었습니다!');
            setIsModalOpen(false);
            setRequestData({ tilId: '', mentorId: '', message: '' });
            // TODO: 목록 갱신을 위해 fetchFeedbackList() 다시 호출
        } catch (error) {
            console.error(error);
            alert('피드백 요청 실패');
        }
    };

    const openWriteModal = (feedbackId) => {
        setSelectedFeedbackId(feedbackId);
        setIsWriteModalOpen(true);
    };

    const handleWriteSubmit = async () => {
        if (!writeData.comment) return alert('코멘트를 작성해주세요!');
        try {
            await writeFeedback(selectedFeedbackId, Number(myMemberId), Number(writeData.technicalScore), Number(writeData.flowScore), Number(writeData.designScore), writeData.comment);
            alert('피드백이 등록되었습니다!');
            setIsWriteModalOpen(false);
            setWriteData({ technicalScore: 5, flowScore: 5, designScore: 5, comment: '' });
        } catch (error) {
            console.error(error);
            alert('피드백 등록 실패');
        }
    };

    const openDetailModal = async (feedbackId) => {
        try {
            const res = await getFeedbackDetail(feedbackId);
            setDetailData(res.data || res);
            setIsDetailModalOpen(true);
        } catch (error) {
            console.error(error);
            alert("상세 정보를 불러오는데 실패했습니다.");
        }
    };

    // ==========================================
    // 🎨 화면 렌더링
    // ==========================================
    return (
        <div className="feedback-page">
            <div className="feedback-hero">
                <div className="feedback-hero-content">
                    <h2>멘토 피드백</h2>
                    <p>전문가 멘토의 꼼꼼한 코드 리뷰와 조언으로 한 단계 더 성장하세요.</p>
                </div>
                <button className="feedback-request-btn" onClick={() => setIsModalOpen(true)}>
                    + 피드백 요청하기
                </button>
            </div>

            <div className="feedback-tabs">
                <button className={`tab-btn ${activeTab === 'REQUESTED' ? 'active' : ''}`} onClick={() => setActiveTab('REQUESTED')}>내가 요청한 피드백</button>
                {isMentor && (
                    <button className={`tab-btn ${activeTab === 'RECEIVED' ? 'active' : ''}`} onClick={() => setActiveTab('RECEIVED')}>내가 받은 요청 (멘토용)</button>
                )}
            </div>

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
                                    <span className={`fb-status ${fb.status}`}>{fb.status === 'REQUESTED' ? '대기 중' : '답변 완료'}</span>
                                    <span className="fb-date">{fb.date}</span>
                                </div>
                                <h3 className="fb-post-title">{fb.postTitle}</h3>
                                <div className="fb-card-footer">
                                    <span className="fb-person">{activeTab === 'REQUESTED' ? `멘토: ${fb.mentorName}` : `요청자: ${fb.requesterName}`}</span>
                                    {activeTab === 'RECEIVED' && fb.status === 'REQUESTED' ? (
                                        // 1. 멘토 탭이면서 대기 중일 때 -> [답변하기]
                                        <button className="fb-action-btn write-btn" onClick={() => openWriteModal(fb.id)}>
                                            답변하기
                                        </button>
                                    ) : fb.status === 'COMPLETED' ? (
                                        // 2. 멘토든 일반 유저든 답변이 완료되었을 때 -> [상세 보기]
                                        <button className="fb-action-btn" onClick={() => openDetailModal(fb.id)}>
                                            상세 보기
                                        </button>
                                    ) : (
                                        // 3. 일반 유저 탭인데 아직 대기 중일 때 -> [답변 대기중] (클릭 금지 처리)
                                        <button className="fb-action-btn" disabled style={{ opacity: 0.5, cursor: 'not-allowed', backgroundColor: '#e2e8f0', color: '#64748b' }}>
                                            답변 대기중
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* 🔥 모달 1: 피드백 요청 (드롭다운 완벽 연동!) */}
            {isModalOpen && (
                <div className="feedback-modal-overlay">
                    <div className="feedback-modal-card">
                        <h3>피드백 요청하기</h3>
                        <div className="feedback-modal-form">

                            <label>피드백 받을 TIL 게시글</label>
                            <select value={requestData.tilId} onChange={(e) => setRequestData({...requestData, tilId: e.target.value})}>
                                <option value="" disabled>어떤 게시글을 리뷰 받을까요?</option>
                                {myPosts.map(post => (
                                    // 백엔드의 PostSimpleResponse 필드명인 postId와 title을 사용합니다!
                                    <option key={post.postId} value={post.postId}>
                                        {post.title}
                                    </option>
                                ))}
                            </select>

                            <label>담당 멘토 선택</label>
                            <select value={requestData.mentorId} onChange={(e) => setRequestData({...requestData, mentorId: e.target.value})}>
                                <option value="" disabled>어떤 멘토에게 요청할까요?</option>
                                {mentors.map(mentor => (
                                    // 백엔드의 MemberResponse 필드명인 memberId와 nickname을 사용합니다!
                                    <option key={mentor.memberId} value={mentor.memberId}>
                                        {mentor.nickname}
                                    </option>
                                ))}
                            </select>

                            <label>요청 메시지</label>
                            <textarea placeholder="어떤 부분을 리뷰받고 싶은지 상세히 적어주세요!" value={requestData.message} onChange={(e) => setRequestData({...requestData, message: e.target.value})} rows={4} />
                        </div>
                        <div className="feedback-modal-actions">
                            <button className="fb-btn-cancel" onClick={() => setIsModalOpen(false)}>취소</button>
                            <button className="fb-btn-submit" onClick={handleRequestSubmit}>요청 보내기</button>
                        </div>
                    </div>
                </div>
            )}

            {/* 모달 2: 멘토 답변 작성 */}
            {isWriteModalOpen && (
                <div className="feedback-modal-overlay">
                    <div className="feedback-modal-card">
                        <h3>피드백 작성하기</h3>
                        <div className="feedback-modal-form">
                            <div style={{ display: 'flex', gap: '12px' }}>
                                <div style={{ flex: 1 }}><label>기술 점수</label><input type="number" min="1" max="5" value={writeData.technicalScore} onChange={(e) => setWriteData({...writeData, technicalScore: e.target.value})} /></div>
                                <div style={{ flex: 1 }}><label>논리/흐름</label><input type="number" min="1" max="5" value={writeData.flowScore} onChange={(e) => setWriteData({...writeData, flowScore: e.target.value})} /></div>
                                <div style={{ flex: 1 }}><label>구조/설계</label><input type="number" min="1" max="5" value={writeData.designScore} onChange={(e) => setWriteData({...writeData, designScore: e.target.value})} /></div>
                            </div>
                            <label>총평</label>
                            <textarea placeholder="따뜻하고 예리한 피드백을 남겨주세요." value={writeData.comment} onChange={(e) => setWriteData({...writeData, comment: e.target.value})} rows={5} />
                        </div>
                        <div className="feedback-modal-actions">
                            <button className="fb-btn-cancel" onClick={() => setIsWriteModalOpen(false)}>취소</button>
                            <button className="fb-btn-submit" onClick={handleWriteSubmit}>등록</button>
                        </div>
                    </div>
                </div>
            )}

            {/* 모달 3: 상세 결과 보기 */}
            {isDetailModalOpen && detailData && (
                <div className="feedback-modal-overlay">
                    <div className="feedback-modal-card">
                        <h3>피드백 결과 리포트</h3>
                        <div className="feedback-detail-content" style={{ display: 'flex', flexDirection: 'column', gap: '20px', marginBottom: '32px' }}>
                            <div style={{ display: 'flex', gap: '12px', background: '#f8fafc', padding: '16px', borderRadius: '16px' }}>
                                <div style={{ flex: 1, textAlign: 'center' }}><div style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>기술 점수</div><div style={{ fontSize: '24px', fontWeight: '900', color: '#8b5cf6' }}>{detailData.technicalScore}<span style={{fontSize: '14px', color: '#94a3b8'}}>/5</span></div></div>
                                <div style={{ flex: 1, textAlign: 'center', borderLeft: '1px solid #e2e8f0', borderRight: '1px solid #e2e8f0' }}><div style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>논리/흐름</div><div style={{ fontSize: '24px', fontWeight: '900', color: '#06b6d4' }}>{detailData.flowScore}<span style={{fontSize: '14px', color: '#94a3b8'}}>/5</span></div></div>
                                <div style={{ flex: 1, textAlign: 'center' }}><div style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>구조/설계</div><div style={{ fontSize: '24px', fontWeight: '900', color: '#10b981' }}>{detailData.designScore}<span style={{fontSize: '14px', color: '#94a3b8'}}>/5</span></div></div>
                            </div>
                            <div><h4 style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#334155' }}>멘토의 총평</h4><div style={{ background: '#f1f5f9', padding: '16px', borderRadius: '12px', fontSize: '15px', color: '#1e293b', lineHeight: '1.6' }}>{detailData.comment}</div></div>
                        </div>
                        <div className="feedback-modal-actions"><button className="fb-btn-cancel" style={{ width: '100%' }} onClick={() => setIsDetailModalOpen(false)}>닫기</button></div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default FeedbackPage;