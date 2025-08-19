// frontend/src/lib/api.ts
import axios from "axios";

/**
 * One axios instance for the whole app.
 * - Base URL points to your Spring Boot API.
 * - withCredentials keeps the JWT cookie attached.
 */
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? "http://localhost:8080",
    withCredentials: true,
});

// Optional: small helper to unwrap .data
export async function get<T>(url: string, config?: Parameters<typeof api.get>[1]) {
    const { data } = await api.get<T>(url, config);
    return data;
}

export default api;        // <-- default export (fixes the TS2613 error)