import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import './Header.css';

export default function Header() {
  const { session, loading, ime, slika, signIn, signOut } = useAuth();

  return (
    <header className="header">
      <Link to="/" className="header__logo">StockLab</Link>

      <div className="header__auth">
        {loading ? null : session ? (
          <>
            {slika && <img src={slika} alt="" className="header__avatar" referrerPolicy="no-referrer" />}
            <span>{ime}</span>
            <button onClick={signOut}>Odjava</button>
          </>
        ) : (
          <button onClick={signIn}>Prijava z Google</button>
        )}
      </div>
    </header>
  );
}