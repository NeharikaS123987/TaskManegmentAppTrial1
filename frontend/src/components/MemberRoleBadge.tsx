export default function MemberRoleBadge({ role }: { role: "OWNER" | "EDITOR" | "VIEWER" | string }) {
  const map: Record<string, string> = {
    OWNER: "bg-purple-100 text-purple-700",
    EDITOR: "bg-blue-100 text-blue-700",
    VIEWER: "bg-gray-100 text-gray-700",
  };
  const cls = map[role] ?? "bg-gray-100 text-gray-700";
  return <span className={`text-xs px-2 py-0.5 rounded ${cls}`}>{role}</span>;
}