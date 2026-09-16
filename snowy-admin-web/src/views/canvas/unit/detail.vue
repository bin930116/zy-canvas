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
			<a-descriptions-item label="类型">
				<a-tag :color="$TOOL.dictTypeColor('zy_unit_type', formData.kind)">{{ $TOOL.dictTypeData('zy_unit_type', formData.kind) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="标题">
				{{ formData.title }}
			</a-descriptions-item>
			<a-descriptions-item label="正文">
				<div v-html="formData.sourceText"></div>
			</a-descriptions-item>
			<a-descriptions-item label="字数">
				{{ formData.wordCount }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				<a-tag :color="$TOOL.dictTypeColor('zy_unit_status', formData.status)">{{ $TOOL.dictTypeData('zy_unit_status', formData.status) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="排序">
				{{ formData.position }}
			</a-descriptions-item>
			<a-descriptions-item label="创建人">
				{{ formData.createUser }}
			</a-descriptions-item>
			<a-descriptions-item label="创建时间">
				{{ formData.createTime }}
			</a-descriptions-item>
			<a-descriptions-item label="更新人">
				{{ formData.updateUser }}
			</a-descriptions-item>
			<a-descriptions-item label="更新时间">
				{{ formData.updateTime }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyUnitDetail">
	import { cloneDeep } from 'lodash-es'
	import zyUnitApi from '@/api/canvas/zyUnitApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyUnitApi.zyUnitDetail({ id: record.id }).then((data) => {
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
