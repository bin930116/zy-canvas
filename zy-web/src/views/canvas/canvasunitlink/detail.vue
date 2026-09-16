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
			<a-descriptions-item label="画布">
				{{ formData.canvasId }}
			</a-descriptions-item>
			<a-descriptions-item label="章节">
				{{ formData.unitId }}
			</a-descriptions-item>
			<a-descriptions-item label="角色">
				<a-tag :color="$TOOL.dictTypeColor('zy_link_type', formData.role)">{{ $TOOL.dictTypeData('zy_link_type', formData.role) }}</a-tag>
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyCanvasUnitLinkDetail">
	import { cloneDeep } from 'lodash-es'
	import zyCanvasUnitLinkApi from '@/api/canvas/zyCanvasUnitLinkApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyCanvasUnitLinkApi.zyCanvasUnitLinkDetail({ id: record.id }).then((data) => {
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
