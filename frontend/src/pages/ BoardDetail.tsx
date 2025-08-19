import { useParams, Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import type { UseQueryResult } from "@tanstack/react-query";
import  api  from "../lib/api";
import Spinner from "../components/Spinner";
import EmptyState from "../components/EmptyState";
import type { BoardList, Task, Activity, TaskStatus } from "../lib/types.ts";
import { subscribeBoardEvents } from "../lib/sse";
import TaskFilters, { type Filters } from "../components/TaskFilters";
import dayjs from "dayjs";

export default function BoardDetail() {
    const { id } = useParams();
    const boardId = Number(id);
    const qc = useQueryClient();
    const [filters, setFilters] = useState<Filters>({});

    // ---- lists
    const lists = useQuery({
        queryKey: ["lists", boardId],
        queryFn: async () => (await api.get<BoardList[]>(`/api/boards/${boardId}/lists`)).data,
    });

    // ---- activity
    const activity = useQuery({
        queryKey: ["activity", boardId],
        queryFn: async () => (await api.get<Activity[]>(`/api/boards/${boardId}/activity`)).data,
        staleTime: 10_000,
    });

    // subscribe to SSE events to auto-refresh tasks/lists/activity
    const apiBase = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
    useEffect(() => {
        if (!boardId) return;
        return subscribeBoardEvents(apiBase, boardId, () => {
            // refresh everything on board events
            void qc.invalidateQueries({ queryKey: ["activity", boardId] });
            void qc.invalidateQueries({ queryKey: ["lists", boardId] });
            // tasks queries are keyed per list; just nuke all 'tasks' to keep it easy
            void qc.invalidateQueries({ predicate: q => Array.isArray(q.queryKey) && q.queryKey[0] === "tasks" });
        });
    }, [boardId, apiBase, qc]);

    // ---- create list
    const [newListName, setNewListName] = useState("");
    const createList = useMutation({
        mutationFn: async () => (await api.post(`/api/boards/${boardId}/lists`, { name: newListName })).data,
        onSuccess: () => { void qc.invalidateQueries({ queryKey: ["lists", boardId] }); setNewListName(""); }
    });

    // utility: hook to load tasks of a list
    const useTasks = (listId: number) =>
        useQuery({
            queryKey: ["tasks", listId],
            queryFn: async () => (await api.get<Task[]>(`/api/lists/${listId}/tasks`)).data,
        });

    // create task
    const createTask = async (listId: number, title: string) => {
        await api.post(`/api/lists/${listId}/tasks`, { title });
        void qc.invalidateQueries({ queryKey: ["tasks", listId] });
    };

    // update task (status/title)
    const updateTask = async (listId: number, taskId: number, patch: Partial<Task>) => {
        await api.put(`/api/lists/${listId}/tasks/${taskId}`, {
            title: patch.title ?? undefined,
            description: patch.description ?? undefined,
            status: patch.status as TaskStatus | undefined,
            dueDate: patch.dueDate ?? undefined
        });
        void qc.invalidateQueries({ queryKey: ["tasks", listId] });
    };

    // move task to another list (backend route from your screenshot)
    const moveTask = async (fromListId: number, taskId: number, targetListId: number) => {
        await api.post(`/api/lists/${fromListId}/tasks/${taskId}/move/${targetListId}`);
        void qc.invalidateQueries({ queryKey: ["tasks", fromListId] });
        void qc.invalidateQueries({ queryKey: ["tasks", targetListId] });
    };

    if (lists.isLoading) return <Spinner />;

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h1 className="text-xl font-semibold">Board {boardId}</h1>
              <div className="flex gap-3">
                <Link to={`/boards/${boardId}/members`} className="text-blue-600 text-sm">Members</Link>
              </div>
            </div>

            {/* create list */}
            <div className="bg-white p-4 rounded shadow space-y-3">
                <div className="font-medium">Add List</div>
                <div className="flex gap-2">
                    <input className="border rounded px-3 py-2 flex-1" value={newListName}
                           onChange={e=>setNewListName(e.target.value)} placeholder="List name" />
                    <button onClick={()=>createList.mutate()} className="bg-blue-600 text-white rounded px-3 py-2">
                        Add
                    </button>
                </div>
            </div>

            <TaskFilters value={filters} onChange={setFilters} />

            {/* lists & tasks */}
            {lists.data?.length ? (
                <div className="grid md:grid-cols-3 gap-4">
                    {lists.data.map((l) => (
                        <ListColumn
                            key={l.id}
                            list={l}
                            useTasks={useTasks}
                            allLists={lists.data!}
                            onCreateTask={createTask}
                            onUpdateTask={updateTask}
                            onMoveTask={moveTask}
                            filters={filters}
                        />
                    ))}
                </div>
            ) : (
                <EmptyState>No lists yet. Create one above.</EmptyState>
            )}

            {/* activity feed */}
            <div className="bg-white p-4 rounded shadow">
                <div className="font-medium mb-2">Activity</div>
                {activity.isLoading ? <Spinner /> : (
                    <ul className="space-y-2">
                        {activity.data?.map(a => (
                            <li key={a.id} className="text-sm text-gray-700">
                                <span className="text-gray-500">
                                    {(a.timestamp ?? a.createdAt) ? new Date(a.timestamp ?? a.createdAt!).toLocaleString() : "—"} —{" "}
                                </span>
                                <span className="font-medium">{a.type}</span>
                                {a.detail ? <>: <span className="text-gray-600">{a.detail}</span></> : null}
                            </li>
                        ))}
                        {(!activity.data || activity.data.length === 0) && (
                            <EmptyState>No activity yet.</EmptyState>
                        )}
                    </ul>
                )}
            </div>
        </div>
    );
}

