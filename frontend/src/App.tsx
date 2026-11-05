import { BrowserRouter, Route, Routes } from 'react-router-dom';
import StockListPage from './pages/StockListPage';

function StockDetailPage() { return <h1>Instrument</h1>; }       // pride v #15

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