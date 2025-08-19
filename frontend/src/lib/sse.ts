// Simple SSE helper for board activity stream
export function subscribeBoardEvents(baseUrl: string, boardId: number, onEvent: (e: MessageEvent) => void): () => void {
    const url = `${baseUrl}/api/boards/${boardId}/events`;
    const es = new EventSource(url, { withCredentials: false });
    es.onmessage = onEvent;
    es.onerror = () => { /* let backend reconnect logic / browser handle */ };
    return () => es.close();
}