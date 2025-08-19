import { useQuery } from "@tanstack/react-query";
import api  from "../lib/api";

type KPI = {
    boardId: number;
    boardName: string;
    tasksTotal: number;
    tasksCompleted: number;
    avgCompletionHours: number | null;
    mostActiveUsers: { userId:number; name:string; count:number }[];
};

export default function AdminAnalytics() {
    const kpis = useQuery({
        queryKey: ["admin", "analytics"],
        queryFn: async () => {
            try {
                // preferred: your backend exposes this
                return (await api.get<KPI[]>("/api/admin/analytics/boards")).data;
            } catch {
                return []; // graceful fallback
            }
        }
    });

    return (
        <div className="space-y-6">
            <h1 className="text-xl font-semibold">Admin Analytics</h1>
            {kpis.data && kpis.data.length > 0 ? (
                <div className="space-y-4">
                    {kpis.data.map(b => (
                        <div key={b.boardId} className="bg-white rounded shadow p-4">
                            <div className="font-medium">{b.boardName}</div>
                            <div className="text-sm text-gray-600">
                                Total: {b.tasksTotal} • Completed: {b.tasksCompleted} •
                                Avg completion: {b.avgCompletionHours === null ? "—" : `${b.avgCompletionHours.toFixed(1)}h`}
                            </div>
                            <div className="mt-2">
                                <div className="text-xs text-gray-500">Most active users:</div>
                                <ul className="text-sm">
                                    {b.mostActiveUsers.map(u => (
                                        <li key={u.userId}>{u.name} — {u.count}</li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div className="text-gray-500">No analytics endpoint found or no data.</div>
            )}
        </div>
    );
}