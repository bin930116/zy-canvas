import { apiClient, request } from "@/services/api/request";

const api = apiClient;

export type Team = {
    id: string;
    userId: string;
    name: string;
    description?: string;
    avatarUrl?: string;
    status: string;
    memberCount?: number;
    createdAt: string;
    updatedAt: string;
};

export type TeamMember = {
    id: string;
    userId: string;
    userName?: string;
    role: "owner" | "admin" | "member";
    status: string;
    invitedBy?: string;
    createdAt: string;
};

export type TeamListPage = {
    records: Team[];
    total: number;
    page: number;
    pageSize: number;
    hasMore: boolean;
};

export function listTeams(params?: { page?: number; pageSize?: number }) {
    return request<TeamListPage>(api.get("/teams", { params }));
}

export function createTeam(input: { name: string; description?: string; avatarUrl?: string }) {
    return request<Team>(api.post("/teams", input));
}

export function getTeam(id: string) {
    return request<Team>(api.get(`/teams/${encodeURIComponent(id)}`));
}

export function updateTeam(id: string, input: { name?: string; description?: string; avatarUrl?: string }) {
    return request<Team>(api.patch(`/teams/${encodeURIComponent(id)}`, input));
}

export function deleteTeam(id: string) {
    return request<void>(api.delete(`/teams/${encodeURIComponent(id)}`));
}

export function listTeamMembers(teamId: string) {
    return request<{ members: TeamMember[]; total: number }>(api.get(`/teams/${encodeURIComponent(teamId)}/members`));
}

export function inviteTeamMember(teamId: string, input: { userId: string; role?: string }) {
    return request<void>(api.post(`/teams/${encodeURIComponent(teamId)}/members`, input));
}

export function updateTeamMemberRole(teamId: string, memberId: string, role: string) {
    return request<void>(api.patch(`/teams/${encodeURIComponent(teamId)}/members/${encodeURIComponent(memberId)}`, { role }));
}

export function removeTeamMember(teamId: string, memberId: string) {
    return request<void>(api.delete(`/teams/${encodeURIComponent(teamId)}/members/${encodeURIComponent(memberId)}`));
}

export function searchUsers(keyword: string) {
    return request<{ users: Array<{ id: string; name: string; account: string }> }>(api.get("/teams/search-users", { params: { keyword } }));
}
