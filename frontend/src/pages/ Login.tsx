import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../lib/api";
import { setToken } from "../lib/auth";
import type { AxiosError } from "axios";

type RouterState = { from?: { pathname?: string } } | null;

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const nav = useNavigate();
  const loc = useLocation();

  const go = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setErr(null);
    try {
      const res = await api.post("/api/auth/login", { email, password });
      setToken(res.data.accessToken);
      const from = (loc.state as RouterState)?.from?.pathname ?? "/boards";
      nav(from, { replace: true });
    } catch (ex: unknown) {
      const err = ex as AxiosError<{ message?: string }>; // type-safe narrowing
      const message =
        err.response?.data?.message ??
        err.message ??
        "Login failed";
      setErr(message);
    }
  };

  return (
    <div className="mx-auto max-w-md mt-16 bg-white p-6 rounded shadow">
      <h1 className="text-xl font-semibold mb-4">Login</h1>
      <form onSubmit={go} className="space-y-3">
        <input
          className="w-full border rounded px-3 py-2"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          className="w-full border rounded px-3 py-2"
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {err && <div className="text-red-600 text-sm">{err}</div>}
        <button className="w-full bg-blue-600 text-white rounded px-3 py-2">
          Sign in
        </button>
      </form>
      <p className="text-sm text-gray-500 mt-3">
        No account yet? Use Postman/curl once:
        <code className="ml-1">POST /api/auth/signup</code>
      </p>
    </div>
  );
}