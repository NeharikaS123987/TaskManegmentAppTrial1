import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { AxiosError } from "axios";
import api from "../lib/api";
import type { Board } from "../lib/types";

function messageFromError(err: unknown): string {
    if (typeof err === "string") return err;
    if (typeof err === "object" && err !== null) {
        const ae = err as AxiosError<{ message?: string }>;
        const data = ae.response?.data as { message?: string } | undefined;
        if (data && typeof data.message === "string" && data.message.length > 0) {
            return data.message;
        }
        if (ae.message) return ae.message;
    }
    return "Unknown error";
}

async function fetchBoards(): Promise<Board[]> {
    const { data } = await api.get("/api/boards");
    return data;
}

async function createBoard(payload: { name: string; description?: string }) {
    const { data } = await api.post("/api/boards", payload);
    return data;
}

export default function Boards() {
    const qc = useQueryClient();
    const { data, isLoading, isError, error } = useQuery({
        queryKey: ["boards"],
        queryFn: fetchBoards,
    });

    const [openForm, setOpenForm] = useState(false);
    const [boardName, setBoardName] = useState("");
    const [description, setDescription] = useState("");

    const createMut = useMutation({
        mutationFn: createBoard,
        onSuccess: async () => {
            setBoardName("");
            setDescription("");
            setOpenForm(false);
            await qc.invalidateQueries({ queryKey: ["boards"] });
        },
    });

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!boardName.trim()) return;
        await createMut.mutateAsync({ name: boardName.trim(), description: description.trim() || undefined });
    };

    if (isLoading) return <div>Loading boards…</div>;

    if (isError) {
        // common causes: 401 (not logged in), 403 (no permission), CORS, wrong API URL
        // Look in DevTools → Network for the exact status
        return (
            <div>
                <div className="text-red-600 font-semibold">Could not load boards.</div>
                <pre className="text-xs opacity-70">{messageFromError(error)}</pre>
            </div>
        );
    }

    const boards = data ?? [];

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold">Your Boards</h1>
                <button
                    onClick={() => setOpenForm((v) => !v)}
                    className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                >
                    {openForm ? "Cancel" : "Create Board"}
                </button>
            </div>

            {openForm && (
                <form onSubmit={onSubmit} className="rounded border bg-white p-4 shadow-sm max-w-lg space-y-3">
                    <div>
                        <label className="block text-sm font-medium">Board name</label>
                        <input
                            className="mt-1 w-full rounded border px-3 py-2"
                            value={boardName}
                            onChange={(e) => setBoardName(e.target.value)}
                            placeholder="e.g., Website Launch"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium">Description (optional)</label>
                        <textarea
                            className="mt-1 w-full rounded border px-3 py-2"
                            rows={3}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Short summary or goals for this board"
                        />
                    </div>
                    <div className="flex gap-3">
                        <button
                            type="submit"
                            className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 disabled:opacity-60"
                            disabled={createMut.isPending}
                        >
                            {createMut.isPending ? "Creating…" : "Create"}
                        </button>
                        {createMut.isError && (
                            <div className="text-sm text-red-600">Failed: {messageFromError(createMut.error)}</div>
                        )}
                    </div>
                </form>
            )}

            {boards.length === 0 ? (
                <div className="rounded border bg-white p-6 text-center shadow-sm">
                    <div className="text-lg font-medium">No boards yet</div>
                    <div className="mt-1 text-sm text-gray-600">Create your first board to get started.</div>
                </div>
            ) : (
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {boards.map((b) => (
                        <Link
                            key={b.id}
                            to={`/boards/${b.id}`}
                            className="rounded border bg-white p-4 shadow-sm hover:shadow"
                        >
                            <div className="flex items-start justify-between">
                                <div className="text-lg font-semibold">{b.name}</div>
                                {b.role && (
                                    <span className="rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-700">{b.role}</span>
                                )}
                            </div>
                            {b.description && (
                                <div className="mt-1 line-clamp-3 text-sm text-gray-600">{b.description}</div>
                            )}
                            <div className="mt-3 text-xs text-gray-500">
                                {b.listsCount ?? 0} lists • {b.tasksCount ?? 0} tasks
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}