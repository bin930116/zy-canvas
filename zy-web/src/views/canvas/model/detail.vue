<template>
	<xn-form-container
		title="详情"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-descriptions :column="1" bordered size="small">
			<a-descriptions-item label="删除标志">
				{{ formData.deleteFlag }}
			</a-descriptions-item>
			<a-descriptions-item label="模型标识">
				{{ formData.modelKey }}
			</a-descriptions-item>
			<a-descriptions-item label="模型名称">
				{{ formData.modelName }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商">
				{{ formData.providerName }}
			</a-descriptions-item>
			<a-descriptions-item label="能力类型">
				<a-tag :color="$TOOL.dictTypeColor('MODEL_TYPE', formData.capability)">{{ $TOOL.dictTypeData('MODEL_TYPE', formData.capability) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="协议类型">
				<a-tag :color="$TOOL.dictTypeColor('MODEL_XY_TYPE', formData.protocol)">{{ $TOOL.dictTypeData('MODEL_XY_TYPE', formData.protocol) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="baseUrl">
				{{ formData.baseUrl }}
			</a-descriptions-item>
			<a-descriptions-item label="API密钥">
				{{ formData.apiKey }}
			</a-descriptions-item>
			<a-descriptions-item label="并发数">
				{{ formData.maxConcurrency }}
			</a-descriptions-item>
			<a-descriptions-item label="超时时间（秒）">
				{{ formData.timeoutSeconds }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				<a-tag :color="$TOOL.dictTypeColor('MODEL_STATUS', formData.status)">{{ $TOOL.dictTypeData('MODEL_STATUS', formData.status) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="是否默认">
				<a-tag :color="$TOOL.dictTypeColor('COMMON_WHETHER', formData.isDefault)">{{ $TOOL.dictTypeData('COMMON_WHETHER', formData.isDefault) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="排序码">
				{{ formData.sortCode }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyModelDetail">
	import { cloneDeep } from 'lodash-es'
	import zyModelApi from '@/api/canvas/zyModelApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyModelApi.zyModelDetail({ id: record.id }).then((data) => {
			formData.value = data
		})
	}
	// 关闭抽屉
	const onClose = () => {
		formData.value = {}
		open.value = false
	}
	// 下载文件
	const downloadFile = (url) => {
		window.open(url)
	}
	// 抛出函数
	defineExpose({
		onOpen
	})
</script>
