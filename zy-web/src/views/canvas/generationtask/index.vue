<template>
	<xn-panel>
		<a-form ref="searchFormRef" :model="searchFormState">
			<a-row :gutter="10">
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="用户" name="userId">
						<a-input v-model:value="searchFormState.userId" placeholder="请输入用户" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="项目" name="projectId">
						<a-input v-model:value="searchFormState.projectId" placeholder="请输入项目" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="任务类型" name="type">
						<a-input v-model:value="searchFormState.type" placeholder="请输入任务类型" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6" v-show="advanced">
					<a-form-item label="状态" name="status">
						<a-select v-model:value="searchFormState.status" placeholder="请选择状态" :options="statusOptions" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
				    <a-form-item>
				        <a-space>
				            <a-button type="primary" @click="tableRef.refresh(true)">
				                <template #icon><SearchOutlined /></template>
				                查询
				            </a-button>
				            <a-button @click="reset">
				                <template #icon><redo-outlined /></template>
				                 重置
				            </a-button>
                            <a @click="toggleAdvanced">
                                {{ advanced ? '收起' : '展开' }}
                                <component :is="advanced ? 'up-outlined' : 'down-outlined'"/>
                            </a>
                        </a-space>
                    </a-form-item>
                </a-col>
            </a-row>
		</a-form>
		<s-table
			ref="tableRef"
			:columns="columns"
			:data="loadData"
			:alert="options.alert.show"
			bordered
			:row-key="(record) => record.id"
			:tool-config="toolConfig"
			:row-selection="options.rowSelection"
			:scroll="{ x: 'max-content' }"
		>
			<template #operator>
				<a-space>
					<a-button type="primary" @click="formRef.onOpen()" v-if="hasPerm('zyGenerationTaskAdd')">
						<template #icon><plus-outlined /></template>
						新增
					</a-button>
					<a-button @click="importModelRef.onOpen()" v-if="hasPerm('zyGenerationTaskImport')">
                        <template #icon><import-outlined /></template>
                        <span>导入</span>
                    </a-button>
                    <a-button @click="exportData" v-if="hasPerm('zyGenerationTaskExport')">
                        <template #icon><export-outlined /></template>
                        <span>导出</span>
                    </a-button>
					<xn-batch-button
						v-if="hasPerm('zyGenerationTaskBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						buttonDanger
						:id-key="'id'"
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchZyGenerationTask"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'status'">
					<a-tag :color="$TOOL.dictTypeColor('zy_task_status', record.status)">{{ $TOOL.dictTypeData('zy_task_status', record.status) }}</a-tag>
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a @click="detailRef.onOpen(record)" v-if="hasPerm('zyGenerationTaskDetail')">详情</a>
						<a-divider type="vertical" v-if="hasPerm(['zyGenerationTaskDetail', 'zyGenerationTaskEdit'], 'and')" />
						<a @click="formRef.onOpen(record)" v-if="hasPerm('zyGenerationTaskEdit')">编辑</a>
						<a-divider type="vertical" v-if="hasPerm(['zyGenerationTaskEdit', 'zyGenerationTaskDelete'], 'and')" />
						<a-popconfirm title="确定要删除吗？" @confirm="deleteZyGenerationTask(record)">
							<a-button type="link" danger size="small" v-if="hasPerm('zyGenerationTaskDelete')">删除</a-button>
						</a-popconfirm>
					</a-space>
				</template>
			</template>
		</s-table>
	</xn-panel>
	<ImportModel ref="importModelRef" />
	<Form ref="formRef" @successful="tableRef.refresh()" />
	<Detail ref="detailRef" />
</template>

<script setup name="generationtask">
	import tool from '@/utils/tool'
	import { cloneDeep } from 'lodash-es'
	import Form from './form.vue'
	import Detail from './detail.vue'
	import ImportModel from './importModel.vue'
	import downloadUtil from '@/utils/downloadUtil'
	import zyGenerationTaskApi from '@/api/canvas/zyGenerationTaskApi'
	const searchFormState = ref({})
	const searchFormRef = ref()
	const tableRef = ref()
	const importModelRef = ref()
	const formRef = ref()
	const detailRef = ref()
	const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
	// 查询区域显示更多控制
	const advanced = ref(false)
	const toggleAdvanced = () => {
		advanced.value = !advanced.value
	}
	const columns = [
		{
			title: '用户',
			dataIndex: 'userId'
		},
		{
			title: '项目',
			dataIndex: 'projectId'
		},
		{
			title: '任务类型',
			dataIndex: 'type'
		},
		{
			title: '进度',
			dataIndex: 'progress'
		},
		{
			title: '状态',
			dataIndex: 'status'
		},
		{
			title: '阶段',
			dataIndex: 'stage'
		},
		{
			title: '开始时间',
			dataIndex: 'startedAt'
		},
		{
			title: '完成时间',
			dataIndex: 'completedAt'
		},
		{
			title: '处理该任务的Worker ID',
			dataIndex: 'workerId'
		},
		{
			title: '任务租约过期时间',
			dataIndex: 'leaseExpiresAt'
		},
		{
			title: '已重试次数',
			dataIndex: 'retryCount'
		},
		{
			title: '最大重试次数',
			dataIndex: 'maxRetries'
		},
	]
	// 操作栏通过权限判断是否显示
	if (hasPerm(['zyGenerationTaskEdit', 'zyGenerationTaskDelete', 'zyGenerationTaskDetail'])) {
		columns.push({
			title: '操作',
			dataIndex: 'action',
			align: 'center',
			fixed: 'right'
		})
	}
	const selectedRowKeys = ref([])
	// 列表选择配置
	const options = {
		alert: {
			show: true,
			clear: () => {
				selectedRowKeys.value = ref([])
			}
		},
		rowSelection: {
			onChange: (selectedRowKey, selectedRows) => {
				selectedRowKeys.value = selectedRowKey
			}
		}
	}
	const loadData = (parameter) => {
		const searchFormParam = cloneDeep(searchFormState.value)
		return zyGenerationTaskApi.zyGenerationTaskPage(Object.assign(parameter, searchFormParam)).then((data) => {
			return data
		})
	}
	// 重置
	const reset = () => {
		searchFormRef.value.resetFields()
		tableRef.value.refresh(true)
	}
	// 删除
	const deleteZyGenerationTask = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		zyGenerationTaskApi.zyGenerationTaskDelete(params).then(() => {
			tableRef.value.refresh(true)
		})
	}
	// 导出
    const exportData = () => {
        if (selectedRowKeys.value.length > 0) {
            const params = selectedRowKeys.value.map((m) => {
                return {
                    id: m
                }
            })
            zyGenerationTaskApi.zyGenerationTaskExport(params).then((res) => {
                downloadUtil.resultDownload(res)
            })
        } else {
            zyGenerationTaskApi.zyGenerationTaskExport([]).then((res) => {
                downloadUtil.resultDownload(res)
            })
        }
    }
	// 批量删除
	const deleteBatchZyGenerationTask = (params) => {
		zyGenerationTaskApi.zyGenerationTaskDelete(params).then(() => {
			tableRef.value.clearRefreshSelected()
		})
	}
	const statusOptions = tool.dictList('zy_task_status')
</script>
