<template>
	<xn-form-container
		title="详情"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-descriptions :column="1" bordered size="small">
			<a-descriptions-item label="DELETE_FLAG">
				{{ formData.deleteFlag }}
			</a-descriptions-item>
			<a-descriptions-item label="团队名称">
				{{ formData.name }}
			</a-descriptions-item>
			<a-descriptions-item label="团队描述">
				{{ formData.description }}
			</a-descriptions-item>
			<a-descriptions-item label="状态：active=正常 / deleted=已删除">
				<a-tag :color="$TOOL.dictTypeColor('zy_team_status', formData.status)">{{ $TOOL.dictTypeData('zy_team_status', formData.status) }}</a-tag>
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyTeamDetail">
	import { cloneDeep } from 'lodash-es'
	import zyTeamApi from '@/api/canvas/zyTeamApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyTeamApi.zyTeamDetail({ id: record.id }).then((data) => {
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
