const StatCard = ({ icon, label, value, unit, description, tone }) => {
    return (
        <article className="mypage-stat-card">
            <div className={`mypage-stat-icon ${tone}`}>{icon}</div>

            <div className="mypage-stat-info">
                <p>{label}</p>

                <strong>
                    {value}
                    <span>{unit}</span>
                </strong>

                <small>{description}</small>
            </div>
        </article>
    );
};

export default StatCard;
