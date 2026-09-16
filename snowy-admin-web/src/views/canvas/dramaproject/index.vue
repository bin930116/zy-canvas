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
					<a-form-item label="名称" name="name">
						<a-input v-model:value="searchFormState.name" placeholder="请输入名称" />
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
					<a-button type="primary" @click="formRef.onOpen()" v-if="hasPerm('zyDramaProjectAdd')">
						<template #icon><plus-outlined /></template>
						新增
					</a-button>
					<a-button @click="importModelRef.onOpen()" v-if="hasPerm('zyDramaProjectImport')">
                        <template #icon><import-outlined /></template>
                        <span>导入</span>
                    </a-button>
                    <a-button @click="exportData" v-if="hasPerm('zyDramaProjectExport')">
                        <template #icon><export-outlined /></template>
                        <span>导出</span>
                    </a-button>
					<xn-batch-button
						v-if="hasPerm('zyDramaProjectBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						buttonDanger
						:id-key="'id'"
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchZyDramaProject"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a @click="detailRef.onOpen(record)" v-if="hasPerm('zyDramaProjectDetail')">详情</a>
						<a-divider type="vertical" v-if="hasPerm(['zyDramaProjectDetail', 'zyDramaProjectEdit'], 'and')" />
						<a @click="formRef.onOpen(record)" v-if="hasPerm('zyDramaProjectEdit')">编辑</a>
						<a-divider type="vertical" v-if="hasPerm(['zyDramaProjectEdit', 'zyDramaProjectDelete'], 'and')" />
						<a-popconfirm title="确定要删除吗？" @confirm="deleteZyDramaProject(record)">
							<a-button type="link" danger size="small" v-if="hasPerm('zyDramaProjectDelete')">删除</a-button>
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

<script setup name="dramaproject">
	import { cloneDeep } from 'lodash-es'
	import Form from './form.vue'
	import Detail from './detail.vue'
	import ImportModel from './importModel.vue'
	import downloadUtil from '@/utils/downloadUtil'
	import zyDramaProjectApi from '@/api/canvas/zyDramaProjectApi'
	const searchFormState = ref({})
	const searchFormRef = ref()
	const tableRef = ref()
	const importModelRef = ref()
	const formRef = ref()
	const detailRef = ref()
	const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
	const columns = [
		{
			title: '用户',
			dataIndex: 'userId'
		},
		{
			title: '名称',
			dataIndex: 'name'
		},
		{
			title: '类型',
			dataIndex: 'type'
		},
		{
			title: '画幅比',
			dataIndex: 'aspectRatio'
		},
		{
			title: '来源类型',
			dataIndex: 'sourceType'
		},
		{
			title: '状态',
			dataIndex: 'status'
		},
	]
	// 操作栏通过权限判断是否显示
	if (hasPerm(['zyDramaProjectEdit', 'zyDramaProjectDelete', 'zyDramaProjectDetail'])) {
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
		return zyDramaProjectApi.zyDramaProjectPage(Object.assign(parameter, searchFormParam)).then((data) => {
			return data
		})
	}
	// 重置
	const reset = () => {
		searchFormRef.value.resetFields()
		tableRef.value.refresh(true)
	}
	// 删除
	const deleteZyDramaProject = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		zyDramaProjectApi.zyDramaProjectDelete(params).then(() => {
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
            zyDramaProjectApi.zyDramaProjectExport(params).then((res) => {
                downloadUtil.resultDownload(res)
            })
        } else {
            zyDramaProjectApi.zyDramaProjectExport([]).then((res) => {
                downloadUtil.resultDownload(res)
            })
        }
    }
	// 批量删除
	const deleteBatchZyDramaProject = (params) => {
		zyDramaProjectApi.zyDramaProjectDelete(params).then(() => {
			tableRef.value.clearRefreshSelected()
		})
	}
</script>
