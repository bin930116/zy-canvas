<template>
	<xn-panel>
		<a-form ref="searchFormRef" :model="searchFormState">
			<a-row :gutter="10">
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="团队名称" name="name">
						<a-input v-model:value="searchFormState.name" placeholder="请输入团队名称" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="状态：active=正常 / deleted=已删除" name="status">
						<a-select v-model:value="searchFormState.status" placeholder="请选择状态：active=正常 / deleted=已删除" :options="statusOptions" />
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
					<a-button type="primary" @click="formRef.onOpen()" v-if="hasPerm('zyTeamAdd')">
						<template #icon><plus-outlined /></template>
						新增
					</a-button>
					<a-button @click="importModelRef.onOpen()" v-if="hasPerm('zyTeamImport')">
                        <template #icon><import-outlined /></template>
                        <span>导入</span>
                    </a-button>
                    <a-button @click="exportData" v-if="hasPerm('zyTeamExport')">
                        <template #icon><export-outlined /></template>
                        <span>导出</span>
                    </a-button>
					<xn-batch-button
						v-if="hasPerm('zyTeamBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						buttonDanger
						:id-key="'id'"
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchZyTeam"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'status'">
					<a-tag :color="$TOOL.dictTypeColor('zy_team_status', record.status)">{{ $TOOL.dictTypeData('zy_team_status', record.status) }}</a-tag>
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a @click="detailRef.onOpen(record)" v-if="hasPerm('zyTeamDetail')">详情</a>
						<a-divider type="vertical" v-if="hasPerm(['zyTeamDetail', 'zyTeamEdit'], 'and')" />
						<a @click="formRef.onOpen(record)" v-if="hasPerm('zyTeamEdit')">编辑</a>
						<a-divider type="vertical" v-if="hasPerm(['zyTeamEdit', 'zyTeamDelete'], 'and')" />
						<a-popconfirm title="确定要删除吗？" @confirm="deleteZyTeam(record)">
							<a-button type="link" danger size="small" v-if="hasPerm('zyTeamDelete')">删除</a-button>
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

<script setup name="team">
	import tool from '@/utils/tool'
	import { cloneDeep } from 'lodash-es'
	import Form from './form.vue'
	import Detail from './detail.vue'
	import ImportModel from './importModel.vue'
	import downloadUtil from '@/utils/downloadUtil'
	import zyTeamApi from '@/api/canvas/zyTeamApi'
	const searchFormState = ref({})
	const searchFormRef = ref()
	const tableRef = ref()
	const importModelRef = ref()
	const formRef = ref()
	const detailRef = ref()
	const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
	const columns = [
		{
			title: '团队名称',
			dataIndex: 'name'
		},
		{
			title: '团队描述',
			dataIndex: 'description'
		},
		{
			title: '状态：active=正常 / deleted=已删除',
			dataIndex: 'status'
		},
	]
	// 操作栏通过权限判断是否显示
	if (hasPerm(['zyTeamEdit', 'zyTeamDelete', 'zyTeamDetail'])) {
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
		return zyTeamApi.zyTeamPage(Object.assign(parameter, searchFormParam)).then((data) => {
			return data
		})
	}
	// 重置
	const reset = () => {
		searchFormRef.value.resetFields()
		tableRef.value.refresh(true)
	}
	// 删除
	const deleteZyTeam = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		zyTeamApi.zyTeamDelete(params).then(() => {
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
            zyTeamApi.zyTeamExport(params).then((res) => {
                downloadUtil.resultDownload(res)
            })
        } else {
            zyTeamApi.zyTeamExport([]).then((res) => {
                downloadUtil.resultDownload(res)
            })
        }
    }
	// 批量删除
	const deleteBatchZyTeam = (params) => {
		zyTeamApi.zyTeamDelete(params).then(() => {
			tableRef.value.clearRefreshSelected()
		})
	}
	const statusOptions = tool.dictList('zy_team_status')
</script>
