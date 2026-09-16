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
			<a-descriptions-item label="成员">
				{{ formData.userId }}
			</a-descriptions-item>
			<a-descriptions-item label="成员角色">
				<a-tag :color="$TOOL.dictTypeColor('zy_member_role', formData.role)">{{ $TOOL.dictTypeData('zy_member_role', formData.role) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				<a-tag :color="$TOOL.dictTypeColor('zy_member_status', formData.status)">{{ $TOOL.dictTypeData('zy_member_status', formData.status) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="邀请人">
				{{ formData.invitedBy }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyTeamMemberDetail">
	import { cloneDeep } from 'lodash-es'
	import zyTeamMemberApi from '@/api/canvas/zyTeamMemberApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyTeamMemberApi.zyTeamMemberDetail({ id: record.id }).then((data) => {
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
