import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Drawer, Dropdown, Form, Input, Popconfirm, Radio, Space, Tag } from "antd";
import { ArrowLeft, Crown, Plus, Shield, Trash2, UserRound, UserRoundPlus } from "lucide-react";
import { useNavigate, useParams } from "react-router";

import { PageHeader, TableSurface, WorkspacePage } from "@/components/layout/workspace-page";
import { WorkspaceErrorState, WorkspaceLoadingState } from "@/components/layout/workspace-state";
import { getTeam, listTeamMembers, inviteTeamMember, updateTeamMemberRole, removeTeamMember, searchUsers } from "@/services/api/teams";
import { useUserStore } from "@/stores/use-user-store";

const roleLabels: Record<string, { label: string; color: string; icon: React.ReactNode }> = {
    owner: { label: "所有者", color: "gold", icon: <Crown className="size-3" /> },
    admin: { label: "管理员", color: "blue", icon: <Shield className="size-3" /> },
    member: { label: "成员", color: "default", icon: <UserRound className="size-3" /> },
};

const roleOptions = [
    { label: "管理员", value: "admin" },
    { label: "成员", value: "member" },
];

const inviteRoleCards = [
    { value: "admin", label: "管理员", icon: <Shield className="size-3.5" />, desc: "可邀请成员、调整角色" },
    { value: "member", label: "成员", icon: <UserRound className="size-3.5" />, desc: "查看团队并参与协作" },
];

