<template>
	<xn-form-container
		title="详情"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-descriptions :column="1" bordered size="small">
			<a-descriptions-item label="用户">
				{{ formData.userId }}
			</a-descriptions-item>
			<a-descriptions-item label="项目">
				{{ formData.projectId }}
			</a-descriptions-item>
			<a-descriptions-item label="任务类型">
				{{ formData.type }}
			</a-descriptions-item>
			<a-descriptions-item label="进度">
				{{ formData.progress }}
			</a-descriptions-item>
			<a-descriptions-item label="删除标志">
				{{ formData.deleteFlag }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				<a-tag :color="$TOOL.dictTypeColor('zy_task_status', formData.status)">{{ $TOOL.dictTypeData('zy_task_status', formData.status) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="阶段">
				{{ formData.stage }}
			</a-descriptions-item>
			<a-descriptions-item label="提示词">
				{{ formData.prompt }}
			</a-descriptions-item>
			<a-descriptions-item label="操作">
				{{ formData.operation }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商">
				{{ formData.provider }}
			</a-descriptions-item>
			<a-descriptions-item label="模型">
				{{ formData.model }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商请求id">
				{{ formData.providerRequestId }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商取消状态">
				{{ formData.providerCancelStatus }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商取消错误">
				{{ formData.providerCancelError }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商取消尝试次数">
				{{ formData.providerCancelAttempts }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商取消请求时间">
				{{ formData.providerCancelRequestedAt }}
			</a-descriptions-item>
			<a-descriptions-item label="提供商取消时间">
				{{ formData.providerCancelledAt }}
			</a-descriptions-item>
			<a-descriptions-item label="错误码">
				{{ formData.errorCode }}
			</a-descriptions-item>
			<a-descriptions-item label="官方状态">
				{{ formData.officialStatus }}
			</a-descriptions-item>
			<a-descriptions-item label="预览URL">
				{{ formData.previewUrl }}
			</a-descriptions-item>
			<a-descriptions-item label="预览类型">
				{{ formData.previewKind }}
			</a-descriptions-item>
			<a-descriptions-item label="预览海报URL">
				{{ formData.previewPosterUrl }}
			</a-descriptions-item>
			<a-descriptions-item label="输入JSON">
				{{ formData.inputJson }}
			</a-descriptions-item>
			<a-descriptions-item label="结果JSON">
				{{ formData.resultJson }}
			</a-descriptions-item>
			<a-descriptions-item label="结果状态">
				{{ formData.resultState }}
			</a-descriptions-item>
			<a-descriptions-item label="文本草稿">
				{{ formData.textDraft }}
			</a-descriptions-item>
			<a-descriptions-item label="错误信息">
				{{ formData.error }}
			</a-descriptions-item>
			<a-descriptions-item label="尝试次数">
				{{ formData.attempts }}
			</a-descriptions-item>
			<a-descriptions-item label="开始时间">
				{{ formData.startedAt }}
			</a-descriptions-item>
			<a-descriptions-item label="完成时间">
				{{ formData.completedAt }}
			</a-descriptions-item>
			<a-descriptions-item label="客户端上下文JSON">
				{{ formData.clientContextJson }}
			</a-descriptions-item>
			<a-descriptions-item label="处理该任务的Worker ID">
				{{ formData.workerId }}
			</a-descriptions-item>
			<a-descriptions-item label="任务租约过期时间">
				{{ formData.leaseExpiresAt }}
			</a-descriptions-item>
			<a-descriptions-item label="已重试次数">
				{{ formData.retryCount }}
			</a-descriptions-item>
			<a-descriptions-item label="最大重试次数">
				{{ formData.maxRetries }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyGenerationTaskDetail">
	import { cloneDeep } from 'lodash-es'
	import zyGenerationTaskApi from '@/api/canvas/zyGenerationTaskApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyGenerationTaskApi.zyGenerationTaskDetail({ id: record.id }).then((data) => {
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
