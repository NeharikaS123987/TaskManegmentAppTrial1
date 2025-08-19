import { isLoggedIn } from "../lib/auth";
import { Navigate, useLocation } from "react-router-dom";

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
    const ok = isLoggedIn();
    const loc = useLocation();
    if (!ok) return <Navigate to="/login" replace state={{ from: loc }} />;
    return <>{children}</>;
}