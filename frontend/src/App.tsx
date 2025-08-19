import { Route, Routes, Navigate } from "react-router-dom";
import Login from "/Users/neha/Desktop/TaskMangementApp/frontend/src/pages/ Login.tsx";
import Boards from "./pages/Boards";
import BoardDetail from "/Users/neha/Desktop/TaskMangementApp/frontend/src/pages/ BoardDetail.tsx";
import NotFound from "./pages/NotFound";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";
import Members from "./pages/Members";
import AdminAnalytics from "./pages/AdminAnalytics";

export default function App() {
    return (
        <div className="min-h-screen bg-gray-50 text-gray-900">
            <Navbar />
            <main className="mx-auto max-w-6xl p-4">
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/" element={<ProtectedRoute><Navigate to="/boards" replace /></ProtectedRoute>} />
                    <Route path="/boards" element={<ProtectedRoute><Boards /></ProtectedRoute>} />
                    <Route path="/boards/:id" element={<ProtectedRoute><BoardDetail /></ProtectedRoute>} />
                    <Route path="*" element={<NotFound />} />
                    <Route path="/boards/:id/members" element={<ProtectedRoute><Members /></ProtectedRoute>} />
                    <Route path="/admin" element={<ProtectedRoute><AdminAnalytics /></ProtectedRoute>} />
                </Routes>
            </main>
        </div>
    );
}