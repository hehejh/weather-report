import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import AlertRuleForm from '../components/AlertRuleForm';
import { listAlerts, createAlert, updateAlert, deleteAlert, testAlert } from '../api/alerts';
import type { AlertRule, AlertRuleRequest } from '../types';

export default function AlertManagePage() {
  const { id: spotIdStr } = useParams<{ id: string }>();
  const spotId = spotIdStr ? Number(spotIdStr) : null;

  const [alerts, setAlerts] = useState<AlertRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingAlert, setEditingAlert] = useState<AlertRule | null>(null);
  const [testResult, setTestResult] = useState<string | null>(null);

  const fetchAlerts = useCallback(async () => {
    if (!spotId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await listAlerts(spotId);
      setAlerts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取提醒列表失败');
    } finally {
      setLoading(false);
    }
  }, [spotId]);

  useEffect(() => {
    fetchAlerts();
  }, [fetchAlerts]);

  const handleCreate = async (data: AlertRuleRequest) => {
    if (!spotId) return;
    try {
      const rule = await createAlert(spotId, data);
      setAlerts((prev) => [rule, ...prev]);
      setShowForm(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建提醒失败');
    }
  };

  const handleUpdate = async (data: AlertRuleRequest) => {
    if (!editingAlert) return;
    try {
      const updated = await updateAlert(editingAlert.id, data);
      setAlerts((prev) => prev.map((a) => (a.id === updated.id ? updated : a)));
      setEditingAlert(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : '更新提醒失败');
    }
  };

  const handleDelete = async (alertId: number) => {
    if (!confirm('确定删除此提醒规则？')) return;
    try {
      await deleteAlert(alertId);
      setAlerts((prev) => prev.filter((a) => a.id !== alertId));
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除提醒失败');
    }
  };

  const handleTest = async (alertId: number) => {
    try {
      const result = await testAlert(alertId);
      setTestResult(result
        ? `测试成功 — 评分: ${result.score}, 已发送: ${result.sent ? '是' : '否'}`
        : '测试完成，未触发（天气条件不满足阈值）');
    } catch (err) {
      setTestResult(err instanceof Error ? err.message : '测试失败');
    }
  };

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
          <Link to={`/spot/${spotId}`} className="btn-ghost text-sm">
            ← 返回
          </Link>
          <h1 className="font-semibold flex-1">天气提醒设置</h1>
          <button onClick={() => setShowForm(true)} className="btn-primary text-sm">
            + 新建
          </button>
        </div>
      </div>

      <div className="max-w-lg mx-auto px-4 pt-4 space-y-4">
        {error && (
          <div className="bg-accent-bad/10 border border-accent-bad/20 rounded-xl p-3">
            <p className="text-sm text-accent-bad">{error}</p>
            <button onClick={() => setError(null)} className="text-xs text-text-secondary mt-1">关闭</button>
          </div>
        )}

        {testResult && (
          <div className="bg-surface-card border border-white/5 rounded-xl p-3">
            <p className="text-sm">{testResult}</p>
            <button onClick={() => setTestResult(null)} className="text-xs text-text-secondary mt-1">关闭</button>
          </div>
        )}

        {showForm && (
          <AlertRuleForm
            onSubmit={handleCreate}
            onCancel={() => setShowForm(false)}
          />
        )}

        {editingAlert && (
          <AlertRuleForm
            initial={{
              alertType: editingAlert.alertType,
              pushTime: editingAlert.pushTime,
              glowProbability: 70,
            }}
            onSubmit={handleUpdate}
            onCancel={() => setEditingAlert(null)}
          />
        )}

        {loading ? (
          <div className="text-center py-8 text-text-muted">加载中...</div>
        ) : alerts.length === 0 && !showForm ? (
          <div className="text-center py-12 text-text-muted">
            <p className="text-4xl mb-2">🔔</p>
            <p className="text-sm">还没有设置提醒规则</p>
            <button onClick={() => setShowForm(true)} className="btn-primary text-sm mt-3">
              创建第一条提醒
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            {alerts.map((alert) => (
              <div
                key={alert.id}
                className="bg-surface-card rounded-xl p-4 border border-white/5 space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <span className="text-sm font-medium">
                      {alert.alertType === 'GLOW_PROBABILITY' ? '霞光概率' : alert.alertType}
                    </span>
                    <span className="text-xs text-text-muted ml-2">
                      推送时间: {alert.pushTime}
                    </span>
                  </div>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${
                    alert.enabled ? 'bg-accent-good/15 text-accent-good' : 'bg-white/5 text-text-muted'
                  }`}>
                    {alert.enabled ? '已启用' : '已禁用'}
                  </span>
                </div>

                {alert.thresholds && (
                  <div className="text-xs text-text-muted font-mono">
                    {alert.thresholds}
                  </div>
                )}

                <div className="flex gap-2">
                  <button
                    onClick={() => setEditingAlert(alert)}
                    className="btn-ghost text-xs"
                  >
                    编辑
                  </button>
                  <button
                    onClick={() => handleTest(alert.id)}
                    className="btn-ghost text-xs"
                  >
                    测试
                  </button>
                  <button
                    onClick={() => handleDelete(alert.id)}
                    className="btn-ghost text-xs text-accent-bad"
                  >
                    删除
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
