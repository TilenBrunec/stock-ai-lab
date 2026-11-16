import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './hooks/useAuth';
import Header from './components/Header';
import StockListPage from './pages/StockListPage';
import StockDetailPage from './pages/StockDetailPage';
import SettingsPage from './pages/SettingsPage';
import NewExperimentPage from './pages/NewExperimentPage';
import ExperimentPage from './pages/ExperimentPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="/" element={<StockListPage />} />
          <Route path="/stocks/:id" element={<StockDetailPage />} />
          <Route path="/nastavitve" element={<SettingsPage />} />
          <Route path="/eksperimenti/nov" element={<NewExperimentPage />} />
          <Route path="/eksperimenti/:id" element={<ExperimentPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