export default function TeamDetailPage() {
    const { teamId = "" } = useParams();
    const navigate = useNavigate();
    const { message, modal } = App.useApp();
    const queryClient = useQueryClient();
    const currentUser = useUserStore((s) => s.user);
    const [inviteOpen, setInviteOpen] = useState(false);
    const [form] = Form.useForm();
    const [inviting, setInviting] = useState(false);

    const teamQuery = useQuery({
        queryKey: ["team", teamId],
        queryFn: () => getTeam(teamId),
        enabled: Boolean(teamId),
    });

    const membersQuery = useQuery({
        queryKey: ["team", teamId, "members"],
        queryFn: () => listTeamMembers(teamId),
        enabled: Boolean(teamId),
    });

    const inviteMutation = useMutation({
        mutationFn: (values: { userId: string; role: string }) => inviteTeamMember(teamId, values),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: ["team", teamId, "members"] });
            setInviteOpen(false);
            form.resetFields();
            message.success("成员已邀请");
        },
        onError: (error: Error) => message.error(error.message || "邀请失败"),
    });

    const roleMutation = useMutation({
        mutationFn: ({ memberId, role }: { memberId: string; role: string }) => updateTeamMemberRole(teamId, memberId, role),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: ["team", teamId, "members"] });
            message.success("角色已更新");
        },
        onError: (error: Error) => message.error(error.message || "更新失败"),
    });

    const removeMutation = useMutation({
        mutationFn: (memberId: string) => removeTeamMember(teamId, memberId),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: ["team", teamId, "members"] });
            message.success("成员已移除");
        },
        onError: (error: Error) => message.error(error.message || "移除失败"),
    });

    const closeInvite = () => {
        setInviteOpen(false);
        form.resetFields();
    };

    const handleInvite = async () => {
        let account = "";
        let role = "member";
        try {
            const values = await form.validateFields();
            account = String(values.account || "").trim();
            role = values.role || "member";
        } catch (error) {
            if (error && typeof error === "object" && "errorFields" in error) return;
            message.error("查询用户失败");
            return;
        }
        setInviting(true);
        try {
            const result = await searchUsers(account);
            const users = result.users || [];
            if (users.length === 0) {
                message.warning("未找到该用户，请检查账号是否正确");
                return;
            }
            const user = users[0];
            // Check duplicate
            const isAlreadyMember = members.some((m) => m.userId === user.id);
            if (isAlreadyMember) {
                message.warning(`「${user.name || user.account}」已是团队成员`);
                return;
            }
            const roleInfo = roleLabels[role] || roleLabels.member;
            modal.confirm({
                title: "确认邀请该成员？",
                icon: null,
                centered: true,
                content: (
                    <div className="pt-1">
                        <div className="rounded-lg border border-border/70 bg-foreground/[.04] p-3.5">
                            <div className="flex items-center gap-3">
                                <div className="grid size-9 shrink-0 place-items-center rounded-full bg-foreground/[.07]">
                                    <UserRound className="size-4 text-foreground/55" />
                                </div>
                                <div className="min-w-0">
                                    <div className="truncate text-sm font-medium">{user.name || user.account}</div>
                                    <div className="mt-0.5 truncate text-xs text-foreground/45">{user.account}</div>
                                </div>
                            </div>
                            <div className="mt-3 flex items-center gap-1.5 border-t border-border/60 pt-2.5 text-xs text-foreground/55">
                                <span>将以</span>
                                <Tag color={roleInfo.color} className="!mr-0">
                                    <span className="flex items-center gap-1">{roleInfo.icon}{roleInfo.label}</span>
                                </Tag>
                                <span>身份加入</span>
                            </div>
                        </div>
                    </div>
                ),
                okText: "确认邀请",
                cancelText: "取消",
                onOk: () => inviteMutation.mutateAsync({ userId: user.id, role }),
            });
        } catch {
            message.error("查询用户失败");
        } finally {
            setInviting(false);
        }
    };

    const team = teamQuery.data;
    const members = membersQuery.data?.members || [];
    const isOwner = members.some((m) => m.userId === currentUser?.id && m.role === "owner");

    if (teamQuery.isLoading) return <WorkspacePage><WorkspaceLoadingState label="正在加载团队" /></WorkspacePage>;
    if (teamQuery.isError || !team) return <WorkspacePage><WorkspaceErrorState title="团队不存在" description="团队可能已被删除。" actionLabel="返回团队列表" onRetry={() => navigate("/teams")} /></WorkspacePage>;

    return (
        <WorkspacePage>
            <div className="mb-2">
                <Button type="text" icon={<ArrowLeft className="size-4" />} onClick={() => navigate("/teams")}>返回团队列表</Button>
            </div>
            <PageHeader
                title={team.name}
                description={team.description || "团队详情"}
                meta={<span className="text-sm text-foreground/45">{members.length} 位成员</span>}
                actions={
                    isOwner ? (
                        <Button type="primary" icon={<Plus className="size-3.5" />} onClick={() => { setInviteOpen(true); form.resetFields(); }}>
                            邀请成员
                        </Button>
                    ) : null
                }
            />
            <TableSurface>
                <table className="w-full text-sm">
                    <thead>
                        <tr className="border-b border-border/60 text-left text-foreground/55">
                            <th className="px-4 py-3 font-medium">成员</th>
                            <th className="px-4 py-3 font-medium">角色</th>
                            <th className="px-4 py-3 font-medium">加入时间</th>
                            {isOwner && <th className="px-4 py-3 font-medium text-right">操作</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {members.map((member) => {
                            const roleInfo = roleLabels[member.role] || roleLabels.member;
                            return (
                                <tr key={member.id} className="border-b border-border/40 last:border-b-0">
                                    <td className="px-4 py-3">
                                        <div className="flex items-center gap-3">
                                            <div className="grid size-8 place-items-center rounded-full bg-foreground/[.06]">
                                                <UserRound className="size-4 text-foreground/40" />
                                            </div>
                                            <span className="font-medium">{member.userName || member.userId}</span>
                                        </div>
                                    </td>
                                    <td className="px-4 py-3">
                                        <Tag color={roleInfo.color}>
                                            <span className="flex items-center gap-1">
                                                {roleInfo.icon}
                                                {roleInfo.label}
                                            </span>
                                        </Tag>
                                    </td>
                                    <td className="px-4 py-3 text-foreground/50">
                                        {member.createdAt ? new Date(member.createdAt).toLocaleDateString("zh-CN") : "-"}
                                    </td>
                                    {isOwner && (
                                        <td className="px-4 py-3 text-right">
                                            {member.role !== "owner" && (
                                                <Space size={4}>
                                                    <Dropdown
                                                        trigger={["click"]}
                                                        menu={{
                                                            items: roleOptions.map((opt) => ({ key: opt.value, label: opt.label })),
                                                            onClick: ({ key }) => roleMutation.mutate({ memberId: member.userId, role: key }),
                                                        }}
                                                    >
                                                        <Button type="text" size="small" icon={<Shield className="size-3.5" />} />
                                                    </Dropdown>
                                                    <Popconfirm title="确定移除该成员？" onConfirm={() => removeMutation.mutate(member.userId)}>
                                                        <Button type="text" danger size="small" icon={<Trash2 className="size-3.5" />} />
                                                    </Popconfirm>
                                                </Space>
                                            )}
                                        </td>
                                    )}
                                </tr>
                            );
                        })}
                        {members.length === 0 && (
                            <tr>
                                <td colSpan={isOwner ? 4 : 3} className="px-4 py-12 text-center text-foreground/40">
                                    暂无成员
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </TableSurface>
            <Drawer
                rootClassName="invite-member-drawer"
                title="邀请成员"
                width={420}
                open={inviteOpen}
                onClose={closeInvite}
                destroyOnHidden
                footer={
                    <div className="flex items-center justify-end gap-2">
                        <Button onClick={closeInvite}>取消</Button>
                        <Button type="primary" icon={<UserRoundPlus className="size-3.5" />} loading={inviting || inviteMutation.isPending} onClick={handleInvite}>
                            邀请
                        </Button>
                    </div>
                }
            >
                <p className="mb-5 text-xs leading-relaxed text-foreground/45">
                    通过注册账号邀请用户加入「{team.name}」，加入后可在成员列表中随时调整角色或移除。
                </p>
                <Form form={form} layout="vertical" requiredMark={false} initialValues={{ role: "member" }}>
                    <Form.Item
                        name="account"
                        label="用户账号"
                        rules={[{ required: true, whitespace: true, message: "请输入用户账号" }]}
                        extra="对方需已注册本平台账号"
                    >
                        <Input placeholder="输入手机号或账号" allowClear autoComplete="off" />
                    </Form.Item>
                    <Form.Item name="role" label="角色">
                        <Radio.Group className="invite-role-cards grid w-full grid-cols-2 gap-2.5">
                            {inviteRoleCards.map((role) => (
                                <Radio key={role.value} value={role.value} className="invite-role-card">
                                    <span className="invite-role-card-icon">{role.icon}</span>
                                    <span className="flex min-w-0 flex-col gap-0.5">
                                        <span className="text-[13px] font-medium">{role.label}</span>
                                        <span className="text-xs text-foreground/45">{role.desc}</span>
                                    </span>
                                </Radio>
                            ))}
                        </Radio.Group>
                    </Form.Item>
                </Form>
            </Drawer>
        </WorkspacePage>
    );
}
