<template>
	<xn-panel>
		<a-form ref="searchFormRef" :model="searchFormState">
			<a-row :gutter="10">
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="模型标识" name="modelKey">
						<a-input v-model:value="searchFormState.modelKey" placeholder="请输入模型标识" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
					<a-form-item label="模型名称" name="modelName">
						<a-input v-model:value="searchFormState.modelName" placeholder="请输入模型名称" />
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
					<a-button type="primary" @click="formRef.onOpen()" v-if="hasPerm('zyModelAdd')">
						<template #icon><plus-outlined /></template>
						新增
					</a-button>
					<a-button @click="importModelRef.onOpen()" v-if="hasPerm('zyModelImport')">
                        <template #icon><import-outlined /></template>
                        <span>导入</span>
                    </a-button>
                    <a-button @click="exportData" v-if="hasPerm('zyModelExport')">
                        <template #icon><export-outlined /></template>
                        <span>导出</span>
                    </a-button>
					<xn-batch-button
						v-if="hasPerm('zyModelBatchDelete')"
						buttonName="批量删除"
						icon="DeleteOutlined"
						buttonDanger
						:id-key="'id'"
						:selectedRowKeys="selectedRowKeys"
						@batchCallBack="deleteBatchZyModel"
					/>
				</a-space>
			</template>
			<template #bodyCell="{ column, record }">
				<template v-if="column.dataIndex === 'capability'">
					<a-tag :color="$TOOL.dictTypeColor('MODEL_TYPE', record.capability)">{{ $TOOL.dictTypeData('MODEL_TYPE', record.capability) }}</a-tag>
				</template>
				<template v-if="column.dataIndex === 'protocol'">
					<a-tag :color="$TOOL.dictTypeColor('MODEL_XY_TYPE', record.protocol)">{{ $TOOL.dictTypeData('MODEL_XY_TYPE', record.protocol) }}</a-tag>
				</template>
				<template v-if="column.dataIndex === 'status'">
					<a-tag :color="$TOOL.dictTypeColor('MODEL_STATUS', record.status)">{{ $TOOL.dictTypeData('MODEL_STATUS', record.status) }}</a-tag>
				</template>
				<template v-if="column.dataIndex === 'action'">
					<a-space>
						<a @click="detailRef.onOpen(record)" v-if="hasPerm('zyModelDetail')">详情</a>
						<a-divider type="vertical" v-if="hasPerm(['zyModelDetail', 'zyModelEdit'], 'and')" />
						<a @click="formRef.onOpen(record)" v-if="hasPerm('zyModelEdit')">编辑</a>
						<a-divider type="vertical" v-if="hasPerm(['zyModelEdit', 'zyModelDelete'], 'and')" />
						<a-popconfirm title="确定要删除吗？" @confirm="deleteZyModel(record)">
							<a-button type="link" danger size="small" v-if="hasPerm('zyModelDelete')">删除</a-button>
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

<script setup name="model">
	import tool from '@/utils/tool'
	import { cloneDeep } from 'lodash-es'
	import Form from './form.vue'
	import Detail from './detail.vue'
	import ImportModel from './importModel.vue'
	import downloadUtil from '@/utils/downloadUtil'
	import zyModelApi from '@/api/canvas/zyModelApi'
	const searchFormState = ref({})
	const searchFormRef = ref()
	const tableRef = ref()
	const importModelRef = ref()
	const formRef = ref()
	const detailRef = ref()
	const toolConfig = { refresh: true, height: true, columnSetting: true, striped: false }
	const columns = [
		{
			title: '模型标识',
			dataIndex: 'modelKey'
		},
		{
			title: '模型名称',
			dataIndex: 'modelName'
		},
		{
			title: '提供商',
			dataIndex: 'providerName'
		},
		{
			title: '能力类型',
			dataIndex: 'capability'
		},
		{
			title: '协议类型',
			dataIndex: 'protocol'
		},
		{
			title: 'API密钥',
			dataIndex: 'apiKey'
		},
		{
			title: '状态',
			dataIndex: 'status'
		},
	]
	// 操作栏通过权限判断是否显示
	if (hasPerm(['zyModelEdit', 'zyModelDelete', 'zyModelDetail'])) {
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
		return zyModelApi.zyModelPage(Object.assign(parameter, searchFormParam)).then((data) => {
			return data
		})
	}
	// 重置
	const reset = () => {
		searchFormRef.value.resetFields()
		tableRef.value.refresh(true)
	}
	// 删除
	const deleteZyModel = (record) => {
		let params = [
			{
				id: record.id
			}
		]
		zyModelApi.zyModelDelete(params).then(() => {
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
            zyModelApi.zyModelExport(params).then((res) => {
                downloadUtil.resultDownload(res)
            })
        } else {
            zyModelApi.zyModelExport([]).then((res) => {
                downloadUtil.resultDownload(res)
            })
        }
    }
	// 批量删除
	const deleteBatchZyModel = (params) => {
		zyModelApi.zyModelDelete(params).then(() => {
			tableRef.value.clearRefreshSelected()
		})
	}
</script>
