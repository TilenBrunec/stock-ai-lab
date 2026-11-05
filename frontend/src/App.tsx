import { BrowserRouter, Route, Routes } from 'react-router-dom';
import StockListPage from './pages/StockListPage';
import StockDetailPage from './pages/StockDetailPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<StockListPage />} />
        <Route path="/stocks/:id" element={<StockDetailPage />} />
      </Routes>
    </BrowserRouter>
  );
}