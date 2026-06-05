import { useState } from 'react';
import {
    formatDateText,
    formatMoney,
    getPaybackResultStatusLabel,
    getRefundStatusLabel,
    getRemainingDays,
    getSubscriptionStatusLabel,
    normalizeProgressRate,
} from '../../utils/mypageUtils';
import '../../styles/mypage/SubscriptionPaybackSection.css';

const SubscriptionPaybackSection = ({
                                        subscription,
                                        payback,
                                        isLoading,
                                        isActionLoading,
                                        onSubscribe,
                                        onCancel,
                                    }) => {
    const [isPaybackOpen, setIsPaybackOpen] = useState(false);

    // ACTIVE 또는 CANCEL_RESERVED 모두 구독 정보 표시
    const hasActiveSubscription = Boolean(subscription?.isActive) || subscription?.status === 'CANCEL_RESERVED';
    const isCancelReserved = subscription?.status === 'CANCEL_RESERVED';
    const hasPayback = Boolean(payback?.paybackParticipationId);

    const remainingDays = getRemainingDays(subscription?.endedAt);
    const progressRate = normalizeProgressRate(payback?.progressRate);

    const handleTogglePayback = () => {
        setIsPaybackOpen((prev) => !prev);
    };

    if (isLoading) {
        return (
            <section className="mypage-panel mypage-subscription-panel">
                <div className="mypage-subscription-loading">
                    구독 및 페이백 정보를 불러오는 중입니다.
                </div>
            </section>
        );
    }

    if (!hasActiveSubscription) {
        return (
            <section className="mypage-panel mypage-subscription-panel">
                <div className="mypage-panel-header">
                    <div>
                        <h2>구독 / 페이백</h2>
                        <p>프리미엄 구독을 시작하고 페이백 챌린지에 참여해보세요.</p>
                    </div>
                </div>

                <div className="mypage-subscription-empty-card">
                    <div className="mypage-subscription-empty-icon">P</div>

                    <div>
                        <strong>현재 활성 구독이 없습니다.</strong>
                        <p>
                            구독을 시작하면 현재 활성 페이백 정책이 자동으로 적용되고,
                            구독 기간 동안의 TIL 작성 기록으로 진행률이 계산됩니다.
                        </p>
                    </div>

                    <button
                        type="button"
                        className="mypage-subscription-primary-button"
                        onClick={onSubscribe}
                        disabled={isActionLoading}
                    >
                        {isActionLoading ? '처리 중...' : '프리미엄 구독 시작'}
                    </button>
                </div>
            </section>
        );
    }

    return (
        <section className="mypage-panel mypage-subscription-panel">
            <div className="mypage-panel-header mypage-subscription-header">
                <div>
                    <h2>구독 / 페이백</h2>
                    <p>현재 구독 회차의 페이백 진행 상태입니다.</p>
                </div>

                <span className="mypage-subscription-status-badge">
                    {getSubscriptionStatusLabel(subscription.status)}
                </span>
            </div>

            <div className="mypage-subscription-compact-bar">
                <div className="mypage-subscription-period-text">
                    <span>구독 기간</span>
                    <strong>
                        {formatDateText(subscription.startedAt)} ~ {formatDateText(subscription.endedAt)}
                    </strong>
                    {isCancelReserved
                        ? <em>만료 후 자동 갱신 없음 · 남은 기간 {remainingDays}일</em>
                        : <em>자동 갱신 · 남은 기간 {remainingDays}일</em>
                    }
                </div>

                <button
                    type="button"
                    className="mypage-subscription-cancel-button"
                    onClick={isCancelReserved ? undefined : onCancel}
                    disabled={isCancelReserved || isActionLoading}
                >
                    {isActionLoading ? '처리 중...' : isCancelReserved ? '취소됨' : '구독 취소'}
                </button>
            </div>

            <div className={`mypage-payback-card ${isPaybackOpen ? 'open' : ''}`}>
                <button
                    type="button"
                    className="mypage-payback-summary-button"
                    onClick={handleTogglePayback}
                >
                    <div className="mypage-payback-summary-left">
                        <span>페이백 챌린지</span>
                        <strong>
                            {hasPayback ? payback.policyName : '진행 중인 페이백 없음'}
                        </strong>

                        {!isPaybackOpen && hasPayback && (
                            <div className="mypage-payback-mini-progress">
                                <div
                                    className="mypage-payback-mini-progress-fill"
                                    style={{ width: `${progressRate}%` }}
                                />
                            </div>
                        )}
                    </div>

                    <div className="mypage-payback-summary-right">
                        {!isPaybackOpen && hasPayback && (
                            <strong>{progressRate.toFixed(0)}%</strong>
                        )}

                        <em>{isPaybackOpen ? '접기' : '펼치기'}</em>
                    </div>
                </button>

                {isPaybackOpen && (
                    hasPayback ? (
                        <div className="mypage-payback-detail">
                            <div className="mypage-payback-top">
                                <div>
                                    <span>이번 구독 페이백</span>
                                    <strong>{payback.policyName}</strong>
                                </div>

                                <em>{getPaybackResultStatusLabel(payback.resultStatus)}</em>
                            </div>

                            <div className="mypage-payback-progress-row">
                                <span>
                                    {payback.achievedWriteDays ?? 0} / {payback.requiredWriteDays ?? 0}일 작성
                                </span>
                                <strong>{progressRate.toFixed(0)}%</strong>
                            </div>

                            <div className="mypage-payback-progress-track">
                                <div
                                    className="mypage-payback-progress-fill"
                                    style={{ width: `${progressRate}%` }}
                                />
                            </div>

                            <div className="mypage-payback-info-row">
                                <div>
                                    <span>예상 환급액</span>
                                    <strong>{formatMoney(payback.refundAmount)}</strong>
                                </div>

                                <div>
                                    <span>환급 상태</span>
                                    <strong>{getRefundStatusLabel(payback.refundStatus)}</strong>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="mypage-payback-empty">
                            진행 중인 페이백 참여 내역이 없습니다.
                        </div>
                    )
                )}
            </div>
        </section>
    );
};

export default SubscriptionPaybackSection;
