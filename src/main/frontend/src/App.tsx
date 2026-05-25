import { BrowserRouter, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import SpotDetailPage from './pages/SpotDetailPage';
import AlertManagePage from './pages/AlertManagePage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/spot/:id" element={<SpotDetailPage />} />
        <Route path="/spot/:id/alerts" element={<AlertManagePage />} />
      </Routes>
    </BrowserRouter>
  );
}
