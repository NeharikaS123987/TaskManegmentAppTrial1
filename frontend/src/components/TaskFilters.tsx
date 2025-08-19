import { useState } from "react";

export type Filters = {
    q?: string;
    status?: "TODO" | "IN_PROGRESS" | "DONE" | "";
    dueFrom?: string;
    dueTo?: string;
    assigneeId?: string;
};

export default function TaskFilters({ value, onChange }: {
    value: Filters;
    onChange: (f: Filters) => void;
}) {
    const [local, setLocal] = useState<Filters>(value);

    function commit(next: Partial<Filters>) {
        const merged = { ...local, ...next };
        setLocal(merged);
        onChange(merged);
    }

    return (
        <div className="bg-white p-3 rounded shadow flex flex-wrap gap-2">
            <input className="border rounded px-2 py-1" placeholder="Search title/description"
                   value={local.q ?? ""} onChange={e=>commit({ q: e.target.value })} />
            <select className="border rounded px-2 py-1" value={local.status ?? ""} onChange={e=>commit({ status: e.target.value as Filters["status"] })}>
                <option value="">Any status</option>
                <option value="TODO">To-Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="DONE">Done</option>
            </select>
            <input type="date" className="border rounded px-2 py-1"
                   value={local.dueFrom ?? ""} onChange={e=>commit({ dueFrom: e.target.value })} />
            <span className="text-gray-500 text-sm self-center">to</span>
            <input type="date" className="border rounded px-2 py-1"
                   value={local.dueTo ?? ""} onChange={e=>commit({ dueTo: e.target.value })} />
            <input className="border rounded px-2 py-1" placeholder="Assignee userId"
                   value={local.assigneeId ?? ""} onChange={e=>commit({ assigneeId: e.target.value })} />
            <button className="ml-auto text-xs text-gray-600 hover:text-gray-900"
                    onClick={()=>{ setLocal({}); onChange({}); }}>Clear</button>
        </div>
    );
}