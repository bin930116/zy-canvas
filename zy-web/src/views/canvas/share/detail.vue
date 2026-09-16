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
			<a-descriptions-item label="分享token">
				{{ formData.token }}
			</a-descriptions-item>
			<a-descriptions-item label="是否启用">
				<a-tag :color="$TOOL.dictTypeColor('COMMON_WHETHER', formData.enabled)">{{ $TOOL.dictTypeData('COMMON_WHETHER', formData.enabled) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="过期时间">
				{{ formData.expiresAt }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyShareDetail">
	import { cloneDeep } from 'lodash-es'
	import zyShareApi from '@/api/canvas/zyShareApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyShareApi.zyShareDetail({ id: record.id }).then((data) => {
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
