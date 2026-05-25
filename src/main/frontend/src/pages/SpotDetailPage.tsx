import { useParams, Link } from 'react-router-dom';
import WeatherDashboard from '../components/WeatherDashboard';
import GoldenHourTimeline from '../components/GoldenHourTimeline';
import SevenDayForecast from '../components/SevenDayForecast';
import { useWeatherData } from '../hooks/useWeatherData';

export default function SpotDetailPage() {
  const { id } = useParams<{ id: string }>();
  const spotId = id ? Number(id) : null;
  const { dashboard, forecast, loading, error, refresh } = useWeatherData(spotId);

  if (!spotId || isNaN(spotId)) {
    return (
      <div className="min-h-[100dvh] flex items-center justify-center">
        <p className="text-text-muted">无效的地点 ID</p>
      </div>
    );
  }

  return (
    <div className="min-h-[100dvh] pb-8">
      {/* Header */}
      <div className="sticky top-0 z-50 bg-surface/95 backdrop-blur border-b border-white/5">
        <div className="flex items-center gap-3 px-4 py-3">
          <Link to="/" className="btn-ghost text-sm">
            ← 返回
          </Link>
          <h1 className="font-semibold flex-1 truncate">
            {dashboard?.spotName ?? '加载中...'}
          </h1>
          <button onClick={refresh} className="btn-ghost text-sm" title="刷新天气数据">
            🔄
          </button>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 pt-4 space-y-6">
        {loading && !dashboard && (
          <div className="text-center py-12">
            <div className="animate-pulse-soft text-4xl mb-3">⏳</div>
            <p className="text-text-secondary">正在获取天气数据...</p>
          </div>
        )}

        {error && (
          <div className="bg-accent-bad/10 border border-accent-bad/20 rounded-xl p-4 text-center">
            <p className="text-sm text-accent-bad">{error}</p>
            <button onClick={refresh} className="btn-secondary text-sm mt-2">重试</button>
          </div>
        )}

        {dashboard && (
          <>
            <WeatherDashboard
              photographyIndex={dashboard.photographyIndex}
              indexLabel={dashboard.indexLabel}
              current={dashboard.current}
              solar={dashboard.solar}
              glow={dashboard.glow}
              spotName={dashboard.spotName}
            />
            <GoldenHourTimeline solar={dashboard.solar} />
            {forecast?.weekForecast && <SevenDayForecast forecast={forecast.weekForecast} />}
          </>
        )}

        {/* Alert rules link */}
        {dashboard && (
          <Link
            to={`/spot/${spotId}/alerts`}
            className="btn-secondary w-full text-center block"
          >
            🔔 管理天气提醒
          </Link>
        )}
      </div>
    </div>
  );
}
