<template>
	<xn-form-container
		title="详情"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-descriptions :column="1" bordered size="small">
			<a-descriptions-item label="项目">
				{{ formData.projectId }}
			</a-descriptions-item>
			<a-descriptions-item label="标题">
				{{ formData.title }}
			</a-descriptions-item>
			<a-descriptions-item label="介质类型">
				<a-tag :color="$TOOL.dictTypeColor('zy_asset_type', formData.mediaType)">{{ $TOOL.dictTypeData('zy_asset_type', formData.mediaType) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="分类">
				{{ formData.category }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				{{ formData.status }}
			</a-descriptions-item>
			<a-descriptions-item label="主版本">
				{{ formData.primaryVersionId }}
			</a-descriptions-item>
			<a-descriptions-item label="版本数">
				{{ formData.versionCount }}
			</a-descriptions-item>
			<a-descriptions-item label="用途">
				{{ formData.usages }}
			</a-descriptions-item>
			<a-descriptions-item label="所属文件夹id">
				{{ formData.folderId }}
			</a-descriptions-item>
			<a-descriptions-item label="排序">
				{{ formData.position }}
			</a-descriptions-item>
			<a-descriptions-item label="存储key">
				{{ formData.storageKey }}
			</a-descriptions-item>
			<a-descriptions-item label="时长(毫秒)">
				{{ formData.durationMs }}
			</a-descriptions-item>
			<a-descriptions-item label="预览文本">
				{{ formData.previewText }}
			</a-descriptions-item>
			<a-descriptions-item label="关联角色id">
				{{ formData.characterId }}
			</a-descriptions-item>
			<a-descriptions-item label="来源">
				{{ formData.source }}
			</a-descriptions-item>
			<a-descriptions-item label="扩展JSON">
				{{ formData.extJson }}
			</a-descriptions-item>
			<a-descriptions-item label="封面图片URL">
				{{ formData.coverUrl }}
			</a-descriptions-item>
			<a-descriptions-item label="删除标志">
				{{ formData.deleteFlag }}
			</a-descriptions-item>
			<a-descriptions-item label="标签(JSON数组)">
				{{ formData.tags }}
			</a-descriptions-item>
			<a-descriptions-item label="备注信息">
				{{ formData.note }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyAssetDetail">
	import { cloneDeep } from 'lodash-es'
	import zyAssetApi from '@/api/canvas/zyAssetApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyAssetApi.zyAssetDetail({ id: record.id }).then((data) => {
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
