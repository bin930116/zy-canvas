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
			<a-descriptions-item label="用户">
				{{ formData.userId }}
			</a-descriptions-item>
			<a-descriptions-item label="项目">
				{{ formData.projectId }}
			</a-descriptions-item>
			<a-descriptions-item label="提示词">
				{{ formData.prompt }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				{{ formData.status }}
			</a-descriptions-item>
			<a-descriptions-item label="画布快照JSON">
				{{ formData.canvasSnapshotJson }}
			</a-descriptions-item>
			<a-descriptions-item label="画布操作JSON">
				{{ formData.canvasOpsJson }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zySessionDetail">
	import { cloneDeep } from 'lodash-es'
	import zySessionApi from '@/api/canvas/zySessionApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zySessionApi.zySessionDetail({ id: record.id }).then((data) => {
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
