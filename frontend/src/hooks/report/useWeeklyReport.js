import { useState, useEffect, useCallback, useRef } from 'react';
import { getWeeklyReport, generateWeeklyReport } from '../../api/weeklyReportApi.js';
import { toDateString } from '../../utils/mypageUtils.js';
import { getMemberId } from "../../utils/authUtils.js";

const GENERATING_KEY = 'til_report_generating';
const POLL_INTERVAL_MS = 4000;
const POLL_MAX_ATTEMPTS = 75; // 4초 × 75 = 5분

const getLastMonday = () => {
    const today = new Date();
    const day = today.getDay();
    const result = new Date(today);
    result.setDate(today.getDate() - (day === 0 ? 6 : day - 1) - 7);
    return toDateString(result);
};

export const saveGeneratingFlag = (memberId, weekStart) => {
    localStorage.setItem(GENERATING_KEY, JSON.stringify({
        memberId,
        weekStart,
        expiresAt: Date.now() + 5 * 60 * 1000, // 5분 만료
    }));
};

const loadGeneratingFlag = (memberId, weekStart) => {
    try {
        const stored = localStorage.getItem(GENERATING_KEY);
        if (!stored) return false;
        const { memberId: mid, weekStart: ws, expiresAt } = JSON.parse(stored);
        if (mid === memberId && ws === weekStart && Date.now() < expiresAt) return true;
    } catch { /* ignore */ }
    localStorage.removeItem(GENERATING_KEY);
    return false;
};

export const clearGeneratingFlag = () => localStorage.removeItem(GENERATING_KEY);

// status: 'loading' | 'idle' | 'generating' | 'done' | 'error' | 'noPost'
export const useWeeklyReport = () => {
    const lastMonday = getLastMonday();
    const memberId = getMemberId();
    const [report, setReport] = useState(null);
    const [status, setStatus] = useState('loading');

    const pollRef = useRef(null);
    const pollCountRef = useRef(0);

    const stopPolling = useCallback(() => {
        if (pollRef.current) {
            clearInterval(pollRef.current);
            pollRef.current = null;
        }
        pollCountRef.current = 0;
    }, []);

    const startPolling = useCallback(() => {
        stopPolling();
        pollCountRef.current = 0;
        pollRef.current = setInterval(async () => {
            pollCountRef.current += 1;

            // 최대 시도 횟수 초과 시 포기
            if (pollCountRef.current > POLL_MAX_ATTEMPTS) {
                clearGeneratingFlag();
                setStatus('error');
                stopPolling();
                return;
            }

            try {
                const data = await getWeeklyReport(lastMonday);
                if (data) {
                    setReport(data);
                    setStatus('done');
                    clearGeneratingFlag();
                    stopPolling();
                }
            } catch { /* 일시적 오류 무시하고 계속 폴링 */ }
        }, POLL_INTERVAL_MS);
    }, [lastMonday, stopPolling]);

    // 마운트 시 초기 상태 결정
    useEffect(() => {
        let cancelled = false;
        setStatus('loading');
        stopPolling();

        getWeeklyReport(lastMonday)
            .then((data) => {
                if (cancelled) return;
                if (data) {
                    setReport(data);
                    setStatus('done');
                    clearGeneratingFlag();
                } else if (loadGeneratingFlag(memberId, lastMonday)) {
                    // 이전에 생성 중이었음 — 폴링으로 완료 감지
                    setStatus('generating');
                    startPolling();
                } else {
                    setStatus('idle');
                }
            })
            .catch(() => {
                if (!cancelled) setStatus('idle');
            });

        return () => {
            cancelled = true;
            stopPolling();
        };
    }, [lastMonday, memberId, startPolling, stopPolling]);

    const generateReport = useCallback(async () => {
        setStatus('generating');
        saveGeneratingFlag(memberId, lastMonday);
        startPolling(); // 페이지 이탈 대비 폴링 동시 시작

        try {
            const data = await generateWeeklyReport(lastMonday);
            setReport(data);
            setStatus('done');
            clearGeneratingFlag();
            stopPolling();
        } catch (err) {
            clearGeneratingFlag();
            stopPolling();
            try {
                const body = JSON.parse(err.message);
                if (body?.message?.includes('TIL이 없어')) {
                    setStatus('noPost');
                    return;
                }
            } catch { /* ignore */ }
            setStatus('error');
        }
    }, [lastMonday, memberId, startPolling, stopPolling]);

    return { report, status, lastMonday, generateReport };
};
