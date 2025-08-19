import { Link, useNavigate, useLocation } from "react-router-dom";
import { isLoggedIn, clearToken } from "../lib/auth";

function NavLink({ to, children }: { to: string; children: React.ReactNode }) {
    const loc = useLocation();
    const active = loc.pathname.startsWith(to);
    return (
        <Link to={to} className={active ? "text-blue-600 font-medium" : "text-gray-700 hover:text-gray-900"}>
            {children}
        </Link>
    );
}

export default function Navbar() {
    const nav = useNavigate();
    const logged = isLoggedIn();

    return (
        <header className="bg-white shadow-sm">
            <div className="mx-auto max-w-6xl flex items-center justify-between p-4">
                <Link to="/" className="font-semibold">Task Manager</Link>
                <nav className="flex items-center gap-5">
                    {logged && (
                        <>
                            <NavLink to="/boards">Boards</NavLink>
                            <NavLink to="/admin">Admin</NavLink>
                        </>
                    )}
                    {logged ? (
                        <button
                            onClick={() => { clearToken(); nav("/login"); }}
                            className="rounded bg-gray-100 px-3 py-1 hover:bg-gray-200"
                        >
                            Logout
                        </button>
                    ) : (
                        <Link to="/login" className="rounded bg-blue-600 px-3 py-1 text-white">Login</Link>
                    )}
                </nav>
            </div>
        </header>
    );
}