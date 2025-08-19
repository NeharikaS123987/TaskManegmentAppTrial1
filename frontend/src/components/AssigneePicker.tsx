import { useEffect, useState } from "react";
import  api  from "../lib/api";

type User = { id: number; name: string; email: string };

export default function AssigneePicker({
                                           boardId,
                                           selected,
                                           onChange,
                                       }: {
    boardId: number;
    selected: number[];
    onChange: (ids: number[]) => void;
}) {
    const [users, setUsers] = useState<User[]>([]);
    const [ids, setIds] = useState<number[]>(selected ?? []);

    // keep local state in sync with parent
    useEffect(() => {
        setIds(selected ?? []);
    }, [selected]);

    // load board members
    useEffect(() => {
        (async () => {
            try {
                // BoardMemberResponse: { userId, name, email, role }
                const r = await api.get<
                    { userId: number; name: string; email: string; role: string }[]
                >(`/api/boards/${boardId}/members`);
                setUsers(r.data.map(m => ({ id: m.userId, name: m.name, email: m.email })));
            } catch {
                // Optional admin fallback if you exposed /api/users
                try {
                    const r2 = await api.get<User[]>("/api/users");
                    setUsers(r2.data);
                } catch {
                    setUsers([]);
                }
            }
        })();
    }, [boardId]);

    function toggle(id: number) {
        const next = ids.includes(id) ? ids.filter(x => x !== id) : [...ids, id];
        setIds(next);
        onChange(next);
    }

    return (
        <div className="flex flex-wrap gap-2">
            {users.map(u => (
                <button
                    key={u.id}
                    type="button"
                    onClick={() => toggle(u.id)}
                    className={`text-xs rounded px-2 py-1 border ${
                        ids.includes(u.id)
                            ? "bg-blue-600 text-white border-blue-600"
                            : "bg-white text-gray-700"
                    }`}
                    title={u.email}
                >
                    {u.name}
                </button>
            ))}
            {users.length === 0 && (
                <span className="text-xs text-gray-500">
          No members found for this board.
        </span>
            )}
        </div>
    );
}