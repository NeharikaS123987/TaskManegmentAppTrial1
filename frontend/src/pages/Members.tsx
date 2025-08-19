import { useParams, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import  api  from "../lib/api";
import MemberRoleBadge from "../components/MemberRoleBadge.tsx";
import { useState } from "react";

type Role = "OWNER" | "EDITOR" | "VIEWER";

type Member = { userId: number; name: string; email: string; role: Role };

export default function Members() {
    const { id } = useParams();
    const boardId = Number(id);
    const qc = useQueryClient();

    const members = useQuery({
        queryKey: ["members", boardId],
        queryFn: async () => (await api.get<Member[]>(`/api/boards/${boardId}/members`)).data,
    });

    const [email, setEmail] = useState("");
    const [role, setRole] = useState<Role>("VIEWER");

    const invite = useMutation({
        mutationFn: async () => (await api.post(`/api/boards/${boardId}/members`, { email, role })).data,
        onSuccess: async () => { setEmail(""); setRole("VIEWER"); await qc.invalidateQueries({ queryKey: ["members", boardId] }); }
    });

    const remove = useMutation({
        mutationFn: async (userId: number) => (await api.delete(`/api/boards/${boardId}/members/${userId}`)).data,
        onSuccess: async () => { await qc.invalidateQueries({ queryKey: ["members", boardId] }); }
    });

    const changeRole = useMutation({
        mutationFn: async ({ userId, role }: { userId: number; role: Role }) =>
            (await api.put(`/api/boards/${boardId}/members/${userId}/role`, { role })).data,
        onSuccess: async () => { await qc.invalidateQueries({ queryKey: ["members", boardId] }); }
    });

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
              <h1 className="text-xl font-semibold">Board {boardId}</h1>
              <Link to={`/boards/${boardId}/members`} className="text-blue-600 text-sm">Manage members</Link>
            </div>

            <div className="bg-white p-4 rounded shadow space-y-3">
                <div className="font-medium">Invite member</div>
                <div className="flex gap-2">
                    <input className="border rounded px-3 py-2 flex-1" placeholder="Email"
                           value={email} onChange={e=>setEmail(e.target.value)} />
                    <select className="border rounded px-2" value={role} onChange={(e)=>setRole(e.target.value as Role)}>
                        <option value="VIEWER">Viewer</option>
                        <option value="EDITOR">Editor</option>
                    </select>
                    <button onClick={()=>invite.mutate()} className="bg-blue-600 text-white rounded px-3 py-2">Invite</button>
                </div>
                <div className="text-xs text-gray-500">Only owners/admins can invite or change roles.</div>
            </div>

            <div className="bg-white p-4 rounded shadow">
                <div className="font-medium mb-2">Current members</div>
                <table className="w-full text-sm">
                    <thead>
                    <tr className="text-left text-gray-500">
                        <th className="py-2">Name</th><th>Email</th><th>Role</th><th></th>
                    </tr>
                    </thead>
                    <tbody>
                    {members.data?.map(m => (
                        <tr key={m.userId} className="border-t">
                            <td className="py-2">{m.name}</td>
                            <td>{m.email}</td>
                            <td><MemberRoleBadge role={m.role} /></td>
                            <td className="text-right space-x-2">
                                <select className="border rounded px-1 py-0.5 text-xs"
                                        value={m.role}
                                        onChange={(e)=>changeRole.mutate({ userId: m.userId, role: e.target.value as Role })}
                                >
                                    <option>VIEWER</option>
                                    <option>EDITOR</option>
                                    <option>OWNER</option>
                                </select>
                                <button className="text-red-600 text-xs" onClick={()=>remove.mutate(m.userId)}>Remove</button>
                            </td>
                        </tr>
                    ))}
                    {(!members.data || members.data.length===0) && (
                        <tr><td colSpan={4} className="text-gray-500 py-3">No members yet.</td></tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}