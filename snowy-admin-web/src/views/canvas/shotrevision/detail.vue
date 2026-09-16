<template>
	<xn-form-container
		title="详情"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-descriptions :column="1" bordered size="small">
			<a-descriptions-item label="分镜id">
				{{ formData.shotId }}
			</a-descriptions-item>
			<a-descriptions-item label="版本号">
				{{ formData.version }}
			</a-descriptions-item>
			<a-descriptions-item label="剧情描述">
				{{ formData.plotDescription }}
			</a-descriptions-item>
			<a-descriptions-item label="动作">
				{{ formData.action }}
			</a-descriptions-item>
			<a-descriptions-item label="台词">
				{{ formData.dialogue }}
			</a-descriptions-item>
			<a-descriptions-item label="景别">
				{{ formData.shotSize }}
			</a-descriptions-item>
			<a-descriptions-item label="机位角度">
				{{ formData.cameraAngle }}
			</a-descriptions-item>
			<a-descriptions-item label="删除标志">
				{{ formData.deleteFlag }}
			</a-descriptions-item>
			<a-descriptions-item label="运镜">
				{{ formData.cameraMovement }}
			</a-descriptions-item>
			<a-descriptions-item label="时长(毫秒)">
				{{ formData.durationMs }}
			</a-descriptions-item>
			<a-descriptions-item label="图片提示词">
				{{ formData.imagePrompt }}
			</a-descriptions-item>
			<a-descriptions-item label="视频提示词">
				{{ formData.videoPrompt }}
			</a-descriptions-item>
			<a-descriptions-item label="负面提示词">
				{{ formData.negativePrompt }}
			</a-descriptions-item>
			<a-descriptions-item label="连续性说明">
				{{ formData.continuityNotes }}
			</a-descriptions-item>
			<a-descriptions-item label="动作节拍JSON">
				{{ formData.actionBeatsJson }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyShotRevisionDetail">
	import { cloneDeep } from 'lodash-es'
	import zyShotRevisionApi from '@/api/canvas/zyShotRevisionApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyShotRevisionApi.zyShotRevisionDetail({ id: record.id }).then((data) => {
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
