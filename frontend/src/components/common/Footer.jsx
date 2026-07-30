import "../../styles/common/Footer.css";

const Footer = () => {
    return (
        <footer className="app-footer">
            <div className="app-footer-icon">★</div>

            <div>
                <strong>꾸준함이 최고의 실력입니다!</strong>
                <p>오늘도 기록하는 당신을 응원합니다. 작은 기록이 성장의 증거가 됩니다.</p>
            </div>

            <div className="app-footer-flower" aria-hidden="true">
                <span />
                <span />
                <span />
                <span />
                <i />
            </div>
        </footer>
    );
};

export default Footer;
