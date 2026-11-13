import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './hooks/useAuth';
import Header from './components/Header';
import StockListPage from './pages/StockListPage';
import StockDetailPage from './pages/StockDetailPage';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="/" element={<StockListPage />} />
          <Route path="/stocks/:id" element={<StockDetailPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}