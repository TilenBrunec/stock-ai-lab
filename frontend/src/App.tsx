
import { BrowserRouter, Route, Routes } from 'react-router-dom';


function StockListPage() { return <h1>Instrumenti</h1>; }        // pride v #14
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