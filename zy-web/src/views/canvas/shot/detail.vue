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
			<a-descriptions-item label="项目">
				{{ formData.projectId }}
			</a-descriptions-item>
			<a-descriptions-item label="所属章节">
				{{ formData.unitId }}
			</a-descriptions-item>
			<a-descriptions-item label="当前版本">
				{{ formData.currentRevisionId }}
			</a-descriptions-item>
			<a-descriptions-item label="标题">
				{{ formData.title }}
			</a-descriptions-item>
			<a-descriptions-item label="描述">
				{{ formData.description }}
			</a-descriptions-item>
			<a-descriptions-item label="排序">
				{{ formData.position }}
			</a-descriptions-item>
			<a-descriptions-item label="时长(毫秒)">
				{{ formData.durationMs }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				{{ formData.status }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyShotDetail">
	import { cloneDeep } from 'lodash-es'
	import zyShotApi from '@/api/canvas/zyShotApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyShotApi.zyShotDetail({ id: record.id }).then((data) => {
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
