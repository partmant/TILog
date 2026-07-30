import {
    useCallback,
    useEffect,
    useState,
} from 'react';
import { useNavigate } from 'react-router-dom';
import {
    cancelSubscription,
    getCurrentPaybackParticipation,
    getMySubscriptionStatus,
    resumeSubscription,
    subscribePremium,
} from '../../api/subscriptionPaybackApi';
import { isLoggedIn } from '../../utils/authUtils';

export const useSubscriptionPayback = () => {
    const navigate = useNavigate();

    const [subscription, setSubscription] = useState(null);
    const [payback, setPayback] = useState(null);
    const [isSubscriptionLoading, setIsSubscriptionLoading] = useState(true);
    const [isSubscriptionActionLoading, setIsSubscriptionActionLoading] = useState(false);

    const loadSubscriptionAndPayback = useCallback(async () => {
        const subscriptionResponse = await getMySubscriptionStatus();

        const hasValidSubscription =
            subscriptionResponse?.isActive ||
            subscriptionResponse?.status === 'CANCEL_RESERVED';

        if (!hasValidSubscription) {
            return {
                subscriptionResponse,
                paybackResponse: null,
            };
        }

        try {
            const paybackResponse = await getCurrentPaybackParticipation();

            return {
                subscriptionResponse,
                paybackResponse,
            };
        } catch (error) {
            console.error('[PAYBACK API ERROR]', error);

            return {
                subscriptionResponse,
                paybackResponse: null,
            };
        }
    }, []);

    const applySubscriptionAndPayback = useCallback(({
                                                         subscriptionResponse,
                                                         paybackResponse,
                                                     }) => {
        setSubscription(subscriptionResponse);
        setPayback(paybackResponse);
    }, []);

    const refreshSubscriptionAndPayback = useCallback(async () => {
        const result = await loadSubscriptionAndPayback();
        applySubscriptionAndPayback(result);
    }, [loadSubscriptionAndPayback, applySubscriptionAndPayback]);

    const handleSubscribe = async () => {
        try {
            setIsSubscriptionActionLoading(true);
            setIsSubscriptionLoading(true);

            await subscribePremium();
            await refreshSubscriptionAndPayback();
        } catch (error) {
            console.error('[SUBSCRIBE API ERROR]', error);
            alert(error.message ?? '구독 신청에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
            setIsSubscriptionLoading(false);
        }
    };

    const handleCancelSubscription = async () => {
        const isConfirmed = window.confirm('구독을 취소하시겠습니까?');

        if (!isConfirmed) {
            return;
        }

        try {
            setIsSubscriptionActionLoading(true);
            setIsSubscriptionLoading(true);

            await cancelSubscription();
            await refreshSubscriptionAndPayback();
        } catch (error) {
            console.error('[SUBSCRIPTION CANCEL API ERROR]', error);
            alert(error.message ?? '구독 취소에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
            setIsSubscriptionLoading(false);
        }
    };

    const handleResumeSubscription = async () => {
        try {
            setIsSubscriptionActionLoading(true);
            setIsSubscriptionLoading(true);

            await resumeSubscription();
            await refreshSubscriptionAndPayback();
        } catch (error) {
            console.error('[SUBSCRIPTION RESUME API ERROR]', error);
            alert(error.message ?? '구독 재개에 실패했습니다.');
        } finally {
            setIsSubscriptionActionLoading(false);
            setIsSubscriptionLoading(false);
        }
    };

    useEffect(() => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }

        let isMounted = true;

        const fetchSubscriptionAndPayback = async () => {
            try {
                const result = await loadSubscriptionAndPayback();

                if (!isMounted) {
                    return;
                }

                applySubscriptionAndPayback(result);
            } catch (error) {
                if (!isMounted) {
                    return;
                }

                console.error('[SUBSCRIPTION API ERROR]', error);
                setSubscription(null);
                setPayback(null);
            } finally {
                if (isMounted) {
                    setIsSubscriptionLoading(false);
                }
            }
        };

        fetchSubscriptionAndPayback();

        return () => {
            isMounted = false;
        };
    }, [
        navigate,
        loadSubscriptionAndPayback,
        applySubscriptionAndPayback,
    ]);

    return {
        subscription,
        payback,
        isSubscriptionLoading,
        isSubscriptionActionLoading,
        handleSubscribe,
        handleCancelSubscription,
        handleResumeSubscription,
    };
};
