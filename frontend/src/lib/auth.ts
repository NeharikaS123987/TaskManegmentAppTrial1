import { jwtDecode } from "jwt-decode";

const TOKEN_KEY = "tm_token";

type JWTPayload = {
  exp?: number;
  [key: string]: unknown;
};

export function setToken(token: string) { localStorage.setItem(TOKEN_KEY, token); }
export function getToken(): string | null { return localStorage.getItem(TOKEN_KEY); }
export function clearToken() { localStorage.removeItem(TOKEN_KEY); }

export function isLoggedIn(): boolean {
    const t = getToken();
    if (!t) return false;
    try {
        const dec = jwtDecode<JWTPayload>(t);
        if (!dec?.exp) return true;
        return Date.now() < dec.exp * 1000;
    } catch {
        return false;
    }
}