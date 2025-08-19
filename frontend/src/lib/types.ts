// frontend/src/lib/types.ts
export type BoardRole = "OWNER" | "EDITOR" | "VIEWER" | undefined;

export interface Board {
    id: number;
    name: string;
    description?: string;
    role?: BoardRole;
    listsCount?: number;
    tasksCount?: number;
}

export interface BoardList {
    id: number;
    name: string;
    boardId: number;
    order?: number;
}

export type TaskStatus = "TODO" | "TO_DO" | "IN_PROGRESS" | "DONE";

export interface Task {
    id: number;
    title: string;
    description?: string;
    dueDate?: string;      // ISO date
    status: TaskStatus;
    listId: number;
    assigneeIds?: number[];
    createdAt?: string;    // ISO instant
    updatedAt?: string;    // ISO instant
    completedAt?: string;  // ISO instant
}

export interface Activity {
    id: number;
    type: string;          // e.g. "TASK_MOVED"
    detail?: string;
    timestamp?: string;
    createdAt?: string;
}