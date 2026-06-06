import { useState, useEffect, useCallback } from 'react';
import { getWeeklyReport, generateWeeklyReport } from '../../api/weeklyReportApi.js';
import { toDateString } from '../../utils/mypageUtils.js';
import {getMemberId} from "../../utils/authUtils.js";

const memberId = getMemberId();
const getLastMonday = () => {
    const today = new Date();
    const day = today.getDay(); // 0=일, 1=월 ... 6=토
    const result = new Date(today);
    result.setDate(today.getDate() - (day === 0 ? 6 : day - 1) - 7);
    return toDateString(result);
};

// status: 'loading' | 'idle' | 'generating' | 'done' | 'error' | 'noPost'
export const useWeeklyReport = () => {
    const lastMonday = getLastMonday();
    const [report, setReport] = useState(null);
    const [status, setStatus] = useState('loading');

    useEffect(() => {
        let cancelled = false;
        setStatus('loading');

        getWeeklyReport(lastMonday)
            .then((data) => {
                if (cancelled) return;
                setReport(data);
                setStatus(data ? 'done' : 'idle');
            })
            .catch(() => {
                if (!cancelled) setStatus('idle');
            });

        return () => { cancelled = true; };
    }, [lastMonday]);

    const generateReport = useCallback(async () => {
        setStatus('generating');
        try {
            const data = await generateWeeklyReport(lastMonday);
            setReport(data);
            setStatus('done');
        } catch (err) {
            try {
                const body = JSON.parse(err.message);
                if (body?.message?.includes('TIL이 없어')) {
                    setStatus('noPost');
                    return;
                }
            } catch {}
            setStatus('error');
        }
    }, [lastMonday]);

    return { report, status, lastMonday, generateReport };
};