function ListColumn({
                        list,
                        useTasks,
                        allLists,
                        onCreateTask,
                        onUpdateTask,
                        onMoveTask,
                        filters,
                    }: {
    list: BoardList;
    useTasks: (listId: number) => UseQueryResult<Task[], unknown>;
    allLists: BoardList[];
    onCreateTask: (listId: number, title: string) => Promise<void>;
    onUpdateTask: (listId: number, taskId: number, patch: Partial<Task>) => Promise<void>;
    onMoveTask: (fromListId: number, taskId: number, targetListId: number) => Promise<void>;
    filters: Filters;
}) {
    const tasks = useTasks(list.id);
    const filtered = (tasks.data ?? []).filter((t: Task) => {
        if (filters.q) {
          const q = filters.q.toLowerCase();
          const hay = `${t.title ?? ""} ${t.description ?? ""}`.toLowerCase();
          if (!hay.includes(q)) return false;
        }
        if (filters.status && t.status !== filters.status) return false;
        if (filters.assigneeId && !(t.assigneeIds ?? []).includes(Number(filters.assigneeId))) return false;
        if (filters.dueFrom && t.dueDate && dayjs(t.dueDate).isBefore(dayjs(filters.dueFrom))) return false;
        if (filters.dueTo && t.dueDate && dayjs(t.dueDate).isAfter(dayjs(filters.dueTo))) return false;
        return true;
    });
    const [title, setTitle] = useState("");

    return (
        <div className="bg-white rounded shadow p-4">
            <div className="font-medium mb-2">{list.name}</div>

            <div className="space-y-2">
                {filtered.map((t: Task) => (
                    <div key={t.id} className="border rounded p-2">
                        <div className="font-medium">{t.title}</div>
                        <div className="text-xs text-gray-500">{t.status}</div>
                        <div className="mt-2 flex gap-2">
                            {/* Quick status toggles */}
                            <button className="text-xs rounded bg-gray-100 px-2 py-1"
                                    onClick={()=>onUpdateTask(list.id, t.id, { status: "TODO" })}>TODO</button>
                            <button className="text-xs rounded bg-gray-100 px-2 py-1"
                                    onClick={()=>onUpdateTask(list.id, t.id, { status: "IN_PROGRESS" })}>IN&nbsp;PROGRESS</button>
                            <button className="text-xs rounded bg-gray-100 px-2 py-1"
                                    onClick={()=>onUpdateTask(list.id, t.id, { status: "DONE" })}>DONE</button>

                            {/* Move dropdown */}
                            <select
                                className="ml-auto text-xs border rounded px-2 py-1"
                                value={list.id}
                                onChange={(e) => onMoveTask(list.id, t.id, Number(e.target.value))}
                            >
                                {allLists.map(l => (
                                    <option key={l.id} value={l.id}>{l.name}</option>
                                ))}
                            </select>
                        </div>
                    </div>
                ))}
                {filtered.length === 0 && <EmptyState>No tasks.</EmptyState>}
            </div>

            {/* add task */}
            <div className="mt-3 flex gap-2">
                <input className="border rounded px-2 py-1 flex-1" placeholder="New task title"
                       value={title} onChange={e=>setTitle(e.target.value)} />
                <button
                    className="bg-gray-800 text-white rounded px-2"
                    onClick={async ()=>{
                        if (!title.trim()) return;
                        await onCreateTask(list.id, title.trim());
                        setTitle("");
                    }}>
                    +
                </button>
            </div>
        </div>
    );
}