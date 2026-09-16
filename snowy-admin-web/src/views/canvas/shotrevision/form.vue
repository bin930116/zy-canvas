<template>
	<xn-form-container
		:title="formData.id ? '编辑短剧项目分镜版本' : '增加短剧项目分镜版本'"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="horizontal">
			<a-row :gutter="16">
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="分镜id：" name="shotId">
						<a-input v-model:value="formData.shotId" placeholder="请输入分镜id" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="版本号：" name="version">
						<a-input v-model:value="formData.version" placeholder="请输入版本号" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="剧情描述：" name="plotDescription">
						<a-textarea v-model:value="formData.plotDescription" placeholder="请输入剧情描述" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="动作：" name="action">
						<a-input v-model:value="formData.action" placeholder="请输入动作" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="台词：" name="dialogue">
						<a-input v-model:value="formData.dialogue" placeholder="请输入台词" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="景别：" name="shotSize">
						<a-input v-model:value="formData.shotSize" placeholder="请输入景别" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="机位角度：" name="cameraAngle">
						<a-input v-model:value="formData.cameraAngle" placeholder="请输入机位角度" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="运镜：" name="cameraMovement">
						<a-input v-model:value="formData.cameraMovement" placeholder="请输入运镜" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="时长(毫秒)：" name="durationMs">
						<a-input-number v-model:value="formData.durationMs" :min="1" :max="10000" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="图片提示词：" name="imagePrompt">
						<a-textarea v-model:value="formData.imagePrompt" placeholder="请输入图片提示词" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="视频提示词：" name="videoPrompt">
						<a-textarea v-model:value="formData.videoPrompt" placeholder="请输入视频提示词" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="负面提示词：" name="negativePrompt">
						<a-textarea v-model:value="formData.negativePrompt" placeholder="请输入负面提示词" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="连续性说明：" name="continuityNotes">
						<a-textarea v-model:value="formData.continuityNotes" placeholder="请输入连续性说明" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="动作节拍JSON：" name="actionBeatsJson">
						<a-textarea v-model:value="formData.actionBeatsJson" placeholder="请输入动作节拍JSON" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
			</a-row>
		</a-form>
		<template #footer>
			<a-button style="margin-right: 8px" @click="onClose">关闭</a-button>
			<a-button type="primary" @click="onSubmit" :loading="submitLoading">保存</a-button>
		</template>
	</xn-form-container>
</template>

<script setup name="zyShotRevisionForm">
	import { cloneDeep } from 'lodash-es'
	import { required } from '@/utils/formRules'
	import zyShotRevisionApi from '@/api/canvas/zyShotRevisionApi'
	// 抽屉状态
	const open = ref(false)
	const emit = defineEmits({ successful: null })
	const formRef = ref()
	// 表单数据
	const formData = ref({})
	const submitLoading = ref(false)

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		if (record) {
			let recordData = cloneDeep(record)
			formData.value = Object.assign({}, recordData)
		}
	}
	// 关闭抽屉
	const onClose = () => {
		formRef.value.resetFields()
		formData.value = {}
		open.value = false
	}
	// 默认要校验的
	const formRules = {
	}
	// 验证并提交数据
	const onSubmit = () => {
		formRef.value
			.validate()
			.then(() => {
				submitLoading.value = true
				const formDataParam = cloneDeep(formData.value)
				zyShotRevisionApi
					.zyShotRevisionSubmitForm(formDataParam, formDataParam.id)
					.then(() => {
						onClose()
						emit('successful')
					})
					.finally(() => {
						submitLoading.value = false
					})
			})
			.catch(() => {})
	}
	// 抛出函数
	defineExpose({
		onOpen
	})
</script>
