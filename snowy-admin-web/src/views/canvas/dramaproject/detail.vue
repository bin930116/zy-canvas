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
			<a-descriptions-item label="名称">
				{{ formData.name }}
			</a-descriptions-item>
			<a-descriptions-item label="类型">
				{{ formData.type }}
			</a-descriptions-item>
			<a-descriptions-item label="画幅比">
				{{ formData.aspectRatio }}
			</a-descriptions-item>
			<a-descriptions-item label="来源类型">
				{{ formData.sourceType }}
			</a-descriptions-item>
			<a-descriptions-item label="描述">
				{{ formData.description }}
			</a-descriptions-item>
			<a-descriptions-item label="封面资源id">
				{{ formData.coverResourceId }}
			</a-descriptions-item>
			<a-descriptions-item label="风格预设id">
				{{ formData.stylePresetId }}
			</a-descriptions-item>
			<a-descriptions-item label="风格配置JSON">
				{{ formData.styleProfileJson }}
			</a-descriptions-item>
			<a-descriptions-item label="默认图片模型">
				{{ formData.defaultImageModel }}
			</a-descriptions-item>
			<a-descriptions-item label="默认视频模型">
				{{ formData.defaultVideoModel }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				{{ formData.status }}
			</a-descriptions-item>
			<a-descriptions-item label="乐观锁版本号">
				{{ formData.revision }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyDramaProjectDetail">
	import { cloneDeep } from 'lodash-es'
	import zyDramaProjectApi from '@/api/canvas/zyDramaProjectApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyDramaProjectApi.zyDramaProjectDetail({ id: record.id }).then((data) => {
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
