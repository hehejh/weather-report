import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

const HomePage = lazy(() => import('./pages/HomePage'));
const SpotDetailPage = lazy(() => import('./pages/SpotDetailPage'));
const AlertManagePage = lazy(() => import('./pages/AlertManagePage'));

function Loading() {
  return (
    <div className="min-h-[100dvh] flex items-center justify-center bg-surface">
      <div className="text-center">
        <div className="animate-pulse-soft text-4xl mb-3">📸</div>
        <p className="text-text-secondary text-sm">加载中...</p>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<Loading />}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/spot/:id" element={<SpotDetailPage />} />
          <Route path="/spot/:id/alerts" element={<AlertManagePage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
