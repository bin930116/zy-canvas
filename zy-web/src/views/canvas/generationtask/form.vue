<template>
	<xn-form-container
		:title="formData.id ? '编辑任务队列信息' : '增加任务队列信息'"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="horizontal">
			<a-row :gutter="16">
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="用户：" name="userId">
						<a-input v-model:value="formData.userId" placeholder="请输入用户" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="项目：" name="projectId">
						<a-input v-model:value="formData.projectId" placeholder="请输入项目" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="任务类型：" name="type">
						<a-input v-model:value="formData.type" placeholder="请输入任务类型" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="进度：" name="progress">
						<a-slider v-model:value="formData.progress" :max="1000" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="状态：" name="status">
						<a-select v-model:value="formData.status" placeholder="请选择状态" :options="statusOptions" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="阶段：" name="stage">
						<a-input v-model:value="formData.stage" placeholder="请输入阶段" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提示词：" name="prompt">
						<a-textarea v-model:value="formData.prompt" placeholder="请输入提示词" :auto-size="{ minRows: 3, maxRows: 5 }" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="操作：" name="operation">
						<a-input v-model:value="formData.operation" placeholder="请输入操作" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商：" name="provider">
						<a-input v-model:value="formData.provider" placeholder="请输入提供商" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="模型：" name="model">
						<a-input v-model:value="formData.model" placeholder="请输入模型" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商请求id：" name="providerRequestId">
						<a-input v-model:value="formData.providerRequestId" placeholder="请输入提供商请求id" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商取消状态：" name="providerCancelStatus">
						<a-input v-model:value="formData.providerCancelStatus" placeholder="请输入提供商取消状态" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商取消错误：" name="providerCancelError">
						<a-input v-model:value="formData.providerCancelError" placeholder="请输入提供商取消错误" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商取消尝试次数：" name="providerCancelAttempts">
						<a-input v-model:value="formData.providerCancelAttempts" placeholder="请输入提供商取消尝试次数" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商取消请求时间：" name="providerCancelRequestedAt">
						<a-date-picker v-model:value="formData.providerCancelRequestedAt" value-format="YYYY-MM-DD HH:mm:ss" show-time placeholder="请选择提供商取消请求时间" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商取消时间：" name="providerCancelledAt">
						<a-date-picker v-model:value="formData.providerCancelledAt" value-format="YYYY-MM-DD HH:mm:ss" show-time placeholder="请选择提供商取消时间" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="错误码：" name="errorCode">
						<a-input v-model:value="formData.errorCode" placeholder="请输入错误码" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="官方状态：" name="officialStatus">
						<a-input v-model:value="formData.officialStatus" placeholder="请输入官方状态" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="预览URL：" name="previewUrl">
						<a-input v-model:value="formData.previewUrl" placeholder="请输入预览URL" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="预览类型：" name="previewKind">
						<a-input v-model:value="formData.previewKind" placeholder="请输入预览类型" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="预览海报URL：" name="previewPosterUrl">
						<a-input v-model:value="formData.previewPosterUrl" placeholder="请输入预览海报URL" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="输入JSON：" name="inputJson">
						<a-input v-model:value="formData.inputJson" placeholder="请输入输入JSON" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="结果JSON：" name="resultJson">
						<a-input v-model:value="formData.resultJson" placeholder="请输入结果JSON" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="结果状态：" name="resultState">
						<a-input v-model:value="formData.resultState" placeholder="请输入结果状态" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="文本草稿：" name="textDraft">
						<a-input v-model:value="formData.textDraft" placeholder="请输入文本草稿" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="错误信息：" name="error">
						<a-input v-model:value="formData.error" placeholder="请输入错误信息" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="尝试次数：" name="attempts">
						<a-input v-model:value="formData.attempts" placeholder="请输入尝试次数" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="开始时间：" name="startedAt">
						<a-date-picker v-model:value="formData.startedAt" value-format="YYYY-MM-DD HH:mm:ss" show-time placeholder="请选择开始时间" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="完成时间：" name="completedAt">
						<a-date-picker v-model:value="formData.completedAt" value-format="YYYY-MM-DD HH:mm:ss" show-time placeholder="请选择完成时间" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="客户端上下文JSON：" name="clientContextJson">
						<a-input v-model:value="formData.clientContextJson" placeholder="请输入客户端上下文JSON" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="处理该任务的Worker ID：" name="workerId">
						<a-input v-model:value="formData.workerId" placeholder="请输入处理该任务的Worker ID" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="任务租约过期时间：" name="leaseExpiresAt">
						<a-date-picker v-model:value="formData.leaseExpiresAt" value-format="YYYY-MM-DD HH:mm:ss" show-time placeholder="请选择任务租约过期时间" style="width: 100%" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="已重试次数：" name="retryCount">
						<a-input v-model:value="formData.retryCount" placeholder="请输入已重试次数" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="最大重试次数：" name="maxRetries">
						<a-input v-model:value="formData.maxRetries" placeholder="请输入最大重试次数" allow-clear />
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

<script setup name="zyGenerationTaskForm">
	import tool from '@/utils/tool'
	import { cloneDeep } from 'lodash-es'
	import { required } from '@/utils/formRules'
	import zyGenerationTaskApi from '@/api/canvas/zyGenerationTaskApi'
	// 抽屉状态
	const open = ref(false)
	const emit = defineEmits({ successful: null })
	const formRef = ref()
	// 表单数据
	const formData = ref({})
	const submitLoading = ref(false)
	const statusOptions = ref([])

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		if (record) {
			let recordData = cloneDeep(record)
			formData.value = Object.assign({}, recordData)
		}
		statusOptions.value = tool.dictList('zy_task_status')
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
				zyGenerationTaskApi
					.zyGenerationTaskSubmitForm(formDataParam, formDataParam.id)
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
