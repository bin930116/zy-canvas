<template>
	<xn-form-container
		:title="formData.id ? '编辑模型配置' : '增加模型配置'"
		:width="700"
		v-model:open="open"
		:destroy-on-close="true"
		@close="onClose"
	>
		<a-form ref="formRef" :model="formData" :rules="formRules" layout="horizontal">
			<a-row :gutter="16">
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="模型标识：" name="modelKey">
						<a-input v-model:value="formData.modelKey" placeholder="请输入模型标识" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="模型名称：" name="modelName">
						<a-input v-model:value="formData.modelName" placeholder="请输入模型名称" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="提供商：" name="providerName">
						<a-input v-model:value="formData.providerName" placeholder="请输入提供商" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="能力类型：" name="capability">
						<a-select v-model:value="formData.capability" placeholder="请选择能力类型" :options="capabilityOptions" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="协议类型：" name="protocol">
						<a-select v-model:value="formData.protocol" placeholder="请选择协议类型" :options="protocolOptions" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="baseUrl：" name="baseUrl">
						<a-input v-model:value="formData.baseUrl" placeholder="请输入baseUrl" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="API密钥：" name="apiKey">
						<a-input v-model:value="formData.apiKey" placeholder="请输入API密钥" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="并发数：" name="maxConcurrency">
						<a-input v-model:value="formData.maxConcurrency" placeholder="请输入并发数" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="超时时间（秒）：" name="timeoutSeconds">
						<a-input v-model:value="formData.timeoutSeconds" placeholder="请输入超时时间（秒）" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="能力规格JSON：" name="capabilitySpecJson">
						<a-input v-model:value="formData.capabilitySpecJson" placeholder="请输入能力规格JSON" allow-clear />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="状态：" name="status">
						<a-select v-model:value="formData.status" placeholder="请选择状态" :options="statusOptions" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="是否默认：" name="isDefault">
						<a-select v-model:value="formData.isDefault" placeholder="请选择是否默认" :options="isDefaultOptions" />
					</a-form-item>
				</a-col>
				<a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
					<a-form-item label="排序码：" name="sortCode">
						<a-input-number v-model:value="formData.sortCode" :min="1" :max="10000" style="width: 100%" />
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

<script setup name="zyModelForm">
	import tool from '@/utils/tool'
	import { cloneDeep } from 'lodash-es'
	import { required } from '@/utils/formRules'
	import zyModelApi from '@/api/canvas/zyModelApi'
	// 抽屉状态
	const open = ref(false)
	const emit = defineEmits({ successful: null })
	const formRef = ref()
	// 表单数据
	const formData = ref({})
	const submitLoading = ref(false)
	const capabilityOptions = ref([])
	const protocolOptions = ref([])
	const statusOptions = ref([])
	const isDefaultOptions = ref([])

	// 打开抽屉
	const onOpen = (record) => {
		open.value = true
		if (record) {
			let recordData = cloneDeep(record)
			formData.value = Object.assign({}, recordData)
		}
		capabilityOptions.value = tool.dictList('MODEL_TYPE')
		protocolOptions.value = tool.dictList('MODEL_XY_TYPE')
		statusOptions.value = tool.dictList('MODEL_STATUS')
		isDefaultOptions.value = tool.dictList('COMMON_WHETHER')
	}
	// 关闭抽屉
	const onClose = () => {
		formRef.value.resetFields()
		formData.value = {}
		open.value = false
	}
	// 默认要校验的
	const formRules = {
		modelKey: [required('请输入模型标识')],
		modelName: [required('请输入模型名称')],
		capability: [required('请输入能力类型')],
		protocol: [required('请输入协议类型')],
		baseUrl: [required('请输入baseUrl')],
		apiKey: [required('请输入API密钥')],
		maxConcurrency: [required('请输入并发数')],
		timeoutSeconds: [required('请输入超时时间（秒）')],
		status: [required('请输入状态')],
		isDefault: [required('请输入是否默认')],
	}
	// 验证并提交数据
	const onSubmit = () => {
		formRef.value
			.validate()
			.then(() => {
				submitLoading.value = true
				const formDataParam = cloneDeep(formData.value)
				zyModelApi
					.zyModelSubmitForm(formDataParam, formDataParam.id)
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
