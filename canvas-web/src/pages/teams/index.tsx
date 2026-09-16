import { useState } from "react";
import { useInfiniteQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { App, Button, Form, Input, Modal } from "antd";
import { FolderKanban, Plus, Search, Users, Trash2 } from "lucide-react";
import { Link, useNavigate } from "react-router";

import { CollectionGrid, ListToolbar, PageHeader, PaginationBar, WorkspacePage } from "@/components/layout/workspace-page";
import { WorkspaceState } from "@/components/layout/workspace-state";
import { listTeams, createTeam, deleteTeam, type Team } from "@/services/api/teams";

export default function TeamsPage() {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { message, modal } = App.useApp();
    const [createOpen, setCreateOpen] = useState(false);
    const [createForm] = Form.useForm();
    const [keyword, setKeyword] = useState("");
    const [page, setPage] = useState(1);
    const [pageSize, setPageSize] = useState(20);

    const query = useInfiniteQuery({
        queryKey: ["teams", "paged"],
        queryFn: ({ pageParam }) => listTeams({ page: pageParam, pageSize: 50 }),
        initialPageParam: 1,
        getNextPageParam: (lastPage) => (lastPage.hasMore ? lastPage.page + 1 : undefined),
    });

    const mutation = useMutation({
        mutationFn: createTeam,
        onSuccess: (team) => {
            setCreateOpen(false);
            createForm.resetFields();
            void queryClient.invalidateQueries({ queryKey: ["teams"] });
            navigate(`/teams/${team.id}`);
        },
        onError: (error: Error) => message.error(error.message || "创建失败"),
    });

    const deleteMutation = useMutation({
        mutationFn: deleteTeam,
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: ["teams"] });
            message.success("团队已删除");
        },
        onError: (error: Error) => message.error(error.message || "删除失败"),
    });

    const confirmDelete = (teamId: string, name: string) => {
        modal.confirm({
            title: "删除团队",
            content: `确定删除「${name}」吗？团队成员关系将一并移除。`,
            okText: "删除",
            okButtonProps: { danger: true, loading: deleteMutation.isPending },
            cancelText: "取消",
            onOk: () => deleteMutation.mutate(teamId),
        });
    };

    const allTeams = query.data?.pages.flatMap((page) => page.records) || [];
    const rows = allTeams.filter((team) => !keyword || team.name.toLowerCase().includes(keyword.toLowerCase()));

    return (
        <WorkspacePage className="library-page" grid>
            <PageHeader
                title="团队"
                description="创建和管理团队，邀请成员协作"
                actions={<Button type="primary" icon={<Plus className="size-3.5" />} onClick={() => setCreateOpen(true)}>创建团队</Button>}
            />
            <ListToolbar className="library-toolbar" active={Boolean(keyword)} onReset={() => setKeyword("")}>
                <Input prefix={<Search className="size-3.5 text-foreground/32" />} value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="搜索团队名称" allowClear className="w-56" size="small" />
            </ListToolbar>
            {query.isLoading ? (
                <WorkspaceState icon="assets" compact title="正在加载团队" description="读取团队列表" />
            ) : rows.length ? (
                <>
                    <CollectionGrid className="library-grid">
                        {rows.map((team) => (
                            <TeamCard key={team.id} team={team} onDelete={() => confirmDelete(team.id, team.name)} />
                        ))}
                    </CollectionGrid>
                    <PaginationBar current={page} pageSize={pageSize} total={allTeams.length} itemLabel="个团队" onChange={(nextPage, nextPageSize) => { setPage(nextPageSize !== pageSize ? 1 : nextPage); setPageSize(nextPageSize); }} />
                </>
            ) : (
                <WorkspaceState icon="assets" compact title="还没有团队" description="创建一个团队，邀请成员开始协作。" action={<Button type="primary" onClick={() => setCreateOpen(true)}>创建团队</Button>} />
            )}
            <Modal className="library-modal" title="创建团队" open={createOpen} footer={null} destroyOnHidden onCancel={() => setCreateOpen(false)} width={480}>
                <Form form={createForm} layout="vertical" initialValues={{}} onFinish={(values) => mutation.mutate(values)}>
                    <Form.Item name="name" label="团队名称" rules={[{ required: true, whitespace: true, message: "请输入团队名称" }]}>
                        <Input autoFocus placeholder="例如：创作组" />
                    </Form.Item>
                    <Form.Item name="description" label="团队描述">
                        <Input.TextArea rows={3} placeholder="简单描述团队的用途（可选）" />
                    </Form.Item>
                    <div className="flex justify-end gap-2">
                        <Button onClick={() => setCreateOpen(false)}>取消</Button>
                        <Button type="primary" htmlType="submit" loading={mutation.isPending}>创建</Button>
                    </div>
                </Form>
            </Modal>
        </WorkspacePage>
    );
}

function TeamCard({ team, onDelete }: { team: Team; onDelete: () => void }) {
    return (
        <Link to={`/teams/${team.id}`} className="library-card project-library-card group">
            <span className="project-library-cover">
                <span className="grid h-full w-full place-items-center bg-foreground/[.06]">
                    <Users className="size-10 text-foreground/20" />
                </span>
                <span className="project-library-cover-scrim" />
                <button
                    type="button"
                    className="project-library-cover-delete"
                    title="删除团队"
                    aria-label={`删除团队 ${team.name}`}
                    onClick={(event) => {
                        event.preventDefault();
                        event.stopPropagation();
                        onDelete();
                    }}
                >
                    <Trash2 className="size-3.5" />
                </button>
            </span>
            <span className="project-library-body">
                <span className="project-library-heading">
                    <strong title={team.name}>{team.name}</strong>
                </span>
                <span className="project-library-subtitle">{team.description || "暂无描述"}</span>
                <span className="project-library-stats">
                    <span className="inline-flex items-center gap-1.5" title="成员数量">
                        <span className="text-foreground/32"><Users className="size-3.5" /></span>
                        <strong className="font-medium tabular-nums text-foreground/65">{team.memberCount || 0}</strong>
                        <span>成员</span>
                    </span>
                </span>
            </span>
        </Link>
    );
}
