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
			<a-descriptions-item label="删除标志">
				{{ formData.deleteFlag }}
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
			<a-descriptions-item label="来源">
				{{ formData.source }}
			</a-descriptions-item>
			<a-descriptions-item label="附加详情JSON">
				{{ formData.detailsJson }}
			</a-descriptions-item>
			<a-descriptions-item label="已解析的资产id">
				{{ formData.resolvedAssetId }}
			</a-descriptions-item>
			<a-descriptions-item label="章节/剧集">
				{{ formData.unitId }}
			</a-descriptions-item>
			<a-descriptions-item label="镜头">
				{{ formData.shotId }}
			</a-descriptions-item>
			<a-descriptions-item label="名称">
				{{ formData.name }}
			</a-descriptions-item>
			<a-descriptions-item label="名称">
				{{ formData.nameKey }}
			</a-descriptions-item>
			<a-descriptions-item label="分类">
				{{ formData.category }}
			</a-descriptions-item>
			<a-descriptions-item label="状态">
				<a-tag :color="$TOOL.dictTypeColor('zy_asset', formData.status)">{{ $TOOL.dictTypeData('zy_asset', formData.status) }}</a-tag>
			</a-descriptions-item>
			<a-descriptions-item label="更新时间">
				{{ formData.updateTime }}
			</a-descriptions-item>
		</a-descriptions>
	</xn-form-container>
</template>

<script setup name="zyAssetCandidateDetail">
	import { cloneDeep } from 'lodash-es'
	import zyAssetCandidateApi from '@/api/canvas/zyAssetCandidateApi'

	// 抽屉状态
	const open = ref(false)
	// 表单数据
	const formData = ref({})

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		zyAssetCandidateApi.zyAssetCandidateDetail({ id: record.id }).then((data) => {
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
