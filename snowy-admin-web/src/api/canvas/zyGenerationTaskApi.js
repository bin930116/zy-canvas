import { baseRequest } from '@/utils/request'

const request = (url, ...arg) => baseRequest(`/canvas/generationtask/` + url, ...arg)

/**
 * 任务队列信息Api接口管理器
 *
 * @author hanbin
 * @date  2026/09/08 14:24
 **/
export default {
	// 获取任务队列信息分页
	zyGenerationTaskPage(data) {
		return request('page', data, 'get')
	},
	// 提交任务队列信息表单 edit为true时为编辑，默认为新增
	zyGenerationTaskSubmitForm(data, edit = false) {
		return request(edit ? 'edit' : 'add', data)
	},
	// 删除任务队列信息
	zyGenerationTaskDelete(data) {
		return request('delete', data)
	},
	// 获取任务队列信息详情
	zyGenerationTaskDetail(data) {
		return request('detail', data, 'get')
	},
	// 下载任务队列信息导入模板
    zyGenerationTaskDownloadTemplate(data) {
		return request('downloadImportTemplate', data, 'get', {
			responseType: 'blob'
		})
    },
	// 导入任务队列信息
	zyGenerationTaskImport(data) {
		return request('importData', data)
	},
	// 导出任务队列信息
	zyGenerationTaskExport(data) {
		return request('exportData', data, 'post', {
			responseType: 'blob'
		})
	},
}
